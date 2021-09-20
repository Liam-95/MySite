"use strict";
// some jquery code for toggle the menu
$(function () {
    $("#navbarToggle").blur(function (event) {
        $('#navbarToggle').click();
        $('html').css('-webkit-tap-highlight-color', 'rgba(0, 0, 0, 0)');
        $("#collapsable-nav").collapse('hide');
    });
});
// game global variables
let counter = 0;
let picked = ''
let sub = document.getElementById('subB');
let userIn = document.getElementById("userInput");
let count = document.getElementById('count');
let stat = document.getElementById('status')
let hint = document.getElementById('hint');
let reset = document.getElementById('reset');
reset.addEventListener("click", customReset)
let characters = 'abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()';
sub.addEventListener("click", gameSubmit);

// on click submit we start the game
function gameSubmit() {
    if (counter === 0) {
        picked = randomChar();
    }
    if (stat.innerHTML !== 'Success! great job!') {
        counter++;
    }
    let guessed = userIn.value.toLowerCase();
    if (guessed.length !== 1 || !characters.includes(guessed)) {
        alert("You need to guess one character (letter | digit | special character)")
        userIn.value = "";
        counter--;
    } else if (guessed === picked) {
        stat.innerHTML = 'Success! great job!'
        stat.style.backgroundColor = "yellow";
        count.innerHTML = "Attempts: " + counter.toString();
        hint.innerHTML = "";
    } else {
        stat.innerHTML = 'Failed! try again!'
        userIn.value = "";
        stat.style.backgroundColor = "red";
        count.innerHTML = "Attempts: " + counter.toString();
        if (counter > 2) {
            if (picked >= '0' && picked <= '9')
                hint.innerHTML = 'Hint: its a digit!'
            else if (isLetter(picked))
                hint.innerHTML = 'Hint: its a letter!'
            else
                hint.innerHTML = 'Hint: its a special character!'
            hint.style.backgroundColor = 'lightblue'
        }
        userIn.focus();
    }
}

// generate random char
function randomChar() {
    return characters.charAt(Math.floor(Math.random() * characters.length));
}

// reset the game
function customReset() {
    let params = document.querySelectorAll('.pgame');
    for (let i = 0; i < params.length; i++) {
        params[i].innerHTML = "";
    }
    stat.style.backgroundColor = ""
    userIn.value = "";
    counter = 0;
    picked = ''
}

function isLetter(str) {
    return str.length === 1 && str.match(/[a-z]/i);
}