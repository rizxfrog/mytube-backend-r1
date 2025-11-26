package com.mytube.comment.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mytube.comment.mapper")
public class MybatisPlusConfig {}

