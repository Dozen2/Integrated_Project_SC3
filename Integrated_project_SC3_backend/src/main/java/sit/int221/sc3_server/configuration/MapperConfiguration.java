package sit.int221.sc3_server.configuration;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemImageDTO;
import sit.int221.sc3_server.DTO.saleItem.SalesItemDetailDTO;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.entity.SaleItemImage;

import java.util.Set;

@Configuration
public class MapperConfiguration {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.typeMap(SaleItemImage.class, SaleItemImageDTO.class);


        // Converter: Set<SaleItemImage> -> String
        Converter<Set<SaleItemImage>, String> mainImageConverter = ctx -> {
            if (ctx.getSource() == null) return null;
            return ctx.getSource().stream()
                    .filter(img -> img.getImageViewOrder() != null && img.getImageViewOrder() == 1)
                    .map(SaleItemImage::getFileName)
                    .findFirst()
                    .orElse(null);
        };

        mapper.typeMap(SaleItem.class, SalesItemDetailDTO.class).addMappings(m ->
                m.using(mainImageConverter).map(SaleItem::getSaleItemImage, SalesItemDetailDTO::setMainImageFileName)
        );


        return mapper;
    }

}

