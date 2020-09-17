$(function () {
    $("#navbarToggle").blur(function (event) {
        $('#navbarToggle').click();
        $('html').css('-webkit-tap-highlight-color', 'rgba(0, 0, 0, 0)');
        //var screenWidth = window.innerWidth;
        $("#collapsable-nav").collapse('hide');
    });
});


$(function () {
    $("#navbarToggle").on("click", function (e) {
        if(e.handled !== true) {
            e.handled = true
      } else {
            return false
      }
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