package sit.int221.sc3_server.exception.crudException;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.CONFLICT)
public class OutOfStockException extends RuntimeException{
    public OutOfStockException(String message){
        super(message);
    }

}
