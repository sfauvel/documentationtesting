import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// https://plugins.jetbrains.com/docs/intellij/plugin-components.html?from=jetbrains.org#project-open
// https://plugins.jetbrains.com/docs/intellij/ide-infrastructure.html#running-tasks-once

public class DocAsTestStartupActivity implements StartupActivity {

    private final static Logger LOG = Logger.getInstance(DocAsTestStartupActivity.class);
    private static final String DOC_AS_TEST_PROPERTIES_FILENAME = "docAsTest.properties";

    private static final String DEFAULT_SRC_PATH = "src/test/java";
    private static final String DEFAULT_SRC_DOCS = "src/test/docs";

    public static String getSrcDocs() {
        return properties.getProperty("DOC_PATH", DEFAULT_SRC_DOCS);
    }

    public static String getSrcPath() {
        return properties.getProperty("TEST_PATH", DEFAULT_SRC_PATH);
    }

    public static void setProperties(Properties properties) {
        DocAsTestStartupActivity.properties = properties;
    }

    private static Properties properties = null;

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.info("runActivity project:" + project.getName());
        loadProperties(project);
    }

    protected static void loadProperties(Project project) {
        LOG.debug("project: " + project.getName());
        final PsiFile[] propertiesByName = FilenameIndex.getFilesByName(project, DOC_AS_TEST_PROPERTIES_FILENAME, GlobalSearchScope.projectScope(project));
        // TODO we assume there is only one property file with this name in the project.
        // TODO We probably need to load property file by project.
        LOG.debug("properties file found: " + propertiesByName.length);
        if (propertiesByName.length > 0) {
            loadProperties(propertiesByName[0].getVirtualFile());
        }
    }

    private static void loadProperties(VirtualFile virtualFile) {

        try (final InputStream inputStream = virtualFile.getInputStream()) {
            Properties tmp_properties = new Properties();
            tmp_properties.load(inputStream);
            properties = tmp_properties;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
