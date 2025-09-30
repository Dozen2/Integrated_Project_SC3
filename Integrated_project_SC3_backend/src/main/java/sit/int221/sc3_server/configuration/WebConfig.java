package sit.int221.sc3_server.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sit.int221.sc3_server.exception.AuthenticationEntryPoint.JwtAccessDeniedHandler;
import sit.int221.sc3_server.exception.AuthenticationEntryPoint.JwtAuthenticationEntryPoint;
import sit.int221.sc3_server.filter.JwtAuthFilter;
import sit.int221.sc3_server.service.Authentication.JwtUserDetailService;

@Configuration
@EnableWebSecurity
public class WebConfig {
    @Autowired
    private JwtUserDetailService jwtUserDetailService;
    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/itb-mshop/v2/**","/itb-mshop/v1/**").permitAll()
//                        .requestMatchers("/itb-mshop/v2/auth/register","/itb-mshop/v2/auth/verify-email"
//                        ,"/itb-mshop/v2/auth/refresh-email-token").permitAll()
//                        .requestMatchers("/itb-mshop/v1/brands/**","/itb-mshop/v2/sale-items","/itb-mshop/v2/sale-items/file/{filename:.+}").permitAll()
//                        .requestMatchers("/itb-mshop/v2/auth/login","/itb-mshop/v2/auth/logout").permitAll()
//                        .requestMatchers(
//                        "/itb-mshop/v2/user/{id}","/itb-mshop/v2/user/profile/all").hasAnyAuthority("ROLE_BUYER","ROLE_SELLER")
//                        .requestMatchers("/itb-mshop/v2/sellers/**").authenticated()

//                        .requestMatchers("/itb-mshop/v2/**","/itb-mshop/v1/**").permitAll()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider(jwtUserDetailService))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex->ex.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.headers(httpSecurityHeadersConfigurer ->
//                httpSecurityHeadersConfigurer.frameOptions(frameOptionsConfig ->
//                        frameOptionsConfig.disable()));
//        http.csrf(crsf -> crsf.disable())
//                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/authentications/**", "/h2/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/users/groups").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/users").hasAnyAuthority("MANAGER", "STAFF")
//                        .requestMatchers("/api/resources/**").not().hasAuthority("GUEST")
//                        .anyRequest().authenticated()
//
//                )
//                .authenticationProvider(authenticationProvider(jwtUserDetailsService))
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(ex->ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//        return http.build();
//    }
    @Bean
    public AuthenticationProvider authenticationProvider(JwtUserDetailService jwtUserDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(jwtUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

}
