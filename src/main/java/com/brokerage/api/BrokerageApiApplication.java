package com.brokerage.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing
@EnableAspectJAutoProxy(exposeProxy = true)
public class BrokerageApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrokerageApiApplication.class, args);
    }

}
