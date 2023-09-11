package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AwsLambdaResponse {
    @JsonProperty(value = "question_summary")
    private String question_summary;

    @JsonProperty(value = "answer")
    private String answer;

    @JsonProperty(value = "similarity_percent")
    private Double similarityPercent;

    public static AwsLambdaResponse of(String question_summary, String answer, Double similarityPercent) {
        return new AwsLambdaResponse(question_summary, answer, similarityPercent);
    }

}
