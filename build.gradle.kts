buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0")
        classpath(kotlin("gradle-plugin", "1.8.0"))
        classpath(kotlin("serialization", "1.8.0"))
    }
}
