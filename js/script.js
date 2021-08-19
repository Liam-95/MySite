"use strict";
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
let characters = 'abcdefghijklmnopqrstuvwxyz0123456789';
sub.addEventListener("click", gameSubmit);

// on click submit we start the game
function gameSubmit() {
    if (counter === 0) {
        picked = randomChar();
    }
    counter++;
    let guessed = userIn.value.toLowerCase();
    if (guessed.length !== 1 || !characters.includes(guessed)) {
        alert("you need to guess one char (letter or digit)")
        userIn.value = "";
        counter--;
    } else if (guessed === picked) {
        stat.innerHTML = 'success! great job!'
        stat.style.backgroundColor = "yellow";
        count.innerHTML = "attempts: " + counter.toString();
        hint.innerHTML = "";
    } else {
        stat.innerHTML = 'failed! try again!'
        userIn.value = "";
        stat.style.backgroundColor = "red";
        count.innerHTML = "attempts: " + counter.toString();
        if (counter > 2) {
            if (picked >= '0' && picked <= '9')
                hint.innerHTML = 'hint: its a digit!'
            else
                hint.innerHTML = 'hint: its a letter!'
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