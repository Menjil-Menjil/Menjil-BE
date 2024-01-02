package seoultech.capstone.menjil.domain.chatbot.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatBotRoomResponse {

    private String roomId;
    private String recipientNickname;
    private String imgUrl;

    public static ChatBotRoomResponse of(String roomId, String recipientNickname, String imgUrl) {
        return new ChatBotRoomResponse(roomId, recipientNickname, imgUrl);
    }
}
