package sit.int221.sc3_server;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import sit.int221.sc3_server.configuration.FileStorageProperties;
import sit.int221.sc3_server.utils.ListMapper;

@EnableConfigurationProperties({FileStorageProperties.class})
@SpringBootApplication
@EnableAsync
public class Sc3ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(Sc3ServerApplication.class, args);
	}

	@Bean
	public ListMapper listMapper(){
		return ListMapper.getInstance();
	}
//	@Bean
//	public ModelMapper modelMapper() {
//		return new ModelMapper();
//	}
}
