package ratingpredictor.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkException extends RuntimeException{

    public NetworkException(String message) {
        super(message);
        log.error(message);
    }
}
