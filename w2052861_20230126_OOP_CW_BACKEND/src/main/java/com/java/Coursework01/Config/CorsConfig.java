package com.java.Coursework01.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Marking this class as a configuration class for Spring Boot
@Configuration
public class CorsConfig {

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     *
     * @return WebMvcConfigurer instance with customized CORS settings.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Defines CORS mappings to allow the frontend application to
             * communicate with the backend securely and efficiently.
             *
             * @param registry CORS registry to add mappings.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply CORS settings to all endpoints
                        .allowedOrigins("http://localhost:4200") // Allow requests from the Angular frontend hosted at this origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specified HTTP methods
                        .allowedHeaders("*"); // Allow all headers in the requests
            }
        };
    }
}
