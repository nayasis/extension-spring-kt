import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	kotlin("jvm") version "2.2.0"
	kotlin("plugin.spring") version "2.2.10"
	kotlin("kapt") version "2.2.10"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	java
	signing
	id("com.vanniktech.maven.publish") version "0.31.0"
}

group       = "io.github.nayasis"
description = "SpringBoot utility for Kotlin"
version     = when {
	project.hasProperty("mavenReleaseVersion") && project.property("mavenReleaseVersion").let { it != "" && it != "unspecified" } -> {
		project.property("mavenReleaseVersion") as String
	}
	else -> "0.1.0-SNAPSHOT"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {

	implementation("io.github.nayasis:basica-kt:0.3.9")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")

	kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	compileOnly("org.springframework.boot:spring-boot-autoconfigure")
	compileOnly("org.springframework:spring-web")
	compileOnly("org.springframework:spring-webmvc")
	compileOnly("org.springframework:spring-test")
	compileOnly("org.springframework.data:spring-data-redis")
	compileOnly("org.springframework.data:spring-data-jpa")
	compileOnly("jakarta.persistence:jakarta.persistence-api")
	compileOnly("org.hibernate.orm:hibernate-core")
	compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

	implementation("org.apache.commons:commons-text:1.14.0") {
		exclude(group = "org.apache.commons", module = "commons-lang3")
	}
	implementation("org.apache.commons:commons-lang3:3.18.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "ch.qos.logback", module = "logback-core")
	}
	testImplementation("ch.qos.logback:logback-core:1.5.19")
	testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
	testImplementation("io.kotest:kotest-assertions-core:5.9.1")
	testImplementation("org.springframework:spring-web")

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
	if(listOf("publishToMavenLocal","publishMavenPublicationToMavenLocal").none{gradle.startParameter.taskNames.contains(it)}) {
		signAllPublications()
	}
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
	pom {
		name        = project.name
		description = "SpringBoot utility library providing common functionality on Kotlin"
		url         = "https://github.com/nayasis/extension-spring-kt"
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