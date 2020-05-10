# 参考 Docker 官方推荐的最佳实践： https://www.docker.com/blog/intro-guide-to-dockerfile-best-practices/
# 请在编写完成后使用 Linter 检查： https://hadolint.github.io/hadolint/

# 使用分阶段构建，编译环境一个镜像，运行环境一个镜像
ARG BUILDER_IMAGE=gradle:jdk11
ARG RUNNER_IMAGE=adoptopenjdk:11-jre-openj9

# ===============================================

# 编译环境
FROM $BUILDER_IMAGE AS buildingStage

# Gradle 基础镜像中已经包含一个低特权用户（gradle）
RUN mkdir -p /build \
    && chown -R gradle /build
USER gradle

# 配置 Gradle 使用 Maven 镜像源（阿里云）
COPY --chown=gradle \
    docs/Gradle/mirrors.init.gradle.kts \
    /home/gradle/.gradle/init.d/mirrors.init.gradle.kts

# 先让 Gradle 下载部分依赖，有利于 Docker 缓存
# Gradle 在 Docker 中无法完美缓存，不必纠结，想要高效使用 Gradle 只能用长期运行的 Build Server 或 CI 服务
# https://stackoverflow.com/questions/25873971/docker-cache-gradle-dependencies
WORKDIR /build
COPY *.gradle.kts ./
RUN gradle buildEnvironment dependencies --no-daemon --quiet

# 编译项目
COPY src ./src
RUN gradle bootJar --no-daemon --quiet

# ===============================================

# 运行环境
FROM $RUNNER_IMAGE AS runningStage

# 即使在容器中，也应当使用低特权用户
RUN mkdir -p /home/runner \
    && useradd runner -d /home/runner \
    && chown -R runner:runner /home/runner
USER runner:runner

EXPOSE 8080/tcp

ENV SPRING_PROFILES_ACTIVE=RELEASE

WORKDIR /app

# 以下步骤会因项目代码变化而使构建缓存失效，因此放在最后
COPY --from=buildingStage /build/build/libs/*.jar app.jar

# 使用 AOT 预热，以加快启动速度，缺点是缓存文件会增大镜像体积，因此以 -Xscmx -Xscmaxaot 参数做限制
# https://developer.ibm.com/technologies/java/articles/eclipse-openj9-class-sharing-in-docker-containers/
RUN sh -c 'java -Xshareclasses -Xscmx48M -Xscmaxaot16M -jar app.jar &' \
    ; sleep 30s \
    ; pgrep "java" | xargs kill

# OpenJ9 JVM 容器环境优化参数
# https://developer.ibm.com/technologies/java/articles/optimize-jvm-startup-with-eclipse-openjj9/
ENTRYPOINT ["java", "-Xshareclasses", "-Xtune:virtualized", "-jar", "app.jar"]
