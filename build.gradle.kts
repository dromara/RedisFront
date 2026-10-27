@file:Suppress("UNCHECKED_CAST")

import groovy.lang.Closure
import io.github.javapackager.gradle.PackagePluginExtension
import io.github.javapackager.gradle.PackageTask
import io.github.javapackager.model.*
import io.github.javapackager.model.Platform
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
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    dependencies { classpath("io.github.javapackager:javapackager:2.0.0") }
}
plugins.apply("io.github.javapackager")

allprojects {
    val repoUsername = "662c9a1d2df38b0129acf288"
    val repoPassword = "pub-user"
    repositories {
        mavenCentral()
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

version = "2026.1"

val applicationName = "RedisFront"
val organization = "dromara.org"
val supportUrl = "https://redisfront.dromara.org"

val hutoolVersion = "5.8.25"
val fifesoftVersion = "3.5.4"
val lettuceVersion = "6.5.4.RELEASE"
val logbackVersion = "1.4.12"

val fatJar = false

val requireModules = listOf(
    "java.desktop",
    "java.prefs",
    "java.base",
    "java.logging",
    "java.sql",
    "java.management",
    "java.naming",
    "jdk.crypto.ec"
)

if (JavaVersion.current() < JavaVersion.VERSION_21)
    throw RuntimeException("compile required Java ${JavaVersion.VERSION_21}, current Java ${JavaVersion.current()}")

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
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    implementation("io.lettuce:lettuce-core:${lettuceVersion}")
    implementation("org.jfree:jfreechart:1.5.3")
    implementation("cn.hutool:hutool-extra:${hutoolVersion}")
    implementation("cn.hutool:hutool-json:${hutoolVersion}")
    implementation("cn.hutool:hutool-http:${hutoolVersion}")
    implementation("com.fifesoft:rsyntaxtextarea:${fifesoftVersion}")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("at.swimmesberger:swingx-core:1.6.8")
    implementation("commons-net:commons-net:3.9.0")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.intellij:forms_rt:7.0.3")
    implementation("com.github.mwiede:jsch:0.2.23")
    implementation("org.dromara:quick-swing:1.1-SNAPSHOT")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
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
    // ChineseSimplified.isl 属于 Inno Setup 的非官方翻译（位于其源码的
    // Files/Languages/Unofficial/ 下），标准安装包并不包含它——GitHub Windows
    // runner 上预装的 6.7.1 就没有这个文件，用 compiler: 前缀必然编译失败。
    // 因此随仓库分发，用绝对路径引用。
    languages["Chinese"] = layout.projectDirectory
        .file("assets/windows/Languages/ChineseSimplified.isl").asFile.absolutePath
    languages["English"] = "compiler:Default.isl"

    description = "package For Windows"

    organizationName = organization
    organizationUrl = supportUrl

    platform = Platform.windows
    createZipball = true
    winConfig(closureOf<WindowsConfig> {
        headerType = HeaderType.gui
        originalFilename = applicationName
        copyright = applicationName
        productName = applicationName
        productVersion = version
        fileVersion = version
        isGenerateSetup = true
        setupLanguages = languages
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
    createTarball = true

    organizationName = organization
    organizationUrl = supportUrl

    linuxConfig(
        closureOf<LinuxConfig> {
            isGenerateDeb = true
            isGenerateRpm = true
            // LinuxConfig 默认 generateAppImage=true，会在打包时额外下载 appimagetool；
            // 我们的发布产物是 deb/rpm/tar.gz，不需要 AppImage，显式关闭以免引入外部网络依赖。
            isGenerateAppImage = false
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
