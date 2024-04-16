package ratingpredictor.dto;

import lombok.Builder;
import lombok.Value;
import ratingpredictor.entity.ParticipantsWithCount;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * DTO for {@link ParticipantsWithCount}
 */
@Value
@Builder
public class ParticipantsWithCountDto implements Serializable {
    String contestName;
    Integer pageNum;
    @Builder.Default
    ArrayList<ParticipantDto> participants = new ArrayList<>();
    Integer userNum;
}