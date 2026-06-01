package org.example;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectReportPlugin implements Plugin<Project> {
    private static final String ANALYSIS_FILE_PATH = "reports/analysis.txt";

    @Override
    public void apply(@NotNull Project project) {
        registerAnalyzeCodeTask(project);
        registerGenerateReadmeTask(project);
        registerGenerateBuildReportTask(project);
    }

    private void registerAnalyzeCodeTask(Project project) {
        project.getTasks().register("analyzeCode", AnalyzeCodeTask.class, task -> {
            task.getAnalysisFile().set(project.getLayout().getBuildDirectory().file(ANALYSIS_FILE_PATH));
        });
    }

    private void registerGenerateReadmeTask(Project project) {
        project.getTasks().register("generateReadme", GenerateReadmeTask.class, task -> {
            task.dependsOn("analyzeCode");
            task.getAnalysisFile().set(project.getLayout().getBuildDirectory().file(ANALYSIS_FILE_PATH));
        });
    }

    private void registerGenerateBuildReportTask(Project project) {
        project.getTasks().register("generateBuildReport", GenerateBuildReportTask.class, task -> {
            task.dependsOn("analyzeCode");
            task.getAnalysisFile().set(project.getLayout().getBuildDirectory().file(ANALYSIS_FILE_PATH));
        });
    }
}