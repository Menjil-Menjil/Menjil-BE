package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AwsLambdaResponse {

    @JsonProperty(value = "question_id")
    private String question_id;         // use underbar because mongodb saved type and return value is different

    @JsonProperty(value = "question_summary")
    private String question_summary;    // use underbar because mongodb saved type and return value is different

    @JsonProperty(value = "answer")
    private String answer;

    @JsonProperty(value = "similarity_percent")
    private Double similarity_percent;     // use underbar because mongodb saved type and return value is different

    public static AwsLambdaResponse of(String question_id, String question_summary,
                                       String answer, Double similarity_percent) {
        return new AwsLambdaResponse(question_id, question_summary, answer, similarity_percent);
    }
}
