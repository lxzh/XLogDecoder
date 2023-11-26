plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")

    implementation("com.google.code.gson:gson:2.10")
    implementation("com.alibaba:fastjson:2.0.21")
    implementation("org.python:jython-slim:2.7.3b1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    implementation("io.reactivex.rxjava3:rxjava:3.1.5")

    //https://mvnrepository.com/artifact/net.objecthunter/exp4j
    //小数精度有问题
    implementation("net.objecthunter:exp4j:0.4.8")

    //http://www.singularsys.com/jep/download-trial.php
    //小数精度有问题
    implementation(files("libs\\jep-java-4.0-trial.jar"))

    // https://mvnrepository.com/artifact/com.mpobjects/bdparsii
    implementation("com.mpobjects:bdparsii:1.0.0")

    //https://github.com/alibaba/qlExpress
    implementation("com.alibaba:QLExpress:3.3.0")

    //https://mvnrepository.com/artifact/net.sourceforge.jeval/jeval
    implementation("net.sourceforge.jeval:jeval:0.9.4")

    // https://mvnrepository.com/artifact/org.cheffo/jeplite
    implementation("org.cheffo:jeplite:0.8.7a")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}