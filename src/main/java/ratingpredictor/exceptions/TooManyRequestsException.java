package ratingpredictor.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
        log.error(message);
    }
}
