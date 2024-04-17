package ratingpredictor.dto;

import lombok.Builder;
import lombok.Value;
import ratingpredictor.model.Participant;

import java.io.Serializable;

/**
 * DTO for {@link Participant}
 */
@Value
@Builder
public class ParticipantDto implements Serializable {
    String contestName;
    String username;
    String region;
    Integer rank;
    Integer score;
    Double finishTime;
}