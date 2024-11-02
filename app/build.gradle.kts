plugins {
    // Apply the shared build logic from a convention plugin
    id("buildsrc.convention.kotlin-jvm")
    // Add java-library plugin if not already included in your convention plugin
    `java-library`
    // Dokka plugin for Kotlin documentation
    id("org.jetbrains.dokka") version "1.9.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    testImplementation(kotlin("test"))
}

// Configure Java compilation and packaging
java {
    withJavadocJar()  // This already creates the javadocJar task
    withSourcesJar()
}

// Configure Javadoc
tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }

    options {
        encoding = "UTF-8"
        (this as StandardJavadocDocletOptions).apply {
            links?.add("https://docs.oracle.com/en/java/javase/11/docs/api/")
        }
    }
}

// Configure Dokka
tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set(project.name)
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                // Replace with your repository URL
                remoteUrl.set(uri("https://github.com/yourusername/yourrepo/blob/main/src/main/kotlin").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}

// Create only the Dokka JAR since javadocJar is already created by java.withJavadocJar()
tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml)
    archiveClassifier.set("dokka")
}