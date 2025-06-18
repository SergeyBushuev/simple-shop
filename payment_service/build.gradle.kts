
plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.5")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("buildPaymentApi") {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/api.yaml")
    outputDir.set("$projectDir/build/generated")
    ignoreFileOverride.set(".openapi-generator-java-sources.ignore")
    invokerPackage.set("ru.yandex.payment_service")
    modelPackage.set("ru.yandex.payment_service.model")
    apiPackage.set("ru.yandex.payment_service.api")
    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "requestMappingMode" to "controller",
        "interfaceOnly" to "true",
        "library" to "spring-boot",
        "reactive" to "true",
        "useSpringBoot3" to "true",
        "useJakartaEe" to "true",
        "useTags" to "true",
        "dateLibrary" to "java8",
        "openApiNullable" to "false",
        "serializableModel" to "true",
        "returnSuccessCode" to "true"
    ))
}

sourceSets["main"].java.srcDir("$projectDir/build/generated/src/main/java")

tasks.compileJava {
    dependsOn("buildPaymentApi")
}

tasks.withType<Test> {
    useJUnitPlatform()
}