package sit.int221.sc3_server.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SaleItemImageRequest {
    private Integer imageViewOrder;
    private String fileName;
    private MultipartFile imageFile;
    /////
}
