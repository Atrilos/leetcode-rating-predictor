package ratingpredictor.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserDataDto implements Serializable {
    String username;
    Double currentRating;
    Double expectedRating;
    Integer attendedContestsCount;
}
