package com.gooners.watguessr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

@Configuration
public class JacksonConfig {
    @Bean
    public Hibernate6Module hibernate5Module() {
        Hibernate6Module module = new Hibernate6Module();
        // if you don’t want Jackson to automatically fetch all lazy associations:
        module.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        return module;
    }
}
