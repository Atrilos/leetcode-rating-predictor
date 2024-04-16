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
    Integer contestId;
    String username;
    Integer rank;
    Integer score;
    Long finishTime;
}