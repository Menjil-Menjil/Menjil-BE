package seoultech.capstone.menjil.domain.main.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.auth.domain.User;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
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
    private List<String> lastAnsweredMessages; // 가장 최근에 답변한 질문(최대 2개)

    public static UserInfoResponse fromUserEntity(User user) {
        return new UserInfoResponse(user.getNickname(), user.getMajor(), user.getCompany(),
                user.getField(), user.getTechStack(), user.getImgUrl(), null);
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setLastAnsweredMessage(List<String> lastAnsweredMessages) {
        this.lastAnsweredMessages = lastAnsweredMessages;
    }
}
