plugins {
    java
    id("org.springframework.boot") version "3.5.8" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
