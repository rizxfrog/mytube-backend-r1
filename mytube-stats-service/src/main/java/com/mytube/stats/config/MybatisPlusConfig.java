package com.mytube.stats.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mytube.stats.mapper")
public class MybatisPlusConfig {}

