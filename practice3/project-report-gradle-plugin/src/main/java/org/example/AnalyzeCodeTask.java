package org.example;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AnalyzeCodeTask extends DefaultTask {

    @OutputFile
    public abstract RegularFileProperty getAnalysisFile();

    @TaskAction
    public void execute() throws IOException {
        File sourceDir = new File(getProject().getProjectDir(), "src/main/java");

        if (!sourceDir.exists()) {
            System.out.println("[AnalyzeCode] Source directory not found: " + sourceDir.getAbsolutePath());
            return;
        }

        List<File> javaFiles = collectJavaFiles(sourceDir);
        int totalLines = 0;
        int totalMethods = 0;
        Map<String, Integer> typeCount = new LinkedHashMap<>();

        for (File file : javaFiles) {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            totalLines += lines.size();
            totalMethods += countMethods(lines);
            String type = detectType(file);
            typeCount.merge(type, 1, Integer::sum);
        }

        File outputFile = getAnalysisFile().get().getAsFile();
        outputFile.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writer.write("=== Code Analysis ===");
            writer.newLine();
            writer.write("Generated at: " + new Date());
            writer.newLine();
            writer.newLine();
            writer.write("Total Java files:    " + javaFiles.size());
            writer.newLine();
            writer.write("Total lines of code: " + totalLines);
            writer.newLine();
            writer.write("Total methods:       " + totalMethods);
            writer.newLine();
            writer.newLine();
            writer.write("--- By type ---");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
        }

        System.out.println("[AnalyzeCode] Java files:    " + javaFiles.size());
        System.out.println("[AnalyzeCode] Lines of code: " + totalLines);
        System.out.println("[AnalyzeCode] Methods:       " + totalMethods);
        System.out.println("[AnalyzeCode] Saved to: " + outputFile.getAbsolutePath());
    }

    private List<File> collectJavaFiles(File directory) throws IOException {
        try (var stream = Files.walk(directory.toPath())) {
            return stream
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::getAbsolutePath))
                    .collect(Collectors.toList());
        }
    }

    private int countMethods(List<String> lines) {
        int count = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if ((trimmed.startsWith("public ") || trimmed.startsWith("private ") || trimmed.startsWith("protected "))
                    && trimmed.contains("(")
                    && trimmed.contains(")")
                    && !trimmed.contains("class ")
                    && !trimmed.contains("interface ")
                    && !trimmed.startsWith("//")) {
                count++;
            }
        }
        return count;
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