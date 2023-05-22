package seoultech.capstone.menjil.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import seoultech.capstone.menjil.global.filter.CustomCorsFilter;
//import seoultech.capstone.menjil.global.filter.CorsFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity              // Spring Security Filter 가 Spring Filter Chain 에 등록이 된다.
public class SecurityConfig {   // WebSecurityConfigurerAdapter is deprecated

    //    private final CorsFilter corsFilter;
    private final CustomCorsFilter customCorsFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. CSRF 해제
        http.csrf().disable();

        // 2. jSessionId 사용 거부
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 3. UsernamePasswordAuthenticationFilter 비활성화
        http.formLogin().disable();

        /* 오류가 발생하면 4번부터 제거 */
        // 4. 로그인 인증창이 뜨지 않게 비활성화
        http.httpBasic().disable();

        // 5. cors 재설정: 현재 사용 X
//        http.cors().configurationSource(configurationSource());

        http.addFilterBefore(customCorsFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE (Javascript 요청 허용)
        configuration.addAllowedOriginPattern("http://localhost:3000");
        configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용
        configuration.addExposedHeader("Authorization"); // 옛날에는 디폴트 였다. 지금은 아닙니다.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
