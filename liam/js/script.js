$(function () {
    $("#navbarToggle").blur(function (event) {
        $('#navbarToggle').click();
        $('html').css('-webkit-tap-highlight-color', 'rgba(0, 0, 0, 0)');
        $("#collapsable-nav").collapse('hide');
    });
});