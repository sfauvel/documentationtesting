package org.sfvl.howto;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.sfvl.codeextraction.CodePath;
import org.sfvl.doctesting.demo.DemoDocumentation;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.doctesting.junitinheritance.ApprovalsBase;
import org.sfvl.doctesting.utils.Config;
import org.sfvl.test_tools.IntermediateHtmlPage;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(IntermediateHtmlPage.class)
public class ProjectOrganization {

    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    private Class<?> toClass(String className) {
        try {
            return Class.forName(className, false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private boolean isSourceClass(Class<?> aClass) {
        return toSourceFile(aClass).isFile();
    }

    private File toSourceFile(Class<?> aClass) {
        return Config.SOURCE_PATH.resolve(CodePath.toPath(aClass)).toFile();
    }

    private GenericVisitorAdapter<Object, List<String>> importVisitor = new GenericVisitorAdapter<Object, List<String>>() {
        @Override
        public Object visit(ImportDeclaration n, List<String> imports) {
            imports.add(n.getName().asString());
            return super.visit(n, imports);
        }
    };

    private String keepImportsOfClass(Set<String> importsToShow, String classImports) {
        return importsToShow.stream()
                .filter(i -> classImports.startsWith(i))
                .findFirst()
                .orElse(null);
    }

    //@Test
    @Disabled
    public void package_dependencies_XXX() throws ClassNotFoundException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("src/main/java"));

        Reflections reflections = new Reflections("org/sfvl", new SubTypesScanner(false));
        final List<File> sourceFiles = reflections.getAllTypes().stream()
                .map(this::toClass)
                .filter(c -> c.getPackage() != DemoDocumentation.class.getPackage())
                .filter(c -> c.getPackage() != ApprovalsBase.class.getPackage())
                .map(this::toSourceFile)
                .distinct()
                .filter(File::isFile)
                .collect(Collectors.toList());

        doc.write(sourceFiles.stream()
                .map(File::getPath)
                .collect(Collectors.joining("\n")));

        Map<String, List<String>> importsByClasses = new HashMap<>();

        Map<String, List<String>> classesByPackages = new HashMap<>();

        for (File sourceFile : sourceFiles) {
            final String path = sourceFile.getPath();
            final String fileName = sourceFile.getName();
            final String fullJavaPath = path.replaceFirst("^" + Config.SOURCE_PATH + "/", "");
            final String packagePath = fullJavaPath.replaceAll("/" + fileName + "$", "");

            final String javaFileKey = fullJavaPath
                    .replaceAll("/", ".")
                    .replaceAll("\\.java$", "");

            if (!classesByPackages.containsKey(packagePath)) {
                classesByPackages.put(packagePath, new ArrayList<>());
            }

            classesByPackages.get(packagePath).add(javaFileKey);

            System.out.println("ProjectOrganization.package_dependencies " + path);
            System.out.println("    => " + packagePath + " - " + fileName);
            System.out.println("    parse => " + packagePath.replaceAll("/", ".") + " - " + fileName);
            CompilationUnit cu = sourceRoot.parse(packagePath.replaceAll("/", "."), fileName);

            final List<String> imports = new ArrayList<>();
            cu.accept(importVisitor, imports);

            System.out.println(packagePath + " - " + fileName);
            for (String anImport : imports) {
                System.out.println(" - " + anImport);
            }

            importsByClasses.put(javaFileKey,
                    imports.stream().filter(imp -> imp.startsWith("org.sfvl")).collect(Collectors.toList()));

        }

        GraphvizGenerator graphvizGenerator = new GraphvizGenerator();
        Map<String, String> importsToShow = new HashMap<>();
        importsToShow.put("org.sfvl", "");

        Stream<GraphvizGenerator.Link> linkStream = importsByClasses.entrySet().stream().flatMap(classListEntry -> {
            Stream<String> importsInClass = classListEntry.getValue().stream()
//                    .map(classImports -> keepImportsOfClass(importsToShow.keySet(), classImports))
//                    .distinct()
                    .filter(Objects::nonNull);

//            return importsInClass.map(e ->
//                    new GraphvizGenerator.Link(
//                            "\"" + classListEntry.getKey() + "\"",
//                            "\"" + e + "\""
//                    )
//            );

            return importsInClass.map(e ->
                    new GraphvizGenerator.Link(
                            toKeyGraphNode(classListEntry.getKey()),
                            toKeyGraphNode(e)
                    )
            );
        });


        Set<String> froms = new HashSet<String>();
        Set<String> tos = new HashSet<String>();

        Set<String> alls = importsByClasses.keySet();

        linkStream
                .peek(link -> froms.add(link.getFrom()))
                .peek(link -> tos.add(link.getTo()))
                .forEach(graphvizGenerator::addLink);

        String allNodes = alls.stream()
                .map(from -> String.format("\"%s\" [fillcolor=\"wheat\" label=\"%s\"]", from, from.replaceFirst("^.*\\.", "")))
                .collect(Collectors.joining("\n", "", "\n"));


        String demoNodes = froms.stream()
                .map(from -> String.format("%s [fillcolor=\"wheat\"]", from, from))
                .collect(Collectors.joining("\n"));


        String libraryNodes = tos.stream()
                .map(to -> String.format("%s [fillcolor=\"palegreen3\"]", to))
                .collect(Collectors.joining("\n"));

        int cluster_number = 0;
        String packages = "";
        for (Map.Entry<String, List<String>> stringListEntry : classesByPackages.entrySet()) {
            cluster_number++;
            final String collect = stringListEntry.getValue().stream()
                    .map(f -> "\"" + f + "\";\n")
                    .collect(Collectors.joining());

            packages += String.join("\n",
                    "subgraph cluster_" + cluster_number + " {",
                    "    label=\"" + stringListEntry.getKey() + "\";",
                    "    rankdir=LR;",
                    "\"" + stringListEntry.getKey().replaceAll("/", ".") + "\" [fillcolor=\"palegreen3\"];",
                    collect,
//                    "    demo [fillcolor=\"wheat\" label=\"Demo\"];",
//                    "    library [fillcolor=\"palegreen3\" label=\"Library\"];",
                    "}",
                    "");

        }

        final String graph = String.join("\n",
                "In these demos, we use libraries:",
                importsToShow.entrySet().stream()
                        .map(e -> "*" + e.getKey() + "*: " + e.getValue())
                        .sorted()
                        .collect(Collectors.joining("\n* ", "\n* ", "\n")),
                "The graph below shows which libraries is used in demos.",

//                graphvizGenerator.generate(
//                        String.join("\n", "node [style=filled]", allNodes),
//                        packages)

                graphvizGenerator.generate("   rankdir=TD;\n" +
                                "    node [margin=0.1 fontcolor=black fontsize=16 width=0.5 shape=rect style=filled]",
                        "")
        );

        doc.write(graph);
//            Reflections reflections = new Reflections("org/sfvl", Scanners.values());
//
//            final String collect = reflections.getMethodsAnnotatedWith(Test.class).stream()
//                    .map(c->c.getName())
//                    .collect(Collectors.joining("\n"));
//            doc.write(collect);
//
//            final List<Class<?>> classes = new ClassFinder().testClasses(this.getClass().getPackage());
//            for (Class<?> aClass : classes) {
//                doc.write(aClass.getName());
//            }
////            Map<Class<?>, List<String>> demoClasses = findDemoClasses().stream().collect(Collectors.toMap(a -> a, a -> new ArrayList()));

    }


    @Test
    public void package_dependencies() throws ClassNotFoundException {
        SourceRoot sourceRoot = new SourceRoot(Paths.get("src/main/java"));
        final List<String> starting_package = Arrays.asList("org", "sfvl");

        Reflections reflections = new Reflections(String.join("/", starting_package), new SubTypesScanner(false));

        final List<Class> classesToAnalysis = reflections.getAllTypes().stream()
                .map(this::toClass)
                .filter(c -> c.getPackage() != DemoDocumentation.class.getPackage())
                .filter(c -> c.getPackage() != ApprovalsBase.class.getPackage())
                .filter(this::isTopLevelClass)
                .filter(c -> toSourceFile(c).isFile())
                .collect(Collectors.toList());

        final Map<String, List<String>> importsByClasses = classesToAnalysis.stream()
                .collect(Collectors.toMap(Class::getName,
                        clazz -> extractImports(sourceRoot, clazz, imp -> imp.startsWith(String.join(".", starting_package)))));

        GraphvizGenerator graphvizGenerator = new GraphvizGenerator()
                .rankDir(GraphvizGenerator.RankDir.TopDown);

        importsByClasses.entrySet().stream()
                .flatMap(classListEntry -> {
                    Stream<String> importsInClass = classListEntry.getValue().stream()
                            .filter(Objects::nonNull);

                    return importsInClass.map(e1 ->
                            new GraphvizGenerator.Link(
                                    toKeyGraphNode(classListEntry.getKey()),
                                    toKeyGraphNode(e1)
                            )
                    );
                }).forEach(graphvizGenerator::addLink);

        final Set<Package> packages = classesToAnalysis.stream().map(Class::getPackage).collect(Collectors.toSet());
        final Map<String, List<Package>> grouped_packages = packages.stream().collect(Collectors.groupingBy(p -> p.getName().split("\\.")[starting_package.size()]));

        String clusters = "";
        for (Map.Entry<String, List<Package>> packageEntry : grouped_packages.entrySet()) {
            clusters += "subgraph cluster_" + packageEntry.getKey() + "{\n";
            clusters += packageEntry.getValue().stream()
                    .map(Package::getName)
                    .map(name -> "    \"" + name + "\"")
                    .collect(Collectors.joining("\n"));
            clusters += "\n}\n";
        }

        final String graph = String.join("\n",
                "The graph below shows dependencies between packages in the project.",

                graphvizGenerator.generate("node [margin=0.1 fontcolor=black fontsize=16 width=0.5 shape=rect style=filled]\n" +
                        clusters,
                        "")
        );

        doc.write(graph);

    }

    private boolean isTopLevelClass(Class<?> c) {
        return !c.isAnonymousClass() && !c.isMemberClass() && !c.isLocalClass();
    }

    private List<String> extractImports(SourceRoot sourceRoot, Class clazz, Predicate<String> importFilter) {
        return extractImports(sourceRoot, clazz).stream().filter(importFilter).collect(Collectors.toList());
    }

    private List<String> extractImports(SourceRoot sourceRoot, Class clazz) {
        final Package aPackage = clazz.getPackage();
        final File file = toSourceFile(clazz);
        CompilationUnit cu = sourceRoot.parse(aPackage.getName(), file.getName());

        final List<String> imports = new ArrayList<>();
        cu.accept(importVisitor, imports);
        return imports;
    }

    private String toKeyGraphNode(String e) {
        return "\"" + e.replaceAll("\\.[A-Z].*", "") + "\"";
    }


}

