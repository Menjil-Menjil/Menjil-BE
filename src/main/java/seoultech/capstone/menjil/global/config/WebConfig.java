package seoultech.capstone.menjil.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;
import seoultech.capstone.menjil.global.filter.CustomCorsFilter;
import seoultech.capstone.menjil.global.filter.JwtAuthenticationFilter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean<CustomCorsFilter> customCorsFilter() {
        FilterRegistrationBean<CustomCorsFilter> registrationBean
                = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CustomCorsFilter());
        registrationBean.setOrder(1);
        registrationBean.setName("First-CustomCorsFilter");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean
                = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationFilter(jwtTokenProvider, objectMapper));
        registrationBean.addUrlPatterns("/api/user/*");
        registrationBean.setOrder(2);
        registrationBean.setName("Second-JwtAuthenticationFilter");
        return registrationBean;
    }

    /**
     * /docs/index.html 접근이 가능하도록 하는 설정이다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/");
    }

    /*
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    } */

}


