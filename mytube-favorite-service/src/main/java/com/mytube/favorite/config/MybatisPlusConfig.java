package com.mytube.favorite.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mytube.favorite.mapper")
public class MybatisPlusConfig {}

