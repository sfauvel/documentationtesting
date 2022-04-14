function toggleMenuOnClick(menuItem) {
    menuItem.getElementsByTagName("a")[0].addEventListener(
        'click',
        event => {
            document.querySelectorAll(".sectlevel1 > li").forEach(function(element) {
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
        menus = document.querySelectorAll(".sectlevel1 > li");
        menus.forEach(toggleMenuOnClick);

        // Move logo to the header
        const project_home = document.getElementById('project_home');
        const header = document.getElementById('header');
        header.appendChild(project_home);

     }
);