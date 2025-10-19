package com.dmu.debug_visual.config;

import com.dmu.debug_visual.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import util.JwtTokenProvider;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${compiler.python.url}")
    private String compilerPythonUrl;

    /**
     * "dev" 프로파일 (개발 환경)을 위한 보안 설정
     * ADMIN 권한 체크를 제외하여 USER 권한으로도 ADMIN API 테스트가 가능합니다.
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. 누구나 접근 가능한 경로
                        .requestMatchers("/ws/**", "/ws-collab/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/users/login", "/api/users/signup").permitAll()
                        .requestMatchers("/api/code/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/comments/**").permitAll()

                        // 2. USER 권한이 필요한 경로
                        .requestMatchers("/api/posts/**").hasRole("USER")
                        .requestMatchers("/api/notifications/**").hasRole("USER")
                        .requestMatchers("/api/report/**").hasRole("USER")
                        .requestMatchers("/api/comments/**").hasRole("USER")
                        .requestMatchers("/api/files/**").hasRole("USER")
                        .requestMatchers("/api/collab").hasRole("USER")

                        // 3. 나머지 모든 요청은 인증된 사용자만 접근 가능 (ADMIN 경로 포함)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * "prod", "default" 등 운영 환경을 위한 보안 설정
     * ADMIN 경로는 ADMIN 권한이 있는 사용자만 접근 가능합니다.
     */
    @Bean
    @Profile("!dev")
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. 누구나 접근 가능한 경로
                        .requestMatchers("/ws/**", "/ws-collab/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/users/login", "/api/users/signup").permitAll()
                        .requestMatchers("/api/code/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**", "/api/comments/**").permitAll()

                        // 2. ADMIN 권한이 필요한 경로
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 3. USER 권한이 필요한 경로
                        .requestMatchers("/api/posts/**").hasRole("USER")
                        .requestMatchers("/api/notifications/**").hasRole("USER")
                        .requestMatchers("/api/report/**").hasRole("USER")
                        .requestMatchers("/api/comments/**").hasRole("USER")
                        .requestMatchers("/api/files/**").hasRole("USER")
                        .requestMatchers("/api/collab").hasRole("USER")


                        // 4. 나머지 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userRepository),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // --- 공통 Bean 설정 ---
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(compilerPythonUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}