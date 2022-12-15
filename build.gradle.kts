import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    java
    id("org.springframework.boot") version "2.7.3"

    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.allopen") version "1.6.21"
    kotlin("plugin.jpa") version "1.6.21"
    kotlin("kapt") version "1.6.21"
}

//apply(plugin = "io.spring.dependency-management")

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

group = "org.quizbe"
version = "0.6"
description = "quizbe"
//java.sourceCompatibility = JavaVersion.VERSION_1_8
java.sourceCompatibility = JavaVersion.VERSION_17


dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")

    // https://docs.spring.io/spring-boot/docs/2.7.1/reference/html/configuration-metadata.html#appendix.configuration-metadata.annotation-processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

//    kapt("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("com.ninja-squad:springmockk:3.1.1")
//    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.3")
//    testImplementation("org.jetbrains.kotlin:kotlin-test:1.7.10")

    implementation("com.google.guava:guava:31.1-jre")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.webjars:bootstrap:5.2.0")
    implementation("org.webjars:popper.js:2.9.3")
    implementation("org.webjars:font-awesome:6.1.2")
    implementation("org.springframework.security:spring-security-config:5.7.3")
    implementation("org.springframework.security:spring-security-web:5.7.3")

    implementation("mysql:mysql-connector-java:8.0.30")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.10")

    implementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")
    implementation("org.jetbrains.kotlin:kotlin-maven-allopen:1.7.10")
    runtimeOnly("org.springframework.boot:spring-boot-devtools:2.7.3")

}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    launchScript()
}

