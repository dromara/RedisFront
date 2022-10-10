@file:Suppress("UNCHECKED_CAST")

import groovy.lang.Closure
import io.github.fvarrui.javapackager.gradle.PackagePluginExtension
import io.github.fvarrui.javapackager.gradle.PackageTask
import io.github.fvarrui.javapackager.model.*
import io.github.fvarrui.javapackager.model.Platform
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.RuntimeException
import kotlin.String
import kotlin.Suppress
import kotlin.to
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "1.7.20-RC"
}

val javaHome: String = System.getProperty("java.home")
val appName: String = "RedisFront"
val appSite: String = "https://gitee.com/westboy/RedisFront"

buildscript {
    repositories {
        maven("https://maven.aliyun.com/repository/public/")
        mavenLocal()
        mavenCentral()
        dependencies {
            classpath("io.github.fvarrui:javapackager:1.6.7")
        }
    }
}

plugins.apply("io.github.fvarrui.javapackager.plugin")

version = "1.0.3"

val flatlafVersion = "2.4"
val hutoolVersion = "5.8.7"
val fifesoftVersion = "3.2.0"
val derbyVersion = "10.15.2.0"
val lettuceVersion = "6.2.0.RELEASE"
val logbackVersion = "1.4.1"

val fatJar = false


val requireModules = listOf(
    "java.desktop",
    "java.prefs",
    "java.base",
    "java.logging",
    "java.sql",
    "java.naming"
)

val currentJdk = File(javaHome)

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
    implementation("io.netty:netty-common:4.1.82.Final")
    implementation("com.formdev:flatlaf:${flatlafVersion}")
    implementation("org.jfree:jfreechart:1.5.3")
    implementation("com.formdev:flatlaf-swingx:${flatlafVersion}")
    implementation("com.formdev:flatlaf-intellij-themes:${flatlafVersion}")
    implementation("com.formdev:flatlaf-extras:${flatlafVersion}")
    implementation("cn.hutool:hutool-extra:${hutoolVersion}")
    implementation("cn.hutool:hutool-json:${hutoolVersion}")
    implementation("cn.hutool:hutool-http:${hutoolVersion}")
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
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
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

tasks.processResources {
    filesMatching("application.properties") {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "copyright" to "Copyright © 2022-${LocalDateTime.now().plusYears(1).year} $appName",
                "version" to "$version"
            )
        )
    }
    updateVersion()
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
    mainClass("com.redisfront.RedisFrontApplication")
    packagingJdk(currentJdk)
    bundleJre(true)
    customizedJre(true)
    modules(requireModules)
    jreDirectoryName("runtimes")
}



tasks.register<PackageTask>("packageForWindows") {

    val setupLanguageMap = LinkedHashMap<String, String>()
    setupLanguageMap["Chinese"] = "compiler:Languages\\ChineseSimplified.isl"
    setupLanguageMap["English"] = "compiler:Default.isl"

    description = "package For Windows"
    platform = Platform.windows
    isCreateZipball = false
    winConfig(closureOf<WindowsConfig> {
        icoFile = getIconFile("RedisFront.ico")
        headerType = HeaderType.gui
        companyName = appSite
        copyright = appName
        productName = appName
        productVersion = version
        copyright = appSite
        fileVersion = version
        originalFilename = appName
        isGenerateSetup = true
        setupLanguages = setupLanguageMap
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
    linuxConfig(
        closureOf<LinuxConfig> {
            pngFile = getIconFile("RedisFront.png")
            isGenerateDeb = true
            isGenerateRpm = true
            isCreateTarball = true
            isGenerateInstaller = true
            categories = listOf("Office")
        } as Closure<LinuxConfig>
    )
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForMac_M1") {
    description = "package For Mac"
    platform = Platform.mac
    macConfig(
        closureOf<MacConfig> {
            icnsFile = getIconFile("RedisFront.icns")
            isGenerateDmg = true
            macStartup = MacStartup.ARM64
        } as Closure<MacConfig>
    )
    dependsOn(tasks.build)
}

tasks.register<PackageTask>("packageForMac") {
    description = "package For Mac"
    platform = Platform.mac
    macConfig(
        closureOf<MacConfig> {
            icnsFile = getIconFile("RedisFront.icns")
            isGenerateDmg = true
            macStartup = MacStartup.X86_64
        } as Closure<MacConfig>
    )
    dependsOn(tasks.build)
}

fun getIconFile(fileName: String): File {
    return File(projectDir.absolutePath + File.separator + "assets" + File.separator + fileName)
}

fun updateVersion() {
    val jsonFile = File(projectDir.absolutePath + File.separator + "assets" + File.separator + "version.json")
    jsonFile.writeText("{\"version\": \"${version}\"}", Charset.forName("utf-8"))
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}
