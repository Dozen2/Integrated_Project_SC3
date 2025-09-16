package sit.int221.sc3_server.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UnAuthenticateException extends RuntimeException {

    public UnAuthenticateException(String message){
        super(message);
    }
}
