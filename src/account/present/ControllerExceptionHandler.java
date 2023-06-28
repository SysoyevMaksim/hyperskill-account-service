package account.present;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorMessage> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, WebRequest request) {
        String message = e.getMessage();
        int index = message.indexOf("default message", message.indexOf("default message") + 1);
        CustomErrorMessage body = new CustomErrorMessage(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message.substring(message.indexOf('[', index) + 1, message.indexOf(']', index)),
                request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomErrorMessage> handleConstraintValidation(
            ConstraintViolationException e, WebRequest request) {
        String message = e.getMessage();
        CustomErrorMessage body = new CustomErrorMessage(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getDescription(false).substring(4));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<CustomErrorMessage> handleAuthentication(
//            AuthenticationException e, WebRequest request) {
//        String message = e.getMessage();
//        CustomErrorMessage body = new CustomErrorMessage(
//                LocalDateTime.now().toString(),
//                HttpStatus.UNAUTHORIZED.value(),
//                "Unauthorized",
//                message,
//                request.getDescription(false).substring(4));
//        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
//    }
}
