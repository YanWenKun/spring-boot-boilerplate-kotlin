/*
 * 如何创建Gradle构建： https://guides.gradle.org/creating-new-gradle-builds
 * 查看Spring官方教程： https://spring.io/guides/tutorials/spring-boot-kotlin/#use-gradle
 * 编辑整理： yanwenkun@foxmail.com
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// 编译器插件
plugins {
    id("org.springframework.boot") version "2.2.1.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    // 这里的 Kotlin 版本号是 Spring 的保险设计，标准 Kotlin 项目会由 plugin 自动管理版本，不需要一个一个写
    kotlin("jvm") version "1.3.60"
    kotlin("plugin.spring") version "1.3.60"
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

// 使用阿里云的 Maven 源，以加速下载
// 一般情况下，不建议在本地 Gradle 中进行全局配置，分项目配置即可
repositories {
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.aliyun.com/repository/jcenter")
    maven("https://maven.aliyun.com/repository/gradle-plugin")
    maven("https://maven.aliyun.com/repository/spring")
    maven("https://maven.aliyun.com/repository/spring-plugin")
    maven("https://maven.aliyun.com/repository/google")
    mavenLocal()
    mavenCentral()
    jcenter()
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
