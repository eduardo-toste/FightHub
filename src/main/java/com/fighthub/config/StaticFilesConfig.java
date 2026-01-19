package com.fighthub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticFilesConfig implements WebMvcConfigurer {

    @Value("${uploads.profile-dir}")
    private String profileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/profiles/**")
                .addResourceLocations("file:" + profileDir + "/");
    }

}
