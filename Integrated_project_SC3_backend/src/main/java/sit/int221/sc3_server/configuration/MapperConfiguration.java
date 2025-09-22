package sit.int221.sc3_server.configuration;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemImageDTO;
import sit.int221.sc3_server.DTO.saleItem.SalesItemDetailDTO;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SaleItemDetailSeller;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SellerDTO;
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

//        mapper.typeMap(SaleItem.class, SaleItemDetailSeller.class).addMappings(m -> {
//            m.map(src -> src.getSeller().getId(),
//                    (dest, v) -> {
//                        if (dest.getSellerDTO() == null) dest.setSellerDTO(new SellerDTO());
//                        dest.getSellerDTO().setId((Integer) v);
//                    });
//
//            //userName -> email
//            m.map(src -> {
//                if (src.getSeller() != null && src.getSeller().getBuyer() != null) {
//                    return src.getSeller().getBuyer().getEmail();
//                }
//                return null;
//            }, (dest, v) -> {
//                if (dest.getSellerDTO() == null) dest.setSellerDTO(new SellerDTO());
//                dest.getSellerDTO().setUserName((String) v);
//            });
//        });




        return mapper;
    }

}

