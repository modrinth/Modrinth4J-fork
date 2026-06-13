plugins {
    `java-library`
    `maven-publish`
    id("org.openapi.generator") version "7.23.0"
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

    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.20")
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

    implementation("org.jspecify:jspecify:1.0.0")

    testImplementation(libs.lombok)
    testImplementation(libs.jetbrains.annotations)
    testImplementation(libs.cdimascio.dotenv)
    testImplementation(libs.commons.codec)
}

openApiGenerate {
    generatorName.set("java")
    inputSpec.set(layout.projectDirectory.file("src/main/resources/labrinth-open-api-3.1.0.yaml").asFile.absolutePath)
    outputDir.set(layout.buildDirectory.dir("generated-sources").map { it.asFile.absolutePath }.get())
    apiPackage.set("com.modrinth.client.api")
    modelPackage.set("com.modrinth.client.model")
    configOptions.set(mapOf(
        "library" to "native",
        "useJakartaEe" to "true",
        "useJspecify" to "true"
    ))
}

sourceSets {
    main {
        java {
            srcDirs("${layout.buildDirectory.get()}/generated-sources/src/main/java")
        }
    }
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
