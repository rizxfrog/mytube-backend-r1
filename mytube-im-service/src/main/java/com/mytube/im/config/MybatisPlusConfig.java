package com.mytube.im.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.mytube.im.mapper")
public class MybatisPlusConfig {}
