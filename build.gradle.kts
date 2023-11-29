import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`java`
	`maven-publish`
	id("org.springframework.boot") version "2.6.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.9.20"
	kotlin("plugin.spring") version "1.9.20"
	kotlin("plugin.allopen") version "1.9.20"
	kotlin("plugin.serialization") version "1.9.20"
	kotlin("kapt") version "1.9.20"
}

group = "com.github.nayasis"
version = "0.2.6-SNAPSHOT"

configurations.all {
	resolutionStrategy.cacheChangingModulesFor(0, "seconds")
	resolutionStrategy.cacheDynamicVersionsFor(5, "minutes")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
	withJavadocJar()
	withSourcesJar()
}

repositories {
	mavenLocal()
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

dependencies {

	kapt("org.springframework.boot:spring-boot-autoconfigure-processor")
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-autoconfigure")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-test")

	implementation("com.github.nayasis:basica-kt:0.3.1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:3.0.5")
	implementation("javax.servlet:javax.servlet-api:4.0.1")
	implementation("org.apache.commons:commons-text:1.10.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.2")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.1")
	testImplementation("ch.qos.logback:logback-classic:1.2.9")

}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf(
			"-Xjsr305=strict"
		)
		jvmTarget = "1.8"
	}
}

//tasks.withType<GenerateMavenPom>().all {
//	doLast {
//		val file = File("$buildDir/publications/maven/pom-default.xml")
//		var text = file.readText()
//		val regex = "(?s)(<dependencyManagement>.+?<dependencies>)(.+?)(</dependencies>.+?</dependencyManagement>)".toRegex()
//		val matcher = regex.find(text)
//		if (matcher != null) {
//			text = regex.replaceFirst(text, "")
//			val firstDeps = matcher.groups[2]!!.value
//			text = regex.replaceFirst(text, "$1$2$firstDeps$3")
//		}
//		file.writeText(text)
//	}
//}

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = project.group.toString()
			artifactId = project.name
			version = project.version.toString()
			from(components["java"])
		}
	}
}