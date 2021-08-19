package smarticulous;

import smarticulous.db.Exercise;
import smarticulous.db.Submission;
import smarticulous.db.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Smarticulous {

    /**
     * The connection to the underlying DB.
     * <p>
     * null if the db has not yet been opened.
     */
    private Connection db;

    /**
     * Open the smarticulous.Smarticulous SQLite database.
     * <p>
     * This should open the database, creating a new one if necessary, and set the {@link #db} field
     * to the new connection.
     * <p>
     * The open method should make sure the database contains the following tables, creating them if necessary:
     * <p>
     * - A ``User`` table containing the following columns (with their types):
     * <p>
     * =========  =====================
     * Column     Type
     * =========  =====================
     * UserId     Integer (Primary Key)
     * Username   Text
     * Firstname  Text
     * Lastname   Text
     * Password   Text
     * =========  =====================
     * <p>
     * - An ``smarticulous.db.Exercise`` table containing the following columns:
     * <p>
     * ============  =====================
     * Column        Type
     * ============  =====================
     * ExerciseId    Integer (Primary Key)
     * Name          Text
     * DueDate       Integer
     * ============  =====================
     * <p>
     * - A ``Question`` table containing the following columns:
     * <p>
     * ============  =====================
     * Column         Type
     * ============  =====================
     * ExerciseId     Integer
     * QuestionId     Integer
     * Name           Text
     * Desc           Text
     * Points         Integer
     * ============  =====================
     * <p>
     * In this table the combination of ``ExerciseId``,``QuestionId`` together comprise the primary key.
     * <p>
     * - A ``smarticulous.db.Submission`` table containing the following columns:
     * <p>
     * ===============  =====================
     * Column            Type
     * ===============  =====================
     * SubmissionId      Integer (Primary Key)
     * UserId            Integer
     * ExerciseId        Integer
     * SubmissionTime    Integer
     * ===============  =====================
     * <p>
     * - A ``QuestionGrade`` table containing the following columns:
     * <p>
     * ===============  =====================
     * Column            Type
     * ===============  =====================
     * SubmissionId      Integer
     * QuestionId        Integer
     * Grade             Real
     * ===============  =====================
     * <p>
     * In this table the combination of ``SubmissionId``,``QuestionId`` together comprise the primary key.
     *
     * @param dburl The JDBC url of the database to open (will be of the form "jdbc:sqlite:...")
     * @return the new connection
     */
    public Connection openDB(String dburl) throws SQLException {
        // CREATING TABLES IF NOT EXISTS
        String userTable = "CREATE TABLE IF NOT EXISTS User(UserId Integer PRIMARY KEY,Username Text Unique,Firstname Text,Lastname Text,Password Text)";
        String exerciseTable = "CREATE TABLE IF NOT EXISTS Exercise(ExerciseId Integer PRIMARY KEY,Name Text,DueDate Integer)";
        String questionTable = "CREATE TABLE IF NOT EXISTS Question(ExerciseId Integer,QuestionId integer,Name Text,Desc Text,Points Integer,PRIMARY KEY (ExerciseId,QuestionId))";
        String submissionTable = "CREATE TABLE IF NOT EXISTS Submission(SubmissionId Integer Primary Key,UserId Integer,ExerciseId Integer,SubmissionTime Integer)";
        String questionGrade = "CREATE TABLE IF NOT EXISTS QuestionGrade(SubmissionId Integer,QuestionId Integer,Grade Real,PRIMARY KEY (SubmissionId,QuestionId))";
        String[] statements = {userTable, exerciseTable, questionTable, submissionTable, questionGrade};
        this.db = DriverManager.getConnection(dburl);
        Statement stmt = db.createStatement();
        // loop trough sql statements and executeUpdate then return the connection
        for (String sql : statements) {
            stmt.executeUpdate(sql);
        }
        return db;
    }


    /**
     * Close the DB if it is open.
     */
    public void closeDB() throws SQLException {
        if (db != null) {
            db.close();
            db = null;
        }
    }
    // =========== User Management =============

    /**
     * Add a user to the database / modify an existing user.
     * <p>
     * Add the user to the database if they don't exist. If a user with user.username does exist,
     * update their password and firstname/lastname in the database.
     *
     * @param user the user
     * @return the userid.
     */
    public int addOrUpdateUser(User user, String password) throws SQLException {
        PreparedStatement pst;
        try {
            // first check whether the user exists already or not
            boolean found = isItThere("User", "Username", user.username, true);
            // if exists
            if (found) {
                String query2 = "UPDATE User SET Firstname=?,Lastname=?,Password=? WHERE Username=?";
                pst = db.prepareStatement(query2);
                pst.setString(1, user.firstname);
                pst.setString(2, user.lastname);
                pst.setString(3, password);
                pst.setString(4, user.username);
                pst.executeUpdate();
                // else user do not exists in the table and we insert the relevant fields.
            } else {
                String query3 = "INSERT INTO User ('Username','Firstname','Lastname','Password') VALUES(?,?,?,?)";
                pst = db.prepareStatement(query3);
                pst.setString(1, user.username);
                pst.setString(2, user.firstname);
                pst.setString(3, user.lastname);
                pst.setString(4, password);
                pst.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // finally we return the userid from the table using again prepareStatement to protect against SQL injection
        String query4 = "SELECT UserId FROM User WHERE Username=?";
        pst = db.prepareStatement(query4);
        pst.setString(1, user.username);
        ResultSet res = pst.executeQuery();
        // returning userid
        return res.getInt("UserId");
    }

    /**
     * Verify a user's login credentials.
     *
     * @param username
     * @param password
     * @return true if the user exists in the database and the password matches; false otherwise.
     * <p>
     * Note: this is totally insecure. For real-life password checking, it's important to store only
     * a password hash
     * @see <a href="https://crackstation.net/hashing-security.html">How to Hash Passwords Properly</a>
     */
    public boolean verifyLogin(String username, String password) throws SQLException {
        PreparedStatement preparedForLogin;
        try {
            // first we check if user exists in the database and if true the if password matches.
            boolean found = isItThere("User", "Username", username, true);
            if (found) {
                String query2 = "SELECT Password FROM User WHERE Username=?";
                preparedForLogin = db.prepareStatement(query2);
                preparedForLogin.setString(1, username);
                ResultSet res = preparedForLogin.executeQuery();
                String pass = res.getString("Password");
                // return True if passwords matches and user exists
                return pass.equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // if arrived here, then user do not exists or password is a mismatch then return false
        return false;
    }

// =========== Exercise Management =============

    /**
     * Add an exercise to the database.
     *
     * @param exercise
     * @return the new exercise id, or -1 if an exercise with this id already existed in the database.
     */
    public int addExercise(Exercise exercise) throws SQLException {
        PreparedStatement preparedForExercise;
        try {
            // we check if exercise exists already in the data base
            boolean found = isItThere("Exercise", "ExerciseId", Integer.toString(exercise.id), false);
            if (found)
                // if found then exists already and return -1
                return -1;
            else {
                // else is not in the data base and we need to insert, using the Exercise objects fields
                String query2 = "INSERT INTO Exercise ('ExerciseId','Name','DueDate') VALUES (?,?,?)";
                preparedForExercise = db.prepareStatement(query2);
                preparedForExercise.setInt(1, exercise.id);
                preparedForExercise.setString(2, exercise.name);
                preparedForExercise.setLong(3, exercise.dueDate.getTime());
                preparedForExercise.executeUpdate();
                // finally we add the exercise questions to the Question table, by looping trough the exercise questions array.
                for (int i = 0; i < exercise.questions.size(); i++) {
                    String query3 = "INSERT INTO Question ('ExerciseId','QuestionId','Name','Desc','Points') VALUES (?,?,?,?,?)";
                    preparedForExercise = db.prepareStatement(query3);
                    preparedForExercise.setInt(1, exercise.id);
                    preparedForExercise.setInt(2, i + 1);
                    preparedForExercise.setString(3, exercise.questions.get(i).name);
                    preparedForExercise.setString(4, exercise.questions.get(i).desc);
                    preparedForExercise.setInt(5, exercise.questions.get(i).points);
                    preparedForExercise.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // finally return exercise.id as requested
        return exercise.id;
    }


    /**
     * Return a list of all the exercises in the database.
     * <p>
     * The list should be sorted by exercise id.
     *
     * @return
     */
    public List<Exercise> loadExercises() throws SQLException {
        // generate list of Exercise
        ArrayList<Exercise> exercisesList = new ArrayList<>();
        int exerciseId;
        long dueDate;
        String name;
        Date date;
        Exercise currentExercise;
        ResultSet resSecond;
        // gel all columns from Exercise table sorted by ExerciseId
        ResultSet res = db.createStatement().executeQuery("SELECT * from Exercise ORDER BY ExerciseId");
        // while next row in Exercise table exists
        while (res.next()) {
            // build from table the Exercise object bit by bit
            exerciseId = res.getInt("ExerciseId");
            dueDate = res.getInt("DueDate");
            name = res.getString("Name");
            // generate new Exercise object with the collected fields
            currentExercise = new Exercise(exerciseId, name, new Date(dueDate));
            // no need to use PreparedStatement as data comes from the database itself and not a user
            resSecond = db.createStatement().executeQuery("SELECT * from Question WHERE ExerciseId='" + exerciseId + "' ORDER BY QuestionId ");
            // while there exists another question WHERE ExerciseId=exerciseId
            // generate the Question fields and add to the currentExercise Question filed
            while (resSecond.next()) {
                String qsName = resSecond.getString("Name");
                String desc = resSecond.getString("Desc");
                int points = resSecond.getInt("Points");
                currentExercise.addQuestion(qsName, desc, points);
            }
            // add the final build Exercise object
            exercisesList.add(currentExercise);
        }
        // when done return the list of Exercise objects
        return exercisesList;
    }

// ========== Submission Storage ===============

    /**
     * Store a submission in the database.
     * The id field of the submission will be ignored if it is -1.
     * <p>
     * Return -1 if the corresponding user doesn't exist in the database.
     *
     * @param submission
     * @return the submission id.
     */
    public int storeSubmission(Submission submission) throws SQLException {
        PreparedStatement storeSubmission;
        boolean goodId = true;
        try {
            // we check if submission exists already in the data base
            boolean found = isItThere("User", "Username", submission.user.username, true);
            if (found) {
                // user exists thus we store the submission,we find what is the UserId (with the sub query below)
                // then we insert new Submission with the relevant fields
                // again using PreparedStatement to protect against SQL injections
                // first we check that submission.id
                int i = 1;
                if (submission.id != -1) {
                    String query = "INSERT INTO Submission ('SubmissionId','UserId','ExerciseId','SubmissionTime') VALUES (?,(SELECT UserId FROM User WHERE Username=?),?,?)";
                    storeSubmission = db.prepareStatement(query);
                    storeSubmission.setInt(i, submission.id);
                    // else submission.id = -1 and we do not insert the value of submission.id and,
                    // it will be auto generate by data base (primary key).
                } else {
                    goodId = false;
                    i--;
                    String query = "INSERT INTO Submission ('UserId','ExerciseId','SubmissionTime') VALUES ((SELECT UserId FROM User WHERE Username=?),?,?)";
                    storeSubmission = db.prepareStatement(query);
                }
                // if we need to insert submission.id then i=1 and we continue to insert values at location 2,3,4
                // else i = 0 and we insert at 1,2,3 (done this way so we dont repeat the code twice =))
                storeSubmission.setString(i + 1, submission.user.username);
                storeSubmission.setInt(i + 2, submission.exercise.id);
                storeSubmission.setLong(i + 3, submission.submissionTime.getTime());
                storeSubmission.executeUpdate();
            } else
                // found - false thus submission do not exists thus return -1
                return -1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // return submission.id iff submission.id != -1
        if (goodId)
            return submission.id;
        // else submission.id = -1 and we need to ask the database for the relevant submission.id
        else {
            // generate the query to get the latest submissionId using last_insert_rowid().
            String query = "SELECT last_insert_rowid() as subId from Submission LIMIT 1";
            try (Statement subId = db.createStatement()) {
                ResultSet rs = subId.executeQuery(query);
                while (rs.next()) {
                    int theSubId = rs.getInt("subId");
                    return theSubId;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    // ============= Submission Query ===============

    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the latest submission for the given exercise by the given user.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getLastSubmission(User, Exercise)}
     *
     * @return PreparedStatement
     */
    PreparedStatement getLastSubmissionGradesStatement() throws SQLException {
        // using the PreparedStatement object
        PreparedStatement lastSubmission;
        // building the query
        // SELECT the requested fields, from the relevant tables ,inner join on
        // Submission.SubmissionId=QuestionGrade.SubmissionId where user.id is equal to the sub-query
        // return value and everything is sorted by SubmissionTime DESC and then by Question.QuestionId.
        String query1 = "SELECT Submission.SubmissionId,QuestionGrade.QuestionId,QuestionGrade.Grade,Submission.submissionTime";
        String query2 = "FROM Question INNER JOIN QuestionGrade INNER JOIN Submission ON (Submission.SubmissionId=QuestionGrade.SubmissionId)";
        String query3 = "WHERE Submission.UserId =(SELECT UserId FROM USER where user.username=?) AND Submission.ExerciseId=? ORDER BY SubmissionTime DESC, (Question.QuestionId) LIMIT ?";
        // adding the Strings to the prepareStatement, now the prepareStatement is ready to be injected with the Parameters
        lastSubmission = db.prepareStatement(query1 + " " + query2 + " " + query3);
        // returning the prepareStatement
        return lastSubmission;
    }

    /**
     * Return a prepared SQL statement that, when executed, will
     * return one row for every question of the <i>best</i> submission for the given exercise by the given user.
     * The best submission is the one whose point total is maximal.
     * <p>
     * The rows should be sorted by QuestionId, and each row should contain:
     * - A column named "SubmissionId" with the submission id.
     * - A column named "QuestionId" with the question id,
     * - A column named "Grade" with the grade for that question.
     * - A column named "SubmissionTime" with the time of submission.
     * <p>
     * Parameter 1 of the prepared statement will be set to the User's username, Parameter 2 to the Exercise Id, and
     * Parameter 3 to the number of questions in the given exercise.
     * <p>
     * This will be used by {@link #getBestSubmission(User, Exercise)}
     */
    PreparedStatement getBestSubmissionGradesStatement() throws SQLException {
        // TODO
        return null;
    }

    /**
     * Return a submission for the given exercise by the given user that satisfies
     * some condition (as defined by an SQL prepared statement).
     * <p>
     * The prepared statement should accept the user name as parameter 1, the exercise id as parameter 2 and a limit on the
     * number of rows returned as parameter 3, and return a row for each question corresponding to the submission, sorted by questionId.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @return
     */
    Submission getSubmission(User user, Exercise exercise, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, user.username);
        stmt.setInt(2, exercise.id);
        stmt.setInt(3, exercise.questions.size());

        ResultSet res = stmt.executeQuery();

        boolean hasNext = res.next();
        if (!hasNext)
            return null;

        int sid = res.getInt("SubmissionId");
        Date submissionTime = new Date(res.getLong("SubmissionTime"));

        float[] grades = new float[exercise.questions.size()];

        for (int i = 0; hasNext; ++i, hasNext = res.next()) {
            grades[i] = res.getFloat("Grade");
        }

        return new Submission(sid, user, exercise, submissionTime, (float[]) grades);
    }

    /**
     * Return the latest submission for the given exercise by the given user.
     * <p>
     * Return null if the user has not submitted the exercise (or is not in the database).
     *
     * @param user
     * @param exercise
     * @return
     */
    public Submission getLastSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getLastSubmissionGradesStatement());
    }


    /**
     * Return the submission with the highest total grade
     *
     * @param user
     * @param exercise
     * @return
     */
    public Submission getBestSubmission(User user, Exercise exercise) throws SQLException {
        return getSubmission(user, exercise, getBestSubmissionGradesStatement());
    }

    /**
     * <p>
     * check whether a row in the wanted table exists where the supplied clause is given,
     * with the given value given.
     * </p>
     *
     * @param from     from which table
     * @param where    set the 'WHERE' clause.
     * @param value    user defined value to look up with (protected against SQL injection).
     * @param isString true iff value type should be considered as String (for the use in pst.setString).
     * @return true if row exists, false otherwise.
     */
    private boolean isItThere(String from, String where, String value, boolean isString) throws SQLException {
        PreparedStatement pst;
        // the following logic is used few times inside this class in order to check the existence of a row in a table.
        // we count the number of rows in the USER table s.t Username= user.Username.
        String queryExits = "SELECT (count(*) > 0) AS FOUND FROM " + from + " WHERE " + where + "=?";
        boolean found = false;
        try {
            // using prepareStatement object to protect against SQL injection.
            pst = db.prepareStatement(queryExits);
            // if String then set value as String.
            if (isString)
                pst.setString(1, value);
                // else it is int as those are the only 2 parameters passed by our methods
            else
                pst.setInt(1, Integer.parseInt(value));
            try (ResultSet rs = pst.executeQuery()) {
                // Only expecting a single result
                if (rs.next()) {
                    // if count == 1 then the user exists and we update the relevant fields.
                    return rs.getBoolean(1); // return ture or false, found or not.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
