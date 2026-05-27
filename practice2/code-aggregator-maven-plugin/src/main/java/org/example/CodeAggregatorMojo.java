package org.example;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo( name = "aggregate")
public class CodeAggregatorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.build.sourceDirectory}", property = "aggregate.sourceDirectory")
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.directory}", property = "aggregate.outputDirectory")
    private File outputDirectory;

    @Parameter(defaultValue = "merged-sources.java", property = "aggregate.outputFileName")
    private String outputFileName;

    @Parameter(defaultValue = "true", property = "aggregate.includeSeparators")
    private boolean includeSeparators;

    @Parameter(defaultValue = "true", property = "aggregate.includePackageDeclarations")
    private boolean includePackageDeclarations;

    @Parameter(defaultValue = "true", property = "aggregate.includeImports")
    private boolean includeImports;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("  Code Aggregate Plugin started");

        if (!sourceDirectory.exists()) {
            getLog().warn("No source code directory: " + sourceDirectory.getAbsolutePath());
            return;
        }

        getLog().info("Source: " + sourceDirectory.getAbsolutePath());

        List<File> javaFiles = collectJavaFiles(sourceDirectory);

        if (javaFiles.isEmpty()) {
            getLog().warn("No .java files found");
            return;
        }

        getLog().info("Found " + javaFiles.size() + " java files");

        if (!outputDirectory.exists()) { outputDirectory.mkdirs(); }

        File outputFile = new File(outputDirectory, outputFileName);
        getLog().info("Result file: " + outputFile.getAbsolutePath());

        mergeFiles(javaFiles, outputFile);

        getLog().info(" Code Aggregate Plugin finished");
    }

    private List<File> collectJavaFiles(File directory) throws MojoExecutionException {
        try (Stream<Path> stream = Files.walk(directory.toPath())) {
            return stream.filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) { throw new MojoExecutionException("Mistake while collecting files at: " + directory);}
    }

    private void mergeFiles(List<File> javaFiles, File outputFile) throws MojoExecutionException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            writeFileHeader(writer);

            int processedCount = 0;
            for (File javaFile : javaFiles) {
                try {
                    processedCount++;
                    if (includeSeparators) writeFileSeparator(writer, javaFile, processedCount, javaFiles.size());

                    writeFileContent(writer, javaFile);
                    writer.newLine();
                } catch (IOException e) {
                    getLog().error("Mistake while writing the file: " + javaFile.getName());
                }
            }

            writeFileFooter(writer, processedCount);
        } catch (IOException e) { throw new MojoExecutionException("Mistake while writing to file: " + outputFile); }
    }

    private void writeFileHeader(BufferedWriter writer) throws IOException {
        writer.write("// AGGREGATED CODE\n");
        writer.write("// PROJECT: " + project.getName() + "\n");
        writer.write("// VERSION: " + project.getVersion() + "\n");
    }

    private void writeFileSeparator(BufferedWriter writer, File javaFile, int current, int total) throws IOException {
        String relativePath = sourceDirectory.toPath().relativize(javaFile.toPath()).toString();
        writer.newLine();
        writer.write("// [" + current + "/" + total + "] " + relativePath + "\n");
        writer.newLine();
    }

    private void writeFileContent(BufferedWriter writer, File javaFile) throws IOException {
        List<String> lines = Files.readAllLines(javaFile.toPath(), StandardCharsets.UTF_8);

        for (String line : lines) {
            if (!includePackageDeclarations && line.trim().startsWith("package ")) continue;
            if (!includeImports && line.trim().startsWith("import ")) continue;
            writer.write(line);
            writer.newLine();
        }
    }

    private void writeFileFooter(BufferedWriter writer, int totalFiles) throws IOException {
        writer.newLine();
        writer.write("// AGGREGATED " + totalFiles + " FILES TOTALLY");
        writer.newLine();
    }
}