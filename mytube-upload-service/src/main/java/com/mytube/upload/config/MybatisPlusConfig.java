package com.mytube.upload.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mytube.upload.mapper")
public class MybatisPlusConfig {}
