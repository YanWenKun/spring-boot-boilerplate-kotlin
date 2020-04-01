/*
 * 如何创建 Gradle 构建： https://guides.gradle.org/creating-new-gradle-builds
 * 查看 Spring 官方教程： https://spring.io/guides/tutorials/spring-boot-kotlin/#use-gradle
 * 编辑整理： code@yanwk.fun
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Gradle 插件
plugins {
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    // 这里的 Kotlin 版本号是 Spring 的保险设计，标准 Kotlin 项目会由 plugin 自动管理版本，不需要一个一个写
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
}

// 项目工程信息
group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

// 为 Spring Boot DevTools 依赖指定“仅开发时”
val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

// 软件仓库地址，默认 Maven 中央库
// 如需配置镜像，建议本地配置，勿入项目配置
repositories {
    mavenCentral()
}

// 指定项目依赖
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict") // 开启Kotlin null安全特性（编译期检查）
        jvmTarget = "1.8"
    }
}
