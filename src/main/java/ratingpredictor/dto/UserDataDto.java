package ratingpredictor.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Value
@Builder
public class UserDataDto implements Serializable {
    String username;
    Double currentRating;
    Integer attendedContestsCount;
}
