plugins {
    java
}

dependencies {
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("org.projectlombok:lombok:1.18.42")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:3.5.8")
    implementation("org.springframework:spring-web:6.2.14")
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.52")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.14")

}
