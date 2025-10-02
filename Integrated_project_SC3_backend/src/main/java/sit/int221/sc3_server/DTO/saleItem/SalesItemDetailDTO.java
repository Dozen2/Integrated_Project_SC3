package sit.int221.sc3_server.DTO.saleItem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;


@Data
@NoArgsConstructor
public class SalesItemDetailDTO {
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
    @Min(0)
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    private String fileName;
    private Integer sellerId;
    private Instant createdOn;
    private Instant updatedOn;
//    // 🔹 เอาเฉพาะชื่อไฟล์รูปหลัก (imageViewOrder = 1)
    private String mainImageFileName;

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
