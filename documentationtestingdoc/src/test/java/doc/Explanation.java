package doc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.MethodReference;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.utils.DocPath;
import tools.RessourcePageExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

@ExtendWith(Explanation.ExplanationPageExtension.class)
public class Explanation {

    public static class ExplanationPageExtension extends RessourcePageExtension {
        @Override
        public String content(Class<?> clazz) {
            return super.content(clazz)
                    + "\n"
                    + String.format("include::%s[leveloffset=+1]", new DocPath(MethodReference.getMethod(Explanation::Videos)).approved().from(clazz));
        }
    }

    @RegisterExtension
    static SimpleApprovalsExtension doc = new SimpleApprovalsExtension();

    @Test
    public void Videos() {
        final String[] videoList = Arrays.stream(Links.class.getDeclaredFields())
                .filter(field -> field.getType().isAssignableFrom(Links.VideoLink.class))
                .map(field -> (Links.VideoLink) getStaticFieldValue(field))
                .map(link -> doc.getFormatter().linkToPage(link.getLink(), link.getDescription()))
                .collect(Collectors.toList()).toArray(new String[0]);


        doc.write(doc.getFormatter().listItems(videoList));
    }

    private Object getStaticFieldValue(Field field) {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


}
