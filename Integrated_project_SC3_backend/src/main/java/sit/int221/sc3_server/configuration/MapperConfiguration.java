package sit.int221.sc3_server.configuration;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sit.int221.sc3_server.DTO.order.OrderItems;
import sit.int221.sc3_server.DTO.order.OrderRequest;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemDetailFileDto;
import sit.int221.sc3_server.DTO.saleItem.file.SaleItemImageDTO;
import sit.int221.sc3_server.DTO.saleItem.SalesItemDetailDTO;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SaleItemDetailSeller;
import sit.int221.sc3_server.DTO.saleItem.sellerSaleItem.SellerDTO;
import sit.int221.sc3_server.entity.Order;
import sit.int221.sc3_server.entity.OrderDetail;
import sit.int221.sc3_server.entity.SaleItem;
import sit.int221.sc3_server.entity.SaleItemImage;

import java.util.Collection;
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

        // Converter: SaleItem -> String (main image filename)
        Converter<SaleItem, String> saleItemMainImageConverter = ctx -> {
            if (ctx.getSource() == null || ctx.getSource().getSaleItemImage() == null) return null;
            return ctx.getSource().getSaleItemImage().stream()
                    .filter(img -> img.getImageViewOrder() != null && img.getImageViewOrder() == 1)
                    .map(SaleItemImage::getFileName)
                    .findFirst()
                    .orElse(null);
        };


        mapper.typeMap(SaleItem.class, SalesItemDetailDTO.class).addMappings(m ->
                m.using(mainImageConverter).map(SaleItem::getSaleItemImage, SalesItemDetailDTO::setMainImageFileName)
        );

        mapper.typeMap(OrderDetail.class, OrderItems.class)
                .addMappings(m -> {
                    m.map(OrderDetail::getId, OrderItems::setNo);
                    m.map(od -> od.getSaleItem().getId(), OrderItems::setSaleItemId);
                    m.map(OrderDetail::getPriceEachAtPurchase, OrderItems::setPrice);
                    m.map(OrderDetail::getQuantity, OrderItems::setQuantity);
                    m.map(od -> od.getSaleItem().getDescription(), OrderItems::setDescription);
                    m.using(saleItemMainImageConverter).map(OrderDetail::getSaleItem,OrderItems::setMainImageFileName);
                });

        // ---- Order -> OrderRequest DTO ----
        mapper.typeMap(Order.class, OrderRequest.class)
                .addMappings(m -> {
                    m.map(o -> o.getBuyer().getId(), OrderRequest::setBuyerId);
                    m.map(o -> o.getSeller().getId(), OrderRequest::setSellerId);
                    m.map(Order::getOrderDate, OrderRequest::setOrderDate);
                    m.map(Order::getShippingAddress, OrderRequest::setShippingAddress);
                    m.map(Order::getOrderNote, OrderRequest::setOrderNote);
                    m.map(Order::getOrderStatus, OrderRequest::setOrderStatus);
                    // map Set<OrderDetail> → List<OrderItems>
                    m.using(ctx -> {
                        if (ctx.getSource() == null) return null;
                        Collection<?> src = (Collection<?>) ctx.getSource();
                        return src.stream()
                                .map(od -> mapper.map(od, OrderItems.class))
                                .toList();
                    }).map(Order::getOrderDetails, OrderRequest::setOrderItems);
                });





        return mapper;
    }

}

