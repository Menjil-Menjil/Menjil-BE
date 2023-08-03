package seoultech.capstone.menjil.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FlaskRequestDto {

    @JsonProperty(value = "mentee_nickname")
    private String menteeNickname;

    @JsonProperty(value = "mentor_nickname")
    private String mentorNickname;

    @JsonProperty(value = "question_origin")
    private String originMessage;

    @JsonProperty(value = "question_summary")
    private String threeLineSummaryMessage;

    @Builder
    private FlaskRequestDto(String menteeNickname, String mentorNickname,
                            String originMessage, String threeLineSummaryMessage) {
        this.menteeNickname = menteeNickname;
        this.mentorNickname = mentorNickname;
        this.originMessage = originMessage;
        this.threeLineSummaryMessage = threeLineSummaryMessage;
    }
}
