package seoultech.capstone.menjil.domain.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.domain.ChatMessage;
import seoultech.capstone.menjil.domain.chat.domain.MessageType;
import seoultech.capstone.menjil.domain.chat.domain.SenderType;

@Getter
@NoArgsConstructor
public class MessagesResponse {
    /**
     * 기존의 채팅 내역이 존재하는 경우, 채팅 내역들을 클라이언트로 보낼 때 사용하는 Response DTO
     * _id 값도 같이 전달하므로, MessageDto를 사용하지 않음
     */

    private String _id; // This is different from MessageDto
    private String roomId;
    private SenderType senderType;
    private String senderNickname;
    private String message;
    private MessageType messageType;
    private String time;

    @Builder
    private MessagesResponse(String _id, String roomId, SenderType senderType,
                             String senderNickname, String message,
                             MessageType messageType, String time) {
        this._id = _id;
        this.roomId = roomId;
        this.senderType = senderType;
        this.senderNickname = senderNickname;
        this.message = message;
        this.messageType = messageType;
        this.time = time;
    }

    public static MessagesResponse fromMessage(ChatMessage chatMessage) {

        // remove nano seconds
        String time = chatMessage.getTime().withNano(0).toString();
        time = time.replace("T", " ");

        return MessagesResponse.builder()
                ._id(chatMessage.get_id())
                .roomId(chatMessage.getRoomId())
                .senderType(chatMessage.getSenderType())
                .senderNickname(chatMessage.getSenderNickname())
                .message(chatMessage.getMessage())
                .messageType(chatMessage.getMessageType())
                .time(time)
                .build();
    }

}
