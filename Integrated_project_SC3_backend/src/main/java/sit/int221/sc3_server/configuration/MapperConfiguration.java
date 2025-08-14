package sit.int221.sc3_server.configuration;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sit.int221.sc3_server.DTO.SaleItemImageDTO;
import sit.int221.sc3_server.entity.SaleItemImage;

@Configuration
public class MapperConfiguration {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.typeMap(SaleItemImage.class, SaleItemImageDTO.class);
        return mapper;
    }
}
