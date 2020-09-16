$(function () {
    $("#navbarToggle").blur(function (event) {
        var screenWidth = window.innerWidth;
        $("#collapsable-nav").collapse('hide');

    });
});
var switchMenuToActive = function () {
    var classes = document.querySelector("#navHomeB").className;
    classes = classes.replace(new RegExp("active", "g"), "");
    document.querySelector("#navHomeB").className = classes;
    // add 'active'
    classes = document.querySelector("#navMenuB").className;
    if (classes.indexOf("active") == -1) {
        classes += " active";
        document.querySelector("#navMenuB").className = classes;
    }
};