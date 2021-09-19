let sub = document.getElementById('subsend');
let res = document.getElementById('subres');
let fname = document.getElementById("fname");
let lname = document.getElementById("lname");
const rbs = document.querySelectorAll('input[name="attendance"]');
sub.addEventListener("click", submited);
res.addEventListener("click", reset);
function submited() {
    let selectedValue;
    for (const rb of rbs) {
        if (rb.checked) {
            selectedValue = rb.value;
            break;
        }
    }
    let table = document.getElementById("table");
    let row = table.insertRow(0);
    let cell1 = row.insertCell(0);
    let cell2 = row.insertCell(1);
    let cell3 = row.insertCell(2);
    DeleteData();
    cell1.innerHTML = fname.value;
    cell2.innerHTML = lname.value;
    cell3.innerHTML = selectedValue.value;
    alert("sdf");
    sub.addEventListener('click', addData);
    let arr = [];

    function addData(){
        DeleteData();
        getData();
        arr.push({
            fname:fname.value,
            lname:lname.value,
            chos:selectedValue.value
        });
        localStorage.setItem("localData", JSON.stringify(arr));
    }

    function getData(){
        let str = localStorage.getItem("localData");
        if (str!= null)
            arr = JSON.parse(str);
    }

    function DeleteData(){
        localStorage.clear();
    }
}
function reset() {
    alert("sdf");
}