package seoultech.capstone.menjil.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageClickIncViewsAndLikesResponse {

    private String questionId;
    private Long views;
    private Long likes;

    public static MessageClickIncViewsAndLikesResponse of(String questionId, Long views, Long likes) {
        return new MessageClickIncViewsAndLikesResponse(questionId, views, likes);
    }
}
