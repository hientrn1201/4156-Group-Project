package dev.coms4156.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to register the API logging interceptor.
 * Ensures all API requests are automatically logged with client information.
 */
@Configuration
public class ApiLoggingConfig implements WebMvcConfigurer {

  @Autowired
  private ApiLoggingInterceptor apiLoggingInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(apiLoggingInterceptor)
        .addPathPatterns("/api/**");
  }
}
