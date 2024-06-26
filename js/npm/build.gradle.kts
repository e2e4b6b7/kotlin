import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "5.0.0"
  base
}

description = "Node utils"

node {
    download.set(true)
}

val deployDir = "$buildDir/deploy_to_npm"
val templateDir = "$projectDir/templates"
val kotlincDir = "$projectDir/../../dist/kotlinc"

fun getProperty(name: String, default: String = "") = findProperty(name)?.toString() ?: default

val deployVersion = getProperty("kotlin.deploy.version", "0.0.0")
val deployTag = getProperty("kotlin.deploy.tag", "dev")
val authToken = getProperty("kotlin.npmjs.auth.token")
val dryRun = getProperty("dryRun", "false") // Pack instead of publish

fun Project.createCopyTemplateTask(templateName: String): Copy {
  return task<Copy>("copy-$templateName-template") {
      from("$templateDir/$templateName")
      into("$deployDir/$templateName")

      expand(hashMapOf("version" to deployVersion))
  }
}

fun Project.createPublishToNpmTask(templateName: String): NpmTask {
  return task<NpmTask>("publish-$templateName-to-npm") {
    val deployDir = File("$deployDir/$templateName")
    workingDir.set(deployDir)

    val deployArgs = listOf("publish", "--//registry.npmjs.org/:_authToken=$authToken", "--tag=$deployTag")
    if (dryRun == "true") {
      println("$deployDir \$ npm arguments: $deployArgs");
      args.set(listOf("pack"))
    }
    else {
      args.set(deployArgs)
    }
  }
}

fun sequential(first: Task, vararg tasks: Task): Task {
  tasks.fold(first) { previousTask, currentTask ->
    currentTask.dependsOn(previousTask)
  }
  return tasks.last()
}

val publishKotlinCompiler = sequential(
  createCopyTemplateTask("kotlin-compiler"),
  task<Copy>("copy-kotlin-compiler") {
    from(kotlincDir)
    into("$deployDir/kotlin-compiler")
  },
  task<Exec>("chmod-kotlinc-bin") {
    commandLine = listOf("chmod", "-R", "ugo+rx", "$deployDir/kotlin-compiler/bin")
  },
  createPublishToNpmTask("kotlin-compiler")
)

task("publishAll") {
    dependsOn(publishKotlinCompiler)
}
