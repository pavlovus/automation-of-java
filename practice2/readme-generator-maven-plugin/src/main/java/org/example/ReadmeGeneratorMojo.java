package org.example;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "generate-readme")
public class ReadmeGeneratorMojo extends AbstractMojo {
    @Component
    private MavenProject project;

    @Parameter(defaultValue = "${project.basedir}", property = "readme.baseDirectory")
    private File baseDirectory;

    @Parameter(defaultValue = "${project.build.sourceDirectory}", property = "readme.sourceDirectory")
    private File sourceDirectory;

    @Parameter(defaultValue = "README.md", property = "readme.outputFileName")
    private String outputFileName;

    @Parameter(defaultValue = "true", property = "readme.includeStructure")
    private boolean includeStructure;

    @Parameter(defaultValue = "true", property = "readme.includeClasses")
    private boolean includeClasses;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("README Generator started");

        File outputFile = new File(baseDirectory, outputFileName);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            writeProjectInfo(writer);

            if (includeStructure) writeProjectStructure(writer);

            if (includeClasses && sourceDirectory.exists()) writeClassList(writer);

            writeBuildSection(writer);

        } catch (IOException e) {
            throw new MojoExecutionException("Mistake while generating README: " + outputFile, e);
        }

        getLog().info("README generated: " + outputFile.getAbsolutePath());
    }

    private void writeProjectInfo(BufferedWriter writer) throws IOException {
        writer.write("# " + project.getName());
        writer.newLine();
        writer.newLine();

        if (project.getDescription() != null) {
            writer.write(project.getDescription());
            writer.newLine();
            writer.newLine();
        }

        writer.write("## Project info: ");
        writer.newLine();
        writer.newLine();
        writer.write("| | |");
        writer.newLine();
        writer.write("|---|---|");
        writer.newLine();
        writer.write("| **Group ID** | `" + project.getGroupId() + "` |");
        writer.newLine();
        writer.write("| **Artifact ID** | `" + project.getArtifactId() + "` |");
        writer.newLine();
        writer.write("| **Версія** | `" + project.getVersion() + "` |");
        writer.newLine();
        writer.write("| **Packaging** | `" + project.getPackaging() + "` |");
        writer.newLine();
        writer.newLine();
    }

    private void writeProjectStructure(BufferedWriter writer) throws IOException {
        writer.write("## Структура проєкту");
        writer.newLine();
        writer.newLine();
        writer.write("```");
        writer.newLine();
        writer.write(project.getArtifactId() + "/");
        writer.newLine();
        writer.write("├── src/");
        writer.newLine();
        writer.write("│   ├── main/");
        writer.newLine();
        writer.write("│   │   └── java/");
        writer.newLine();

        if (sourceDirectory.exists()) {
            List<String> packages = getPackages();
            for (String pkg : packages) {
                writer.write("│   │       └── " + pkg + "/");
                writer.newLine();
            }
        }

        writer.write("│   └── test/");
        writer.newLine();
        writer.write("│       └── java/");
        writer.newLine();
        writer.write("└── pom.xml");
        writer.newLine();
        writer.write("```");
        writer.newLine();
        writer.newLine();
    }

    private void writeClassList(BufferedWriter writer) throws IOException {
        List<File> javaFiles = collectJavaFiles(sourceDirectory);

        if (javaFiles.isEmpty()) return;

        writer.write("## Classes: ");
        writer.newLine();
        writer.newLine();

        Map<String, List<File>> byPackage = new LinkedHashMap<>();
        for (File file : javaFiles) {
            String pkg = getPackageName(file);
            byPackage.computeIfAbsent(pkg, k -> new ArrayList<>()).add(file);
        }

        for (Map.Entry<String, List<File>> entry : byPackage.entrySet()) {
            writer.write("### `" + entry.getKey() + "`");
            writer.newLine();
            writer.newLine();

            for (File file : entry.getValue()) {
                String className = file.getName().replace(".java", "");
                String type = detectType(file);
                writer.write("- **" + className + "** — " + type);
                writer.newLine();
            }
            writer.newLine();
        }
    }

    private void writeBuildSection(BufferedWriter writer) throws IOException {
        writer.write("## Build");
        writer.newLine();
        writer.newLine();
        writer.write("```bash");
        writer.newLine();
        writer.write("# Compile");
        writer.newLine();
        writer.write("mvn compile");
        writer.newLine();
        writer.newLine();
        writer.write("# Run tests");
        writer.newLine();
        writer.write("mvn test");
        writer.newLine();
        writer.newLine();
        writer.write("# Package");
        writer.newLine();
        writer.write("mvn package");
        writer.newLine();
        writer.write("```");
        writer.newLine();
    }

    private List<File> collectJavaFiles(File directory) throws IOException {
        try (var stream = Files.walk(directory.toPath())) {
            return stream.filter(p -> p.toString().endsWith(".java")).map(Path::toFile)
                    .sorted(Comparator.comparing(File::getAbsolutePath)).collect(Collectors.toList());
        }
    }

    private List<String> getPackages() throws IOException {
        try (var stream = Files.walk(sourceDirectory.toPath())) {
            return stream.filter(Files::isDirectory).filter(p -> !p.equals(sourceDirectory.toPath()))
                    .map(p -> sourceDirectory.toPath().relativize(p).toString().replace(File.separator, "."))
                    .sorted().collect(Collectors.toList());
        }
    }

    private String getPackageName(File javaFile) {
        try {
            return Files.readAllLines(javaFile.toPath(), StandardCharsets.UTF_8).stream()
                    .filter(line -> line.trim().startsWith("package ")).findFirst()
                    .map(line -> line.trim().replace("package ", "").replace(";", ""))
                    .orElse("default");
        } catch (IOException e) { return "unknown";}
    }

    private String detectType(File javaFile) {
        try {
            String content = Files.readString(javaFile.toPath(), StandardCharsets.UTF_8);
            if (content.contains("interface "))  return "interface";
            if (content.contains("enum "))       return "enum";
            if (content.contains("@interface ")) return "annotation";
            if (content.contains("abstract "))   return "abstract class";
            return "клас";
        } catch (IOException e) { return "Class"; }
    }
}