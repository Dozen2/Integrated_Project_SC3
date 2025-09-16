package sit.int221.sc3_server.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sit.int221.sc3_server.exception.crudException.CreateFailedException;
import sit.int221.sc3_server.exception.crudException.DeleteFailedException;
import sit.int221.sc3_server.exception.crudException.ItemNotFoundException;
import sit.int221.sc3_server.exception.crudException.UpdateFailedException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Object> handleItemNotFoundException(ItemNotFoundException exception , HttpServletRequest request){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                exception.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ger);
    }

    @ExceptionHandler(CreateFailedException.class)
    public ResponseEntity<Object> handleInternalException(Exception e,HttpServletRequest httpServletRequest){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Sale item create failed",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
    }

    @ExceptionHandler(UpdateFailedException.class)
    public ResponseEntity<Object> handleUpdateFailedException(Exception e,HttpServletRequest httpServletRequest){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Sale item updated failed",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ger);
    }

    @ExceptionHandler(DeleteFailedException.class)
    public ResponseEntity<Object> handleDeleteFailedException(Exception e,HttpServletRequest httpServletRequest){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "brand delete failed",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
    }

    @ExceptionHandler(PageNotFoundException.class)
    public ResponseEntity<Object> handlePageNotFoundException(Exception e ,HttpServletRequest httpServletRequest){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Required parameter 'page' is not present.",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
    }
    @ExceptionHandler(DuplicteItemException.class)
    public ResponseEntity<Object> handleDuplication(Exception e,HttpServletRequest httpServletRequest){
        GeneralErrorResponse ger = new GeneralErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Duplicate item",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ger);
    }

    @ExceptionHandler(UnAuthorizeException.class)
    public ResponseEntity<Object> handleUnAuthorizeRequest(Exception e,HttpServletRequest httpServletRequest){
        GeneralErrorResponse generalErrorResponse = new GeneralErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "UnAuthorize request",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(generalErrorResponse);
    }

    @ExceptionHandler(UnAuthenticateException.class)
    public ResponseEntity<Object> handleUnAuthenticateRequest(Exception e,HttpServletRequest httpServletRequest){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "UnAuthentication request",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ger);
    }
}
