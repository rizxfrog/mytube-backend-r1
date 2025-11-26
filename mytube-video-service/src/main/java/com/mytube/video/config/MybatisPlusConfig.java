package com.mytube.video.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mytube.video.mapper")
public class MybatisPlusConfig {}

