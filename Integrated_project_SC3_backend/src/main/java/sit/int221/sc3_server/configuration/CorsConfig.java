package sit.int221.sc3_server.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowedOrigins}")
    private String[] allowedOrigins;

    @Value("${app.cors.enabled:true}")
    private boolean corsEnabled;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsEnabled) {
            System.out.println("✅ [CorsConfig] addCorsMappings enabled");
            System.out.println("✅ [CorsConfig] Allowed Origins from addCorsMappings: " + Arrays.toString(allowedOrigins));

            registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        } else {
            System.out.println("⚠️ [CorsConfig] CORS disabled in application.properties");
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("✅ [CorsConfig] Creating CorsConfigurationSource bean...");
        System.out.println("✅ [CorsConfig] CORS Enabled: " + corsEnabled);
        System.out.println("✅ [CorsConfig] Allowed Origins: " + Arrays.toString(allowedOrigins));

        CorsConfiguration configuration = new CorsConfiguration();

        if (corsEnabled) {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(List.of("*"));
            configuration.setAllowCredentials(true);
            configuration.setMaxAge(3600L);
            configuration.setExposedHeaders(List.of("Set-cookie"));
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}