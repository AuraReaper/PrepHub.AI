package com.edurag.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.edurag.repository")
public class MongoConfig {
    // Spring Boot auto-configures MongoDB from application.properties
    // This class enables auditing and scanning for repositories
}
