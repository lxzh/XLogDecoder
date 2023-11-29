pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "public"
            setUrl("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "google"
            setUrl("https://maven.aliyun.com/repository/google")
        }
        maven {
            name = "gradle-plugin"
            setUrl("https://maven.aliyun.com/repository/gradle-plugin")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        maven {
            name = "public"
            setUrl("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "google"
            setUrl("https://maven.aliyun.com/repository/google")
        }
        maven {
            name = "gradle-plugin"
            setUrl("https://maven.aliyun.com/repository/gradle-plugin")
        }
    }
}

rootProject.name = "KTDemo"
include(":leetCode")
include(":app")