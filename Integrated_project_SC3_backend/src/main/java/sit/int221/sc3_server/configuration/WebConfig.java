package sit.int221.sc3_server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration

public class WebConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/itb-mshop/v2/user/register","/itb-mshop/v2/**","/itb-mshop/v1/**").permitAll()
//                        .requestMatchers(HttpMethod.DELETE, "/itb-mshop/v2/sale-items/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/itb-mshop/v2/sale-items/**").permitAll()
//                        .requestMatchers(HttpMethod.PUT, "/itb-mshop/v2/sale-items/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

}
