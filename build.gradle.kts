import com.adarshr.gradle.testlogger.theme.ThemeType
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.freefair.gradle.plugins.lombok.LombokExtension
import java.text.SimpleDateFormat
import java.util.*

plugins {
    java
    `maven-publish`
    id("com.adarshr.test-logger") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("com.gradle.plugin-publish") version "0.11.0"
    id("io.freefair.lombok") version "4.1.6" apply false
    id("net.linguica.maven-settings") version "0.5"
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
    val isReleaseBuild = ("true" == System.getProperty("release",""))

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

    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "io.freefair.lombok")

    extra["lombokVersion"] = extensions.findByType(LombokExtension::class)?.version?.get()
    tasks.named("generateLombokConfig") {
        // Don't generate lombok.config files ( https://docs.freefair.io/gradle-plugins/3.6.6/reference/#_lombok_config_handling )
        enabled = false
    }

    apply(plugin = "java-gradle-plugin")
    apply(plugin = "com.gradle.plugin-publish")

    tasks.withType<ShadowJar> {
        dependencies {
            exclude(dependency("org.slf4j:slf4j-api:${project.extra.get("slf4jVersion")}"))
        }
    }

    if ( project.name != "common") {
        apply(plugin = "signing")
        apply(plugin = "maven-publish")
        apply(plugin = "com.github.johnrengelman.shadow")
        apply(plugin = "net.linguica.maven-settings")

        /**
         * The configuration below has been created by reading the documentation at:
         * https://imperceptiblethoughts.com/shadow/plugins/
         *
         * Another great source of information is the configuration of the shadow plugin itself:
         * https://github.com/johnrengelman/shadow/blob/master/build.gradle
         */

        // make build task depend on shadowJar
        val build: DefaultTask by tasks
        val shadowJar = tasks["shadowJar"] as ShadowJar
        build.dependsOn(shadowJar)

        tasks.withType<ShadowJar> {
            // ensure that a single jar is built which is the shadowed one
            // Using non-deprecated archiveClassifier.set doesn't seem to work.
            @Suppress("DEPRECATION")
            classifier = ""
            // no need to add analyzer.init.gradle in the jar since it will never be used from inside the plugin itself
            exclude("analyzer-init.gradle")
           // Minimise the resulting uber-jars to ensure we don't have massive jars
           minimize() {
               // Sometimes minimisation takes away too much ... ensure we keep these.
               exclude(dependency("com.fasterxml.jackson.core:.*:.*"))
               exclude(dependency("org.commonjava.maven.ext:.*:.*"))
               exclude(dependency("org.commonjava.maven.atlas:.*:.*"))
               exclude(dependency("org.aeonbits.owner:.*:.*"))
               exclude(dependency("org.slf4j:.*:.*"))
               exclude(dependency("org.apache.maven:.*:.*"))
           }
            doFirst {
                manifest {
                    attributes["Built-By"] = System.getProperty("user.name")
                    attributes["Build-Timestamp"] = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date())
                    attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
                    attributes["Build-Jdk"] = System.getProperty("java.version") + " ; " + System.getProperty("java.vendor") + " ; " + System.getProperty("java.vm.version")
                    attributes["Build-OS"] = System.getProperty("os.name") + " ; " + System.getProperty("os.arch") + " ; " + System.getProperty("os.version")
                    attributes["Implementation-Version"] = "${project.version}"
                }
            }
        }

        val sourcesJar by tasks.registering(Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        val javadocJar by tasks.registering(Jar::class) {
            archiveClassifier.set("javadoc")
            from(tasks["javadoc"])
        }

        // configure publishing of the shadowJar
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("shadow") {
                    project.shadow.component(this)
                    artifact(sourcesJar.get())
                    artifact(javadocJar.get())

                    // we publish the init gradle file to make it easy for tools that use
                    // the plugin to set it up without having to create their own init gradle file
                    if (project.name == "analyzer") {
                        artifact("${sourceSets.main.get().output.resourcesDir}/analyzer-init.gradle") {
                            classifier = "init"
                            extension = "gradle"
                        }
                    }

                    pom {
                        name.set("Gradle Manipulation Extension")
                        description.set("A tool to work with ProjectNCL to manipulate Gradle builds.")
                        url.set("https://github.com/project-ncl/gradle-manipulator")
                        packaging = "jar"

                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("geoand")
                                name.set("Georgios Andrianakis")
                                email.set("gandrian@redhat.com")
                            }
                            developer {
                                id.set("rnc")
                                name.set("Nick Cross")
                                email.set("ncross@redhat.com")
                            }
                            developer {
                                id.set("TomasHofman")
                                name.set("Tomas Hofman")
                                email.set("thofman@redhat.com")
                            }
                            developer {
                                id.set("metacosm")
                                name.set("Chris Laprun")
                                email.set("claprun@redhat.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:http://github.com/project-ncl/gradle-manipulator.git")
                            developerConnection.set("scm:git:git@github.com:project-ncl/gradle-manipulator.git")
                            url.set("https://github.com/project-ncl/gradle-manipulator")
                        }
                    }
                }
            }
            repositories {
                if (isReleaseBuild) {
                    maven {
                        name = "sonatype-nexus-staging"
                        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                    }
                } else {
                    maven {
                        name = "sonatype-nexus-snapshots"
                        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
                    }
                }
            }
        }
    }


    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testlogger {
        // Some of our tests take a _long_ time ; remove spurious warnings.
        slowThreshold = 120000
        // Nicer looking theme than default.
        theme = ThemeType.MOCHA
    }

    if (project.name != "cli") {
        // Exclude logback from dependency tree.
        configurations {
            "compile" {
                exclude(group = "ch.qos.logback", module = "logback-classic")
            }
            "compile" {
                exclude(group = "ch.qos.logback", module = "logback-core")
            }
        }
    }
}
