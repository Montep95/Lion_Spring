package com.example.securityexam3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception{
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hello/**", "/shop/**", "/userinfo", "/img/**", "/static/**", "/js/**", "/css/**").permitAll()
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin/abc").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERUSER")
                        // hasRole() : 역할 하나만 부여
                        // hasAnyRole() : 역할 여러개 부여

                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/hello")
                );
        return http.build();
    }

    // 실습. USER, ADMIN, SUPERUSER --> 3개의 ROLE이 존재한다고 가정
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder){
        UserDetails user = User.withUsername("user")
                .password(passwordEncoder.encode("1234")) // 원하는 패스워드를 넣음
                .roles("USER")
                .build();

        UserDetails user2 = User.withUsername("Gordon")
                .password(passwordEncoder.encode("1234")) // 원하는 패스워드를 넣음
                .roles("USER", "ADMIN")
                .build();

        UserDetails user3 = User.withUsername("admin")
                .password(passwordEncoder.encode("1234"))
                .roles("ADMIN")
                .build();

        UserDetails user4 = User.withUsername("superuser")
                .password(passwordEncoder.encode("1234"))
                .roles("SUPERUSER")
                .build();

        return new InMemoryUserDetailsManager(user, user2, user3, user4);
    }

    // 비밀번호 인코딩
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}