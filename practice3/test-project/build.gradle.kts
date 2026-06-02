plugins {
    id("org.example.project-report-gradle-plugin")
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.register("cleanReports") {
    group = "reporting"
    description = "Deletes all generated report files: README.md, build-report.txt, code-analysis.txt"

    doLast {
        listOf(
            file("README.md"),
            file("build/build-report.txt"),
            file("build/code-analysis.txt")
        ).forEach { f ->
            if (f.exists()) {
                f.delete()
                println("Deleted: ${f.name}")
            } else {
                println("Not found: ${f.name}")
            }
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}