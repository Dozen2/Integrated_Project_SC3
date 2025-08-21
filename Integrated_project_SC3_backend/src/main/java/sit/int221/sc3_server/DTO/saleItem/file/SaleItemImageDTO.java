package sit.int221.sc3_server.DTO.saleItem.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaleItemImageDTO {

    private String fileName;
    private String originalFileName;
    private Integer imageViewOrder;



}
