plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
}

group = "ru.yandex"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.5")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    runtimeOnly("org.postgresql:postgresql:42.7.2")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("org.springframework.security:spring-security-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("buildPaymentClientApi") {
    generatorName.set("java")
    inputSpec.set("$projectDir/src/main/resources/api.yaml")
    outputDir.set("$projectDir/build/generated")
    ignoreFileOverride.set(".openapi-generator-java-sources.ignore")
    invokerPackage.set("ru.yandex.simple_shop")
    modelPackage.set("ru.yandex.simple_shop.model")
    apiPackage.set("ru.yandex.simple_shop.api")
    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "library" to "webclient",
        "useJakartaEe" to "true",
        "useTags" to "true",
        "openApiNullable" to "false",
        "serializableModel" to "true"
    ))
}

sourceSets["main"].java.srcDir("$projectDir/build/generated/src/main/java")

tasks.compileJava {
    dependsOn("buildPaymentClientApi")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
