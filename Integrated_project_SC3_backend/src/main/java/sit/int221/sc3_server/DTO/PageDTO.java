package sit.int221.sc3_server.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageDTO<T> {
    private List<T> content;
    private Boolean last;
    private Boolean first;
    private Integer totalPages;
    private Integer totalElements;
    private Integer size;
    private String sort;
    @JsonIgnore
    private Integer number;

    public Integer getPage() {
        return number;
    }


//    public void setSort(String sort) {
//        this.sort = "Hello eiei";
//    }
}
