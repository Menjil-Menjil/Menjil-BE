package seoultech.capstone.menjil.domain.chatbot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageClickIncViewsAndLikesRequest {

    @NotBlank
    private String Id;

    @NotBlank
    private String questionId;

    @NotBlank
    private Boolean likeStatus;
}
