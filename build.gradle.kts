@file:Suppress("UNCHECKED_CAST")

import groovy.lang.Closure
import io.github.fvarrui.javapackager.gradle.PackagePluginExtension
import io.github.fvarrui.javapackager.gradle.PackageTask
import io.github.fvarrui.javapackager.model.*
import io.github.fvarrui.javapackager.model.Platform
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    `java-library`
    kotlin("jvm") version "2.0.0"
}

buildscript {
    repositories {
        mavenLocal()
        dependencies {
            classpath("io.github.fvarrui:javapackager:1.7.5")
        }
    }
}
plugins.apply("io.github.fvarrui.javapackager.plugin")

allprojects {
    val repoUsername = "662c9a1d2df38b0129acf288"
    val repoPassword = "pub-user"
    repositories {
        maven {
            url = uri("https://packages.aliyun.com/maven/repository/2048752-snapshot-C7TcE7")
            credentials {
                username = repoUsername
                password = repoPassword
            }
        }
        maven {
            url = uri("https://packages.aliyun.com/maven/repository/2048752-release-f1IHDo")
            credentials {
                username = repoUsername
                password = repoPassword
            }
        }
        mavenLocal()
    }
}

version = "2024.1"

val applicationName = "RedisFront"
val organization = "dromara.org"
val supportUrl = "https://redisfront.dromara.org"

val hutoolVersion = "5.8.25"
val fifesoftVersion = "3.2.0"
val derbyVersion = "10.17.1.0"
val lettuceVersion = "6.2.0.RELEASE"
val logbackVersion = "1.4.12"

val fatJar = false

val requireModules = listOf(
    "java.desktop",
    "java.prefs",
    "java.base",
    "java.logging",
    "java.sql",
    "java.naming"
)

if (JavaVersion.current() < JavaVersion.VERSION_22)
    throw RuntimeException("compile required Java ${JavaVersion.VERSION_22}, current Java ${JavaVersion.current()}")

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
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    implementation("io.lettuce:lettuce-core:${lettuceVersion}")
    implementation("org.jfree:jfreechart:1.5.3")
    implementation("cn.hutool:hutool-extra:${hutoolVersion}")
    implementation("cn.hutool:hutool-json:${hutoolVersion}")
    implementation("cn.hutool:hutool-http:${hutoolVersion}")
    implementation("org.apache.derby:derby:${derbyVersion}")
    implementation("com.fifesoft:rsyntaxtextarea:${fifesoftVersion}")
    implementation("com.fifesoft:rstaui:${fifesoftVersion}")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("at.swimmesberger:swingx-core:1.6.8")
    implementation("commons-net:commons-net:3.9.0")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("com.jcraft:jsch:0.1.55")
    implementation("org.dromara:quick-swing:1.1-SNAPSHOT")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
    testLogging.exceptionFormat = TestExceptionFormat.FULL
}

tasks.compileJava {
    options.encoding = "utf-8"
    options.isDeprecation = false
}

tasks.processResources {
    filesMatching("application.properties") {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "copyright" to "Copyright © 2022-${LocalDateTime.now().plusYears(1).year} $applicationName",
                "version" to "$version"
            )
        )
    }
}


tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes("Main-Class" to "org.dromara.redisfront.RedisFrontMain")
        attributes("Implementation-Vendor" to "redisfront.dromara.org")
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

    if (fatJar) {
        from({
            configurations.runtimeClasspath.get()
                .filter { it.name.endsWith("jar") }
                .map {
                    zipTree(it).matching {
                        exclude("META-INF/LICENSE")
                    }
                }
        })
    }

    from("${rootDir}/LICENSE") {
        into("META-INF")
    }
}

configure<PackagePluginExtension> {
    mainClass("org.dromara.redisfront.RedisFrontMain")
    packagingJdk(File(System.getProperty("java.home")))
    bundleJre(true)
    customizedJre(true)
    modules(requireModules)
    jreDirectoryName("runtimes")
}



tasks.register<PackageTask>("packageForWindows") {

    val languages = LinkedHashMap<String, String>()
    languages["Chinese"] = "compiler:Languages\\ChineseSimplified.isl"
    languages["English"] = "compiler:Default.isl"

    description = "package For Windows"

    organizationName = organization
    organizationUrl = supportUrl

    platform = Platform.windows
    isCreateZipball = false
    winConfig(closureOf<WindowsConfig> {
        headerType = HeaderType.gui
        originalFilename = applicationName
        copyright = applicationName
        productName = applicationName
        productVersion = version
        fileVersion = version
        isGenerateSetup = true
        setupLanguages = languages
        isCreateZipball = true
        isGenerateMsi = false
        isGenerateMsm = false
        msiUpgradeCode = version
        isDisableDirPage = false
        isDisableFinishedPage = false
        isDisableWelcomePage = false
    } as Closure<WindowsConfig>)
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForLinux") {
    description = "package For Linux"
    platform = Platform.linux

    organizationName = organization
    organizationUrl = supportUrl

    linuxConfig(
        closureOf<LinuxConfig> {
            isGenerateDeb = true
            isGenerateRpm = true
            isCreateTarball = true
            isGenerateInstaller = true
            categories = listOf("Office")
        } as Closure<LinuxConfig>
    )
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForMac_arm") {
    description = "package For Mac"
    platform = Platform.mac

    organizationName = organization
    organizationUrl = supportUrl

    macConfig(
        closureOf<MacConfig> {
            isGenerateDmg = true
            macStartup = MacStartup.ARM64
        } as Closure<MacConfig>
    )
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForMac_x86") {
    description = "package For Mac"
    platform = Platform.mac

    organizationName = organization
    organizationUrl = supportUrl

    macConfig(
        closureOf<MacConfig> {
            isGenerateDmg = true
            macStartup = MacStartup.X86_64
        } as Closure<MacConfig>
    )
    dependsOn(tasks.build)
}
