
description = "Kotlin scripting support utilities"

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(kotlinStdlib())
    api(project(":kotlin-script-runtime"))
    api(project(":kotlin-scripting-jvm"))
    api(commonDependency("org.jetbrains.intellij.deps", "trove4j"))
    compileOnly(project(":compiler:cli"))
    compileOnly(project(":daemon-common"))
    compileOnly(project(":kotlin-scripting-compiler"))
    api(project(":kotlin-daemon-client"))
    testCompileOnly(project(":compiler:cli"))
    testApi(project(":kotlin-test:kotlin-test-junit"))
    testImplementation(libs.junit4)
    testApi(project(":kotlin-scripting-compiler"))
    testRuntimeOnly(project(":kotlin-compiler"))
    testApi(intellijCore())
}

optInToExperimentalCompilerApi()

configurations.all {
    resolutionStrategy {
        force(libs.junit4)
    }
}

projectTest {
    workingDir = rootDir
}

runtimeJar()
sourcesJar()
javadocJar()
