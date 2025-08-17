package sit.int221.sc3_server.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int221.sc3_server.entity.SaleItemImage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class SaleItemDetailFileDto {
    private Integer id;
    private String model;
    private String brandName;
    @NotBlank(message = "Name is required and must not be blank")
    private String description;
    private Integer price;
    private Integer ramGb;
    private Integer storageGb;
    private BigDecimal screenSizeInch;
    private String color;
    private Set<SaleItemImageDTO> saleItemImage;
    @Min(0)
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    private Instant createdOn;
    private Instant updatedOn;

//    public List<String> getImageNames() {
//        return saleItemImages.stream()
//                .sorted(Comparator.comparing(SaleItemImage::getImageViewOrder))
//                .map(SaleItemImage::getFileName)
//                .collect(Collectors.toList());
//    }
    public void setColor(String color) {
        if (color != null && color.trim().isEmpty()) {
            this.color = null;
        } else if (color == null || color.trim().isEmpty()) {
            this.color = null;
        } else {
            this.color = color.trim();
        }
    }


}
