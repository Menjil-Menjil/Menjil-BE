package seoultech.capstone.menjil.domain.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoResponse {

    private String roomId;
    private String nickname;
    private String imgUrl;
    private String lastMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastMessageTime;

    public static RoomInfoResponse of(String roomId, String nickname, String imgUrl, String lastMessage,
                                      LocalDateTime lastMessageTime) {
        return new RoomInfoResponse(roomId, nickname, imgUrl, lastMessage, lastMessageTime);
    }
}
