package fr.sfvl;

import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;

class RessourcePageExtension extends HtmlPageExtension {
    public String content(Class<?> clazz) {
        return String.format("include::%s[]", new DocPath(clazz).resource().from(clazz));
    }
}
