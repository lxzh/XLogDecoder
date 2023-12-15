import org.jetbrains.compose.desktop.application.dsl.TargetFormat

apply("config.gradle.kts")

val kotlinVersion = "1.9.20"
val programName: String by project
val gitCommitCount: Int by project
val buildFormatDate: String by project
val gitCommitShortid: String by project
val myPackageVersion: String by project
val myPackageVendor: String by project
val winUpgradeUuid: String by project
val javaVersion = JavaVersion.VERSION_17
val javaVersionString = "17"

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig") version "3.0.3"
    kotlin("plugin.serialization")
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.aliyun.com/nexus/content/groups/public/")
    maven("https://maven.aliyun.com/repository/google")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = javaVersionString
    }
}
tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = javaVersionString
    }
}

dependencies {
    implementation(files("libs\\bcprov-jdk15on-159.jar"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.0-native-mt")
    //implementation("com.jakewharton.timber:timber:4.7.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            outputBaseDir.set(project.rootDir.resolve("out/packages"))
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = programName
            packageVersion = myPackageVersion
            vendor = myPackageVendor
            windows {
                //console = true
                menu = true
                dirChooser = true
                shortcut = true
                perUserInstall = false
                //menuGroup = myMenuGroup
                iconFile.set(project.file("src/main/resources/icon.ico"))
                upgradeUuid = winUpgradeUuid
            }
        }
    }
}
