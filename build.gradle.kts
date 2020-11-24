
plugins {
    java
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}

allprojects {
    repositories {
        maven {
            url = uri("https://maven-central-eu.storage-download.googleapis.com/maven2")
        }
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://repo.gradle.org/gradle/libs-releases-local/")
        }
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            url = uri("http://maven.repository.redhat.com/techpreview/all")
        }
    }
}

subprojects {
    extra["gradleVersion"] = "5.6.4"
    extra["atlasVersion"] = "0.17.2"
    extra["assertjVersion"] = "3.12.2"
    extra["bytemanVersion"] = "4.0.13"
    extra["commonsVersion"] = "2.6"
    extra["commonsBeanVersion"] = "1.9.4"
    extra["jacksonVersion"] = "2.11.2"
    extra["junitVersion"] = "4.13.1"
    extra["groovyVersion"] = "3.0.5"
    extra["logbackVersion"] = "1.2.3"
    extra["mavenVersion"] = "3.5.0"
    extra["ownerVersion"] = "1.0.12"
    extra["pmeVersion"] = "4.1"
    extra["slf4jVersion"] = "1.7.30"
    extra["systemRulesVersion"] = "1.19.0"

    apply(plugin = "java-gradle-plugin")
    apply(plugin = "com.gradle.plugin-publish")
    apply(plugin = "maven-publish")

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(tasks["javadoc"])
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
