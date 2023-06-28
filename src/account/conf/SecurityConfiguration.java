package account.conf;

import account.business.Log;
import account.persistence.LogRepository;
import account.present.CustomAuthenticationProvider;
import account.present.CustomErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    private final LogRepository logRepository;

    public SecurityConfiguration(AuthenticationEntryPoint restAuthenticationEntryPoint, LogRepository logRepository) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.logRepository = logRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/auth/signup").permitAll()
                        .requestMatchers("/api/auth/changepass").authenticated()
                        .requestMatchers("/api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
                        .requestMatchers("/api/acct/payments").hasAnyRole("ACCOUNTANT")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRATOR")
                        .requestMatchers("/api/security/events/").hasAnyRole("AUDITOR")
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, CustomAuthenticationProvider customAuthenticationProvider) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(customAuthenticationProvider)
                .build();
    }


    @Bean
    public PasswordEncoder getEncoder() {
        return NoOpPasswordEncoder.getInstance();
//        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public Set<String> breachedPasswords() {
        return Set.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ServletOutputStream out = response.getOutputStream();
            new ObjectMapper().writeValue(out, new CustomErrorMessage(LocalDateTime.now().toString(),
                    HttpStatus.FORBIDDEN.value(), "Forbidden", "Access Denied!", request.getRequestURI()));
            out.flush();
            String loginPassword = new String(Base64.getDecoder().decode(request.getHeader("Authorization")
                    .substring(6)));
            String login = loginPassword.substring(0, loginPassword.indexOf(':'));
            logRepository.save(new Log(null, LocalDateTime.now().toString(), "ACCESS_DENIED",
                    login, request.getRequestURI(), request.getRequestURI()));
        };
    }
}
