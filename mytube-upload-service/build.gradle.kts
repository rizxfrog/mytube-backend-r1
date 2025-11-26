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
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config:2025.0.0.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

