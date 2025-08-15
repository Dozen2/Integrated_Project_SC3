package sit.int221.sc3_server.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaleItemImageDTO {

    private String fileName;
    private Integer imageViewOrder;


}
