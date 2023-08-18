package seoultech.capstone.menjil.domain.main.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfo;
import seoultech.capstone.menjil.domain.main.dto.response.UserInfo;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserInfoResponseDto {

    private UserInfo userInfo;
    private List<RoomInfo> roomInfoList;

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setRoomInfoList(List<RoomInfo> roomInfoList) {
        this.roomInfoList = roomInfoList;
    }
}
