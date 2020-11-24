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
    implementation(gradleApi())
    testImplementation(gradleTestKit())
    testImplementation("org.commonjava.maven.ext:pom-manipulation-common:${project.extra.get("pmeVersion")}")

    testImplementation("junit:junit:${project.extra.get("junitVersion")}")
    testImplementation("com.github.stefanbirkner:system-rules:${project.extra.get("systemRulesVersion")}")
    testImplementation("org.assertj:assertj-core:${project.extra.get("assertjVersion")}")

    testRuntime("commons-io:commons-io:2.6")
    implementation("org.commonjava.maven.ext:pom-manipulation-annotation:${project.extra.get("pmeVersion")}")
    implementation("org.commonjava.maven.ext:pom-manipulation-common:${project.extra.get("pmeVersion")}")
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {

    java.srcDir("src/functTest/java")
    resources.srcDir("src/functTest/resources")

    // Force the addition of the plugin-under-test-metadata.properties else there are problems under Gradle >= 6.
    // runtimeClasspath += layout.files(project.buildDir.toString() + "/pluginUnderTestMetadata")

//    compileClasspath += sourceSets.main.get().output + configurations.testRuntime
//    runtimeClasspath += sourceSets.main.get().output + compileClasspath

    compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath
    runtimeClasspath += output + compileClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

configurations.getByName("functionalTestImplementation").apply {
    extendsFrom(configurations.getByName("testImplementation"))
}
configurations.getByName("functionalTestRuntime").apply {
    extendsFrom(configurations.getByName("testRuntime"))
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
