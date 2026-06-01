plugins {
    `java-gradle-plugin`
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("projectReportPlugin") {
            id = "org.example.project-report-gradle-plugin"
            implementationClass = "org.example.ProjectReportPlugin"
        }
    }
}