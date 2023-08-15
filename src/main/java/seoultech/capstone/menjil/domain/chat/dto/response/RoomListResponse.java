package seoultech.capstone.menjil.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomListResponse {

    private String roomId;

    private String nickname;
    private String lastMessage;

    @Builder
    private RoomListResponse(String roomId, String nickname, String lastMessage) {
        this.roomId = roomId;
        this.nickname = nickname;
        this.lastMessage = lastMessage;
    }
}
