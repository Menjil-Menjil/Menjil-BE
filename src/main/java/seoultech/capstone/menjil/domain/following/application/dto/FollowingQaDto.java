package seoultech.capstone.menjil.domain.following.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class FollowingQaDto {

    private String questionOrigin;
    private String questionSummary;
    private String answer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime answerTime;

    private Long views;
    private Long likes;

    @Builder
    public FollowingQaDto(String questionOrigin, String questionSummary, String answer,
                          LocalDateTime answerTime, Long views, Long likes) {
        this.questionOrigin = questionOrigin;
        this.questionSummary = questionSummary;
        this.answer = answer;
        this.answerTime = answerTime;
        this.views = views;
        this.likes = likes;
    }
}
