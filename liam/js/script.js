$(function () {
    $("#navbarToggle").blur(function(event){
        var screenWidth = window.innerWidth;
        if(screenWidth<576){
        $("#collapsable-nav").collapse('hide');
    }
    });
});