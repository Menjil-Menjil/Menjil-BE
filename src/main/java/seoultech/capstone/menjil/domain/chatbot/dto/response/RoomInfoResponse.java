package seoultech.capstone.menjil.domain.chatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoResponse {

    private String roomId;
    private String nickname;
    private String imgUrl;

    public static RoomInfoResponse of(String roomId, String nickname, String imgUrl) {
        return new RoomInfoResponse(roomId, nickname, imgUrl);
    }
}
