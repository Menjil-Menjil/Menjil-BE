package seoultech.capstone.menjil.domain.chat.domain;

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

    @Field("mentee_nickname")
    private String menteeNickname;

    @Field("mentor_nickname")
    private String mentorNickname;

    @Field("question_origin")
    private String questionOrigin;

    @Field("question_summary")
    private String questionSummary;

    @Field("question_summary_en")
    private String questionSummaryEn;

    @Field("question_time")
    private LocalDateTime questionTime;

    @Field("answer")
    private String answer;

    @Field("answer_time")
    private LocalDateTime answerTime;

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
}
