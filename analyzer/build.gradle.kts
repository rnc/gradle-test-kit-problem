group = "org.jboss.gm"

pluginBundle {
    website = "https://project-ncl.github.io/gradle-manipulator/"
    vcsUrl = "https://github.com/project-ncl/gradle-manipulator/tree/master/analyzer"
    tags = listOf("versions", "alignment")
}

gradlePlugin {
    plugins {
        create("alignmentPlugin") {
            description = "Plugin that that generates alignment metadata at \${project.rootDir}/manipulation.json"
            id = "org.jboss.gm.analyzer"
            implementationClass = "org.jboss.gm.analyzer.alignment.AlignmentPlugin"
            displayName = "GME Manipulation Plugin"
        }
    }
    // Disable creation of the plugin marker pom.
    this.isAutomatedPublishing = false
}

dependencies {
    compile(localGroovy())
    compile(gradleApi())
    compile("org.commonjava.maven.ext:pom-manipulation-common:${project.extra.get("pmeVersion")}")
    testCompile(gradleTestKit())

    testCompile("junit:junit:${project.extra.get("junitVersion")}")
    testCompile("com.github.stefanbirkner:system-rules:${project.extra.get("systemRulesVersion")}")
    testCompile("org.assertj:assertj-core:${project.extra.get("assertjVersion")}")
    testCompile (files ("${System.getProperty("java.home")}/../lib/tools.jar") )
    testCompile(gradleKotlinDsl())
}

tasks.test {
    systemProperties["jdk.attach.allowAttachSelf"] = "true"
}

// separate source set and task for functional tests

sourceSets.create("functionalTest") {
    java.srcDir("src/functTest/java")
    resources.srcDir("src/functTest/resources")
    // Force the addition of the plugin-under-test-metadata.properties else there are problems under Gradle >= 6.
    runtimeClasspath += layout.files(project.buildDir.toString() + "/pluginUnderTestMetadata")
    compileClasspath += sourceSets["main"].output + configurations.testRuntime
    runtimeClasspath += output + compileClasspath
}

val functionalTest = task<Test>("functionalTest") {
    description = "Runs functional tests"
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
    mustRunAfter(tasks["test"])
    //this will be used in the Wiremock tests - the port needs to match what Wiremock is setup to use
    environment("DA_ENDPOINT_URL", "http://localhost:8089/da/rest/v-1")
    systemProperties["jdk.attach.allowAttachSelf"] = "true"
}

val testJar by tasks.registering(Jar::class) {
    mustRunAfter(tasks["functionalTest"])
    archiveClassifier.set("tests")
    from(sourceSets["functionalTest"].output)
    from(sourceSets.test.get().output)
}

// Publish test source jar so it can be reused by manipulator-groovy-examples.
val testSourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("test-sources")
    from(sourceSets.test.get().allSource)
    from(sourceSets.getByName("functionalTest").java.srcDirs)
}

tasks.check { dependsOn(functionalTest) }
