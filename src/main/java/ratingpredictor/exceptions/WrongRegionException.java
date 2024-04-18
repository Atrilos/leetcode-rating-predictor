package ratingpredictor.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WrongRegionException extends RuntimeException{
    public WrongRegionException(String message) {
        super(message);
        log.error(message);
    }
}
