package com.mytube.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mytube.user.mapper")
public class MybatisPlusConfig {}

