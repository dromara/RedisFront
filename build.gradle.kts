@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.lang.Boolean

plugins {
    `java-library`
}

val releaseVersion = "1.0"
val developmentVersion = "1.0-SNAPSHOT"

version = if (Boolean.getBoolean("release")) releaseVersion else developmentVersion

val flatlafVersion = "2.4"
val hutoolVersion = "5.8.3"
val fifesoftVersion = "3.2.0"
val derbyVersion = "10.15.2.0"
val lettuceVersion = "6.2.0.RELEASE"
val logbackVersion = "1.2.11"
val jfreechartVersion = "1.5.3"

repositories {
    mavenLocal()
    mavenCentral()
}

if (JavaVersion.current() < JavaVersion.VERSION_17)
    throw RuntimeException("compile required Java ${JavaVersion.VERSION_17}, current Java ${JavaVersion.current()}")

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

println()
println("-------------------------------------------------------------------------------")
println("$name Version: $version")
println("Project Path:  $projectDir")
println("Java Version:  ${System.getProperty("java.version")}")
println("Gradle Version: ${gradle.gradleVersion} at ${gradle.gradleHomeDir}")
println("Current Date:  ${LocalDateTime.now().format(dateTimeFormatter)}")
println("-------------------------------------------------------------------------------")
println()

dependencies {

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    implementation("io.lettuce:lettuce-core:${lettuceVersion}")
    implementation("io.netty:netty-common:4.1.79.Final")
    implementation("com.formdev:flatlaf:${flatlafVersion}")
    implementation("com.jfree:jfreechart:${jfreechartVersion}")
    implementation("com.formdev:flatlaf-swingx:${flatlafVersion}")
    implementation("com.formdev:flatlaf-intellij-themes:${flatlafVersion}")
    implementation("com.formdev:flatlaf-extras:${flatlafVersion}")
    implementation("cn.hutool:hutool-extra:${hutoolVersion}")
    implementation("cn.hutool:hutool-json:${hutoolVersion}")
    implementation("org.apache.derby:derby:${derbyVersion}")
    implementation("com.fifesoft:rsyntaxtextarea:${fifesoftVersion}")
    implementation("com.fifesoft:rstaui:${fifesoftVersion}")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("at.swimmesberger:swingx-core:1.6.8")
    implementation("com.jgoodies:jgoodies-forms:1.9.0")
    implementation("commons-net:commons-net:3.8.0")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("com.jcraft:jsch:0.1.55")
}


tasks.test {
    useJUnitPlatform()
    testLogging.exceptionFormat = TestExceptionFormat.FULL
}

tasks.compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.encoding = "utf-8"
    options.isDeprecation = false
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to "com.redisfront.RedisFrontApplication")
        attributes("Implementation-Vendor" to "www.redisfront.com")
        attributes("Implementation-Copyright" to "redisfront")
        attributes("Implementation-Version" to project.version)
        attributes("Multi-Release" to "true")
    }

    exclude("module-info.class")
    exclude("META-INF/versions/*/module-info.class")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.LIST")
    exclude("META-INF/*.factories")

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map {
                zipTree(it).matching {
                    exclude("META-INF/LICENSE")
                }
            }
    })

    from("${rootDir}/LICENSE") {
        into("META-INF")
    }
}

