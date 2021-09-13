function toggleMenuOnClick(menuItem) {
    menuItem.getElementsByTagName("a")[0].addEventListener(
        'click',
        event => {
            document.querySelectorAll(".sectlevel2 > li").forEach(function(element) {
                if (element != menuItem) {
                    element.classList.remove("menu_open");
                }
            });
            event.currentTarget.parentNode.classList.toggle("menu_open");
        }
    );
}

window.addEventListener( "load",
    function() {
        menusLevel2 = document.querySelectorAll(".sectlevel2 > li");
        menusLevel2.forEach(toggleMenuOnClick);
     }
);