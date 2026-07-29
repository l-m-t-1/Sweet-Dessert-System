package com.sweet.dessertsystem;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan({
        "com.sweet.dessertsystem.mapper",
        "com.sweet.dessertsystem.category",
        "com.sweet.dessertsystem.dashboard",
        "com.sweet.dessertsystem.stock"
})
@SpringBootApplication
public class DessertSystemApplication {


    public static void main(String[] args) {

        SpringApplication.run(DessertSystemApplication.class,args);

    }

}
