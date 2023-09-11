package seoultech.capstone.menjil.domain.chat.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AwsLambdaRequest {

    @JsonProperty(value = "mentee_nickname")
    private String menteeNickname;

    @JsonProperty(value = "mentor_nickname")
    private String mentorNickname;

    @JsonProperty(value = "question_origin")
    private String originMessage;

    @JsonProperty(value = "question_summary")
    private String questionSummary;

    public static AwsLambdaRequest of(String menteeNickname, String mentorNickname,
                                      String originMessage, String questionSummary) {
        return new AwsLambdaRequest(menteeNickname, mentorNickname,
                originMessage, questionSummary);
    }
}
