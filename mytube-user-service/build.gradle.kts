plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

dependencies {
    implementation(project(":mytube-api"))
    implementation(project(":mytube-common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.apache.dubbo:dubbo-spring-boot-starter:3.3.5")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.14")
    implementation("org.mybatis:mybatis-spring:3.0.3") // 在 Spring 6 / Spring Boot 3 环境下，需要使用 mybatis-spring:3.x 才能兼容新的 FactoryBean 类型系统，否则会出现你遇到的  “Invalid value type for attribute 'factoryBeanObjectType'” 启动报错。
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config:2025.0.0.0")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery:2025.0.0.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
