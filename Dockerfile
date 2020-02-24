# 参考 Docker 官方推荐的最佳实践： https://www.docker.com/blog/intro-guide-to-dockerfile-best-practices/

# 使用分阶段构建，编译环境一个镜像，运行环境一个镜像
ARG BUILDER_IMAGE=gradle:jdk8
ARG RUNNER_IMAGE=adoptopenjdk:8-jre-openj9

# ===============================================

# 编译环境
FROM $BUILDER_IMAGE AS buildingStage

# Gradle 基础镜像中已经包含一个低特权用户（gradle）
RUN mkdir -p /build && \
    chown -R gradle /build
USER gradle

# 配置 Gradle 使用 Maven 镜像源（阿里云）
COPY --chown=gradle \
    docs/Gradle/mirrors.init.gradle.kts \
    /home/gradle/.gradle/init.d/mirrors.init.gradle.kts

# 先让 Gradle 下载部分依赖，有利于 Docker 缓存
WORKDIR /build
COPY *.gradle.kts ./
RUN gradle buildEnvironment --no-daemon --quiet

# 编译项目
COPY src ./src
RUN gradle bootJar --no-daemon --quiet

# ===============================================

# 运行环境
FROM $RUNNER_IMAGE AS runningStage

# 即使在容器中，也应当使用低特权用户
RUN mkdir -p /home/runner && \
        useradd runner -d /home/runner && \
        chown -R runner:runner /home/runner
USER runner:runner

WORKDIR /app

# OpenJ9 JVM 容器环境优化参数 https://developer.ibm.com/technologies/java/articles/optimize-jvm-startup-with-eclipse-openjj9/
ENV JAVA_OPTS="-Xshareclasses -Xtune:virtualized"

ENV SPRING_PROFILES_ACTIVE=RELEASE

EXPOSE 8080/tcp

COPY --from=buildingStage /build/build/libs/*.jar app.jar

ENTRYPOINT java $JAVA_OPTS -jar app.jar
