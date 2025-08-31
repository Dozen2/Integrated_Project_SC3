package sit.int221.sc3_server.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnAuthorizeException extends RuntimeException{
    public  UnAuthorizeException(String message){
        super(message);
    }
}
