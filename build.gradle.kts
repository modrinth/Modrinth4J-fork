plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

group = project.property("maven_group")!!
version = project.property("version")!!
description = project.property("description").toString()

base {
    archivesName = project.property("archives_base_name").toString()
}

dependencies {
    api(libs.gson)
    api(libs.okhttp)
    compileOnly(libs.lombok)
    compileOnly(libs.jetbrains.annotations)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platformlauncher)

    testImplementation(libs.lombok)
    testImplementation(libs.jetbrains.annotations)
    testImplementation(libs.cdimascio.dotenv)
    testImplementation(libs.commons.codec)
}

tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

tasks.publish { dependsOn(tasks.check) }
tasks.publishToMavenLocal { dependsOn(tasks.check) }

java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

val targetJavaVersion = 17
tasks.withType<JavaCompile>().configureEach {
    this.options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        this.options.release = targetJavaVersion
    }
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
