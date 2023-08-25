package seoultech.capstone.menjil.domain.chat.dto.response;

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
    private String lastMessage;

    private Long lastMessagedTimeOfHour;

    public static RoomInfoResponse of(String roomId, String nickname, String imgUrl, String lastMessage,
                                      Long lastMessagedTimeOfHour) {
        return new RoomInfoResponse(roomId, nickname, imgUrl, lastMessage, lastMessagedTimeOfHour);
    }
}
