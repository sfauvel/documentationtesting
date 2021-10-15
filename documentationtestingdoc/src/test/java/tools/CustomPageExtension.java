package tools;

import org.sfvl.doctesting.junitextension.HtmlPageExtension;
import org.sfvl.doctesting.utils.DocPath;

public class CustomPageExtension extends HtmlPageExtension {
    public String content(Class<?> clazz) {
        return String.join("\n",
                ":toc: left",
                super.content(clazz)
        );
    }
}
