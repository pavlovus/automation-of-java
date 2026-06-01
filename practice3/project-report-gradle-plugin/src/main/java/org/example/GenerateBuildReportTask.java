package org.example;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class GenerateBuildReportTask extends DefaultTask {

    @InputFile
    public abstract RegularFileProperty getAnalysisFile();

    @TaskAction
    public void execute() throws IOException {
        File buildDir = new File(getProject().getProjectDir(), "build");
        buildDir.mkdirs();
        File outputFile = new File(buildDir, "build-report.txt");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writeBuildInfo(writer);
            writeCodeStats(writer);
            writeDependencies(writer);
        }

        System.out.println("[GenerateBuildReport] Report saved to: " + outputFile.getAbsolutePath());
    }

    private void writeBuildInfo(BufferedWriter writer) throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        writer.write("=== Build Report ===");
        writer.newLine();
        writer.write("Generated at:    " + timestamp);
        writer.newLine();
        writer.newLine();

        writer.write("--- Project ---");
        writer.newLine();
        writer.write("Name:            " + getProject().getName());
        writer.newLine();
        writer.write("Group:           " + getProject().getGroup());
        writer.newLine();
        writer.write("Version:         " + getProject().getVersion());
        writer.newLine();
        writer.newLine();

        writer.write("--- Environment ---");
        writer.newLine();
        writer.write("Java version:    " + System.getProperty("java.version"));
        writer.newLine();
        writer.write("Java vendor:     " + System.getProperty("java.vendor"));
        writer.newLine();
        writer.write("Gradle version:  " + getProject().getGradle().getGradleVersion());
        writer.newLine();
        writer.write("OS:              " + System.getProperty("os.name")
                + " " + System.getProperty("os.version"));
        writer.newLine();
        writer.newLine();
    }

    private void writeCodeStats(BufferedWriter writer) throws IOException {
        File analysisFile = getAnalysisFile().get().getAsFile();
        for (String line : Files.readAllLines(analysisFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write(line);
            writer.newLine();
        }
        writer.newLine();
    }

    private void writeDependencies(BufferedWriter writer) throws IOException {
        writer.write("--- Dependencies ---");
        writer.newLine();

        Set<String> listed = new LinkedHashSet<>();

        for (var config : getProject().getConfigurations()) {
            if (!config.isCanBeResolved()) continue;
            try {
                for (ResolvedArtifact artifact : config.getResolvedConfiguration().getResolvedArtifacts()) {
                    var id = artifact.getModuleVersion().getId();
                    listed.add(id.getGroup() + ":" + artifact.getName() + ":" + id.getVersion());
                }
            } catch (Exception ignored) {
                // skip configurations that cannot be resolved at this stage
            }
        }

        if (listed.isEmpty()) {
            writer.write("No dependencies found");
            writer.newLine();
        } else {
            for (String dep : listed) {
                writer.write("  - " + dep);
                writer.newLine();
            }
        }
    }
}