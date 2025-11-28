import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	kotlin("jvm") version "2.2.0"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.spring") version "2.2.10"
	kotlin("kapt") version "2.2.10"
	java
	signing
	id("com.vanniktech.maven.publish") version "0.31.0"


}

group = "com.github.nayasis"
version = when {
	project.hasProperty("mavenReleaseVersion") && project.property("mavenReleaseVersion") != "unspecified" && project.property("mavenReleaseVersion") != "" -> {
		project.property("mavenReleaseVersion") as String
	}
	else -> "0.1.0-SNAPSHOT"
}
description = "SpringBoot utility for Kotlin"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {

	implementation("io.github.nayasis:basica-kt:0.3.8")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

	kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.springframework.boot:spring-boot-autoconfigure")
	implementation("org.springframework:spring-web")
	implementation("org.springframework:spring-webmvc")
	implementation("org.springframework:spring-test")
	implementation("org.springframework.data:spring-data-redis")
	implementation("org.springframework.data:spring-data-jpa")
	implementation("jakarta.persistence:jakarta.persistence-api")
	implementation("org.hibernate.orm:hibernate-core")

	implementation("jakarta.servlet:jakarta.servlet-api:6.1.0")
	implementation("org.apache.commons:commons-text:1.10.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.2")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
	testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
	testImplementation("io.kotest:kotest-assertions-core:5.6.2")
	testImplementation("ch.qos.logback:logback-classic:1.5.19")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<JavaCompile> {
	options.release.set(17)
}

tasks.getByName<Jar>("jar") {
	enabled = true
	archiveClassifier.set("")
}

tasks.withType<BootJar> {
	enabled = false
}

mavenPublishing {
	if (!gradle.startParameter.taskNames.any {
			it.contains("publishToMavenLocal") || it.contains("publishMavenPublicationToMavenLocal")
		}) {
		signAllPublications()
	}
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
	pom {
		name        = project.name
		description = "SpringBoot utility library providing common functionality on Kotlin"
		url         = "https://github.com/nayasis/basica-kt"
		licenses {
			license {
				name = "Apache License, Version 2.0"
				url  = "http://www.apache.org/licenses/LICENSE-2.0.txt"
			}
		}
		developers {
			developer {
				id    = "nayasis"
				name  = "nayasis"
				email = "nayasis@gmail.com"
			}
		}
		scm {
			url                 = "https://github.com/nayasis/extension-spring-kt"
			connection          = "scm:git:github.com/nayasis/extension-spring-kt.git"
			developerConnection = "scm:git:ssh://github.com/nayasis/extension-spring-kt.git"
		}
	}
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.compilerOptions {
	freeCompilerArgs = listOf("-XXLanguage:+BreakContinueInInlineLambdas")
}