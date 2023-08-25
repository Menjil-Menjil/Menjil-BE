package seoultech.capstone.menjil.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlaskRequestDto {

    @JsonProperty(value = "mentee_nickname")
    private String menteeNickname;

    @JsonProperty(value = "mentor_nickname")
    private String mentorNickname;

    @JsonProperty(value = "question_origin")
    private String originMessage;

    @JsonProperty(value = "question_summary")
    private String threeLineSummaryMessage;

    public static FlaskRequestDto of(String menteeNickname, String mentorNickname,
                                     String originMessage, String threeLineSummaryMessage) {
        return new FlaskRequestDto(menteeNickname, mentorNickname,
                originMessage, threeLineSummaryMessage);
    }
}
