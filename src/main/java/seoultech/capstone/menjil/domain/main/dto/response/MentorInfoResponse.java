package seoultech.capstone.menjil.domain.main.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.User;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MentorInfoResponse {
    /**
     * 메인 화면에 보여질 멘토의 정보를 담는 DTO.
     * User Entity로부터 정보를 가져온다.
     */
    private String nickname;
    private String major;       // 본전공
    private String company;     // 재직 중인 회사
    private String field;       // 관심 분야
    private String techStack;   // 기술 스택
    private String imgUrl;
    private String lastAnsweredMessage; // 가장 최근에 답변한 질문 <- 이 부분은 추후 개발 예정

    public static MentorInfoResponse from(User user) {
        return new MentorInfoResponse(user.getNickname(), user.getMajor(), user.getCompany(),
                user.getField(), user.getTechStack(), null, null);
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setLastAnsweredMessage(String lastAnsweredMessage) {
        this.lastAnsweredMessage = lastAnsweredMessage;
    }
}
