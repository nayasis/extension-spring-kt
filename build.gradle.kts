import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`maven`
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.allopen") version "1.6.10"
	kotlin("plugin.noarg") version "1.6.10"
	kotlin("plugin.serialization") version "1.6.10"
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

noArg {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
	annotation("com.github.nayasis.kotlin.basica.annotation.NoArg")
	invokeInitializers = true
}

group = "com.github.nayasis"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

configurations.all {
	resolutionStrategy.cacheChangingModulesFor(0, "seconds")
	resolutionStrategy.cacheDynamicVersionsFor(5, "minutes")
}

java {
	// for 'supportImplementation'
	registerFeature("support") {
		usingSourceSet(sourceSets["main"])
	}
}

repositories {
	mavenLocal()
	mavenCentral()
	jcenter()
	maven { url = uri("https://jitpack.io") }
}

dependencies {

	implementation("com.github.nayasis:basica-kt:0.1.15")
//	implementation("com.github.nayasis:basica-kt:develop-SNAPSHOT")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:1.8.3")
	implementation("au.com.console:kassava:2.1.0")
	implementation("javax.servlet:javax.servlet-api:4.0.1")
	implementation("org.apache.commons:commons-text:1.8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.+")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.2")

	"supportImplementation"("it.ozimov:embedded-redis:0.7.2")
	"supportImplementation"("org.springframework.boot:spring-boot-starter-web:2.5.6")
	"supportImplementation"("org.springframework.boot:spring-boot-starter-data-jpa:2.5.6")
	"supportImplementation"("org.springframework.boot:spring-boot-starter-cache:2.5.6")
	"supportImplementation"("org.springframework.data:spring-data-redis:2.5.6")
	"supportImplementation"("org.springframework.boot:spring-boot-starter-test:2.5.6")
	"supportImplementation"("ch.qos.logback:logback-classic:1.2.3")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
	testImplementation("org.junit.jupiter:junit-jupiter-engine:5.3.1")
	testImplementation("ch.qos.logback:logback-classic:1.2.3")

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