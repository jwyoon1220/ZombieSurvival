plugins {
    kotlin("jvm") version "2.0.20"
}

group = "io.jwyoon1220"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://jitpack.io/")
    maven("https://repo.repsy.io/mvn/lone64/platform")


}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    implementation("io.github.R2turnTrue:chzzk4j:0.0.10")
    implementation("io.github.monun:tap-api:4.6.1")
    implementation("io.github.monun:kommand-api:3.1.7")
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.test {
    useJUnitPlatform()
}