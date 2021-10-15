package tools;

import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;

public class RessourcePageExtension extends HtmlPageExtension {
    public String content(Class<?> clazz) {
        return String.join("\n",
                ":toc: left",
                String.format("include::%s[]", new DocPath(clazz).resource().from(clazz))
        );
    }
}
