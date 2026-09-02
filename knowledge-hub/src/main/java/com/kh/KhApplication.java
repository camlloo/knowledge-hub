package com.kh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.kh.user.mapper", "com.kh.folder.mapper", "com.kh.file.mapper", "com.kh.tag.mapper"})
public class KhApplication {

    public static void main(String[] args) {
        SpringApplication.run(KhApplication.class, args);
    }
}
