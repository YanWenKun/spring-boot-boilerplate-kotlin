// 配置本地 Gradle 全局使用 Maven 镜像源
// 请将本文件置于： ~/.gradle/init.gradle.kts
allprojects {
    repositories {
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/spring")
        maven("https://maven.aliyun.com/repository/spring-plugin")
        maven("https://maven.aliyun.com/repository/google")
        // 不推荐启用本地 Maven，更不推荐将其置于第一位
        // 1. 本地 Maven 本质上是 Maven 自己专用的 Cache，Gradle 无法对其溯源（无法得知是否完整、是否被篡改）
        // 2. 因此 Gradle 不会对本地 Maven 进行缓存操作，这将会降低构建速度
        //mavenLocal()
    }
}
