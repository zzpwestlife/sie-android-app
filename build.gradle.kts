plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.hiltAndroid) apply false
    alias(libs.plugins.ksp) apply false
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    group = "verification"
    description = "Generate aggregated Jacoco coverage report"

    dependsOn(subprojects.map { it.tasks.withType<Test>() })

    reports {
        xml.required.set(true)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoTestReport/html"))
    }

    val sourceDirs = subprojects.flatMap {
        listOf(
            "${it.projectDir}/src/main/java",
            "${it.projectDir}/src/main/kotlin"
        )
    }
    sourceDirectories.setFrom(files(sourceDirs))

    val classDirs = subprojects.flatMap {
        listOf(
            "${it.layout.buildDirectory.get()}/tmp/kotlin-classes/debug",
            "${it.layout.buildDirectory.get()}/intermediates/javac/debug/classes"
        )
    }
    classDirectories.setFrom(files(classDirs).asFileTree.matching {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*"
        )
    })

    executionData.setFrom(
        files(subprojects.flatMap {
            listOf(
                "${it.layout.buildDirectory.get()}/jacoco/testDebugUnitTest.exec",
                "${it.layout.buildDirectory.get()}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
            )
        }).filter { it.exists() }
    )
}
