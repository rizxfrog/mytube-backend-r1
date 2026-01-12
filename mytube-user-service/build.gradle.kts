plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

dependencies {
    implementation(project(":mytube-api"))
    implementation(project(":mytube-common"))
//    implementation("org.projectlombok:lombok:1.18.42") // wrong
    compileOnly("org.projectlombok:lombok:1.18.42") // 编译阶段
    annotationProcessor("org.projectlombok:lombok:1.18.42") // 运行阶段
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.apache.dubbo:dubbo-spring-boot-starter:3.3.5")
    implementation("org.apache.dubbo:dubbo-registry-nacos:3.3.5")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.14")
    implementation("com.baomidou:mybatis-plus-annotation:3.5.14")
    implementation("org.mybatis:mybatis-spring:3.0.3") // 在 Spring 6 / Spring Boot 3 环境下，需要使用 mybatis-spring:3.x 才能兼容新的 FactoryBean 类型系统，否则会出现你遇到的  “Invalid value type for attribute 'factoryBeanObjectType'” 启动报错。
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config:2025.0.0.0")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery:2025.0.0.0")
    implementation("com.alibaba.nacos:nacos-client:2.4.3")
    implementation("software.amazon.awssdk:s3:2.40.17")
//    implementation("software.amazon.awssdk:s3-presigner:2.40.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
}
