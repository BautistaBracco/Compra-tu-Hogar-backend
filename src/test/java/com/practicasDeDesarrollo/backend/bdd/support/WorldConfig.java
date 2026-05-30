package com.practicasDeDesarrollo.backend.bdd.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class WorldConfig {

    @Bean
    @Scope("cucumber-glue")
    public BddWorld bddWorld() {
        return new BddWorld();
    }
}
