package com.journeyplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Enables CORS for the frontend dev server (and any configured origins). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

   @Value("${journey.cors-origins:http://localhost:5173}")
   private String[] allowedOrigins;

   @Override
   public void addCorsMappings(CorsRegistry registry) {
      registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST");
   }
}
