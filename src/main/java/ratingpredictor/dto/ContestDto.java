package ratingpredictor.dto;

import lombok.Builder;
import lombok.Value;
import ratingpredictor.model.Contest;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * DTO for {@link Contest}
 */
@Value
@Builder
public class ContestDto implements Serializable {
    String contestName;
    @Builder.Default
    ArrayList<ParticipantDto> participants = new ArrayList<>();
    Double time;

    @Serial
    private static final long serialVersionUID = 3023426368568569L;
}