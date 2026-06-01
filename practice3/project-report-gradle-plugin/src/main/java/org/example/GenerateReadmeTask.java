package org.example;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GenerateReadmeTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getAnalysisFile();

    @TaskAction
    public void execute() throws IOException {
        File projectDir = getProject().getProjectDir();
        File sourceDir  = new File(projectDir, "src/main/java");
        File outputFile = new File(projectDir, "README.md");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            writeProjectInfo(writer);
            writeProjectStructure(writer, sourceDir);
            if (sourceDir.exists()) {
                writeClassList(writer, sourceDir);
            }
            writeCodeStats(writer);
            writeBuildSection(writer);
        }

        System.out.println("[GenerateReadme] README.md generated: " + outputFile.getAbsolutePath());
    }

    private void writeProjectInfo(BufferedWriter writer) throws IOException {
        writer.write("# " + getProject().getName());
        writer.newLine();
        writer.newLine();

        String description = getProject().getDescription();
        if (description != null && !description.isBlank()) {
            writer.write(description);
            writer.newLine();
            writer.newLine();
        }

        writer.write("## Project Info");
        writer.newLine();
        writer.newLine();
        writer.write("| | |");
        writer.newLine();
        writer.write("|---|---|");
        writer.newLine();
        writer.write("| **Group** | `" + getProject().getGroup() + "` |");
        writer.newLine();
        writer.write("| **Version** | `" + getProject().getVersion() + "` |");
        writer.newLine();
        writer.write("| **Java** | `" + System.getProperty("java.version") + "` |");
        writer.newLine();
        writer.write("| **Gradle** | `" + getProject().getGradle().getGradleVersion() + "` |");
        writer.newLine();
        writer.newLine();
    }

    private void writeProjectStructure(BufferedWriter writer, File sourceDir) throws IOException {
        writer.write("## Project Structure");
        writer.newLine();
        writer.newLine();
        writer.write("```");
        writer.newLine();
        writer.write(getProject().getName() + "/");
        writer.newLine();
        writer.write("├── src/");
        writer.newLine();
        writer.write("│   ├── main/");
        writer.newLine();
        writer.write("│   │   └── java/");
        writer.newLine();

        if (sourceDir.exists()) {
            for (String pkg : getPackages(sourceDir)) {
                writer.write("│   │       └── " + pkg + "/");
                writer.newLine();
            }
        }

        writer.write("│   └── test/");
        writer.newLine();
        writer.write("│       └── java/");
        writer.newLine();
        writer.write("└── build.gradle.kts");
        writer.newLine();
        writer.write("```");
        writer.newLine();
        writer.newLine();
    }

    private void writeClassList(BufferedWriter writer, File sourceDir) throws IOException {
        List<File> javaFiles = collectJavaFiles(sourceDir);
        if (javaFiles.isEmpty()) return;

        writer.write("## Classes");
        writer.newLine();
        writer.newLine();

        Map<String, List<File>> byPackage = new LinkedHashMap<>();
        for (File file : javaFiles) {
            String pkg = getPackageName(file);
            byPackage.computeIfAbsent(pkg, _ -> new ArrayList<>()).add(file);
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

    private void writeCodeStats(BufferedWriter writer) throws IOException {
        writer.write("## Code Statistics");
        writer.newLine();
        writer.newLine();
        writer.write("```");
        writer.newLine();
        File analysisFile = getAnalysisFile().get().getAsFile();
        for (String line : Files.readAllLines(analysisFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(line);
            writer.newLine();
        }
        writer.write("```");
        writer.newLine();
        writer.newLine();
    }

    private void writeBuildSection(BufferedWriter writer) throws IOException {
        writer.write("## Build");
        writer.newLine();
        writer.newLine();
        writer.write("```bash");
        writer.newLine();
        writer.write("# Build the project");
        writer.newLine();
        writer.write("./gradlew build");
        writer.newLine();
        writer.newLine();
        writer.write("# Run tests");
        writer.newLine();
        writer.write("./gradlew test");
        writer.newLine();
        writer.newLine();
        writer.write("# Analyze source code");
        writer.newLine();
        writer.write("./gradlew analyzeCode");
        writer.newLine();
        writer.newLine();
        writer.write("# Generate this README");
        writer.newLine();
        writer.write("./gradlew generateReadme");
        writer.newLine();
        writer.newLine();
        writer.write("# Generate build report");
        writer.newLine();
        writer.write("./gradlew generateBuildReport");
        writer.newLine();
        writer.write("```");
        writer.newLine();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private List<File> collectJavaFiles(File directory) throws IOException {
        try (var stream = Files.walk(directory.toPath())) {
            return stream
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::getAbsolutePath))
                    .collect(Collectors.toList());
        }
    }

    private List<String> getPackages(File sourceDir) throws IOException {
        try (var stream = Files.walk(sourceDir.toPath())) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(p -> !p.equals(sourceDir.toPath()))
                    .map(p -> sourceDir.toPath().relativize(p).toString().replace(File.separator, "."))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }

    private String getPackageName(File javaFile) {
        try {
            return Files.readAllLines(javaFile.toPath(), StandardCharsets.UTF_8).stream()
                    .filter(line -> line.trim().startsWith("package "))
                    .findFirst()
                    .map(line -> line.trim().replace("package ", "").replace(";", ""))
                    .orElse("default");
        } catch (IOException e) {
            return "unknown";
        }
    }

    private String detectType(File javaFile) {
        try {
            String content = Files.readString(javaFile.toPath(), StandardCharsets.UTF_8);
            if (content.contains("@interface ")) return "annotation";
            if (content.contains("interface "))  return "interface";
            if (content.contains("enum "))       return "enum";
            if (content.contains("abstract "))   return "abstract class";
            return "class";
        } catch (IOException e) {
            return "class";
        }
    }
}