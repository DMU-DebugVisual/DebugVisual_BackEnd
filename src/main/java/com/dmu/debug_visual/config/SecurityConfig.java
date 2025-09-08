package com.dmu.debug_visual.config;

import com.dmu.debug_visual.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod; // ✅ 추가
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import util.JwtTokenProvider;

// ⚠️ JwtAuthenticationFilter import는 프로젝트 경로에 맞게 유지하세요.
// import com.dmu.debug_visual.config.JwtAuthenticationFilter; (예시)

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${compiler.python.url}")
    private String compilerPythonUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 우선 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // REST API라면 CSRF 비활성
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // ✅ 프리플라이트 전역 허용 (중요)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ 공개 엔드포인트
                .requestMatchers(
                    "/api/users/login",
                    "/api/users/signup",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/actuator/health",
                    "/healthz",
                    "/public/**"
                ).permitAll()

                // 역할 기반
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/posts/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/notifications/**").hasAnyRole("USER", "ADMIN")

                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            // JWT 필터 (위치 유지)
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                    UsernamePasswordAuthenticationFilter.class)
            // 폼/기본 인증 비활성
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 백엔드 → 파이썬 컴파일러 호출용 WebClient
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(compilerPythonUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ✅ CORS: 프록시(Nginx)와 값 일치 — zivorp.com만 허용(+ www, http/https)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // AllowedOriginPatterns("*") + Credentials(true)는 브라우저에서 막히기 쉬움.
        // 명시적으로 허용 도메인만 지정하세요.
        config.setAllowedOrigins(List.of(
            "https://zivorp.com",
            "http://zivorp.com",
            "https://www.zivorp.com",
            "http://www.zivorp.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
