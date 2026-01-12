plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

dependencies {
    implementation(project(":mytube-api"))
    implementation(project(":mytube-common"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.apache.dubbo:dubbo-spring-boot-starter:3.3.5")
    implementation("io.minio:minio:8.6.0")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.14")
    implementation("org.mybatis:mybatis-spring:3.0.3")
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config:2025.0.0.0")
    implementation("software.amazon.awssdk:s3:2.40.17")
//    implementation("software.amazon.awssdk:s3-presigner:2.40.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
