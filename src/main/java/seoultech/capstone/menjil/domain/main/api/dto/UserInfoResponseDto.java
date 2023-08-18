package seoultech.capstone.menjil.domain.main.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.capstone.menjil.domain.chat.dto.response.RoomInfoDto;
import seoultech.capstone.menjil.domain.main.dto.response.UserInfoDto;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserInfoResponseDto {

    private UserInfoDto userInfoDto;
    private List<RoomInfoDto> roomInfoDtoList;

    public void setUserInfoDto(UserInfoDto userInfoDto) {
        this.userInfoDto = userInfoDto;
    }

    public void setRoomInfoDtoList(List<RoomInfoDto> roomInfoDtoList) {
        this.roomInfoDtoList = roomInfoDtoList;
    }
}
