package com.itniuma.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

public class SpringMvcSupport extends WebMvcConfigurationSupport {
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pages/** ").addResourceLocations("/pages/");
        registry.addResourceHandler("/js/** ").addResourceLocations("/js/");
        registry.addResourceHandler("/css/** ").addResourceLocations("/css/");
        registry.addResourceHandler("/plugins/** ").addResourceLocations("/plugins/");
    }
}
