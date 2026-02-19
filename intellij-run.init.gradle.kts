// Mimic IntelliJ's "Run using Gradle" Application task for a main() method.
// Usage:
//   .\gradlew.bat -I intellij-run.init.gradle.kts ":com.campasian.CampasianApplication.main()" --stacktrace
//
// Keep this file in-repo so the run can be reproduced outside the IDE.

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.the

gradle.allprojects {
    // Register once the Java plugin is applied so `sourceSets` exists.
    plugins.withId("java") {
        tasks.register("com.campasian.CampasianApplication.main()", JavaExec::class.java) {
            group = "application"
            description = "IntelliJ-like Gradle run task for com.campasian.CampasianApplication"

            // Try to run as a named module if the project is modular; Gradle will fall back when needed.
            mainModule.set("com.campasian")
            mainClass.set("com.campasian.CampasianApplication")

            // `plugins.withId(...)` can fire before the Java plugin has finished wiring extensions.
            // Resolve `sourceSets` lazily at execution time.
            doFirst {
                val sourceSets = project.the<SourceSetContainer>()
                (this as JavaExec).classpath = sourceSets.getByName("main").runtimeClasspath
            }
        }
    }
}
