package sit.int221.sc3_server.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sit.int221.sc3_server.exception.crudException.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


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

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleUnAuthenticateRequest(Exception e,HttpServletRequest httpServletRequest){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "UnAuthentication request",
                e.getMessage(),
                httpServletRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ger);
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Object> handleOutOFStockException(Exception e,HttpServletRequest request){
        GeneralErrorResponse ger = new GeneralErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict request",
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ger);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");

        // ดึง message จากแต่ละ field ที่ invalid
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        body.put("message", errorMessage);
        body.put("path", request.getRequestURI());
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
