package com.orsonwu.limited_sale;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.orsonwu.limited_sale.db.mappers")
@ComponentScan(basePackages = {"com.orsonwu"})
public class LimitedSaleProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(LimitedSaleProjectApplication.class, args);
    }

}
