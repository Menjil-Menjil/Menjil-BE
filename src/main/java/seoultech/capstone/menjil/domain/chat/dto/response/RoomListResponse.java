package seoultech.capstone.menjil.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomListResponse {

    private String nickname;
    private String lastMessage;

    @Builder
    private RoomListResponse(String nickname, String lastMessage) {
        this.nickname = nickname;
        this.lastMessage = lastMessage;
    }
}
