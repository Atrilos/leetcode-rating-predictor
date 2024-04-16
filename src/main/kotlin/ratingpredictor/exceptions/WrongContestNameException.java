package ratingpredictor.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WrongContestNameException extends RuntimeException {
    public WrongContestNameException(String message) {
        super(message);
        log.error(message);
    }
}
