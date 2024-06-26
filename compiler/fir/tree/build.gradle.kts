import org.jetbrains.kotlin.ideaExt.idea

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":compiler:frontend.common"))
    api(project(":core:compiler.common"))
    api(project(":compiler:fir:cones"))

    if (kotlinBuildProperties.isInIdeaSync) {
        compileOnly(project("tree-generator")) // Provided, so that IDEA can recognize references to this module in KDoc.
    }

    // Necessary only to store bound PsiElement inside FirElement
    compileOnly(intellijCore())
}

sourceSets {
    "main" {
        projectDefault()
        generatedDir()
    }
}

val generatorClasspath by configurations.creating

dependencies {
    generatorClasspath(project("tree-generator"))
}

val generationRoot = projectDir.resolve("gen")

val generateTree by tasks.registering(NoDebugJavaExec::class) {

    val generatorRoot = "$projectDir/tree-generator/src/"

    val generatorConfigurationFiles = fileTree(generatorRoot) {
        include("**/*.kt")
    }

    inputs.files(generatorConfigurationFiles)
    outputs.dirs(generationRoot)

    args(generationRoot)
    workingDir = rootDir
    classpath = generatorClasspath
    mainClass.set("org.jetbrains.kotlin.fir.tree.generator.MainKt")
    systemProperties["line.separator"] = "\n"
}

tasks.named("compileKotlin") {
    dependsOn(generateTree)
}

if (kotlinBuildProperties.isInJpsBuildIdeaSync) {
    apply(plugin = "idea")
    idea {
        this.module.generatedSourceDirs.add(generationRoot)
    }
}
