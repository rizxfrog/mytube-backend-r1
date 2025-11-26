plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

dependencies {
    implementation(project(":mytube-api"))
    implementation(project(":mytube-common"))
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.apache.dubbo:dubbo-spring-boot-starter:3.3.5")
    implementation("org.apache.dubbo:dubbo-registry-nacos:3.3.5")
    implementation("com.alibaba.nacos:nacos-client:2.4.3")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-config:2025.0.0.0")
    implementation("com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery:2025.0.0.0")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
