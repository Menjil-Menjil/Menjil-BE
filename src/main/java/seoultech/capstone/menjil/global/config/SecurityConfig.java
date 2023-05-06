package seoultech.capstone.menjil.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity              // Spring Security Filter 가 Spring Filter Chain 에 등록이 된다.
public class SecurityConfig {   // WebSecurityConfigurerAdapter is deprecated

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
//        http
//                .authorizeRequests()
//                .antMatchers("/**")
//                .authenticated()
//                .anyRequest()
//                .permitAll();
//                .and()
//                .oauth2Login()
//                .userInfoEndpoint()
//                .userService(customOAuth2UserService);

        return http.build();
    }
}
