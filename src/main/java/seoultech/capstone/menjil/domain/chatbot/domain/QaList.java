package seoultech.capstone.menjil.domain.chatbot.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Document(collection = "qa_list")
public class QaList {

    @Id
    private String _id;

    @Field(name = "mentee_nickname")
    private String menteeNickname;

    @Field(name = "mentor_nickname")
    private String mentorNickname;

    @Field(name = "question_origin")
    private String questionOrigin;

    @Field(name = "question_summary")
    private String questionSummary;

    @Field(name = "question_summary_en")
    private String questionSummaryEn;

    @Field(name = "question_time")
    private LocalDateTime questionTime;

    @Field(name = "answer")
    private String answer;

    @Field(name = "answer_time")
    private LocalDateTime answerTime;

    @Field(name = "views")
    private Long views;

    @Field(name = "likes")
    private Long likes;

    @Builder
    private QaList(String menteeNickname, String mentorNickname, String questionOrigin,
                   String questionSummary, String questionSummaryEn, LocalDateTime questionTime,
                   String answer, LocalDateTime answerTime) {
        this.menteeNickname = menteeNickname;
        this.mentorNickname = mentorNickname;
        this.questionOrigin = questionOrigin;
        this.questionSummary = questionSummary;
        this.questionSummaryEn = questionSummaryEn;
        this.questionTime = questionTime;
        this.answer = answer;
        this.answerTime = answerTime;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }
}
