package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FlaskResponseDto {
    @JsonProperty(value = "question_summary")
    private String threeLineSummaryMessage;

    @JsonProperty(value = "answer")
    private String answer;

    @Builder
    private FlaskResponseDto(String threeLineSummaryMessage, String answer) {
        this.threeLineSummaryMessage = threeLineSummaryMessage;
        this.answer = answer;
    }
}
