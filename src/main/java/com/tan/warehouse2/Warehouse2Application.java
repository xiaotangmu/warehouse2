package com.tan.warehouse2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(value = "com.tan.warehouse2.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Warehouse2Application {

	public static void main(String[] args) {
		SpringApplication.run(Warehouse2Application.class, args);
	}

}
