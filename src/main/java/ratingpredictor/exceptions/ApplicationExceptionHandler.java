package ratingpredictor.exceptions;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(WrongContestNameException.class)
    public void handleUserAlreadyExistsException(WrongContestNameException ex,
                                                 HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public void handleTooManyRequestsException(TooManyRequestsException ex,
                                                 HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage());
    }

    @ExceptionHandler(NetworkException.class)
    public void handleNetworkException(NetworkException ex,
                                               HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler(WrongRegionException.class)
    public void handleWrongRegionException(WrongRegionException ex,
                                               HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }
}
