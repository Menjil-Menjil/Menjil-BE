package seoultech.capstone.menjil.domain.chat.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import seoultech.capstone.menjil.domain.chat.application.RoomService;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.domain.chat.dto.response.MessagesResponse;
import seoultech.capstone.menjil.global.common.dto.ApiResponse;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import javax.validation.Valid;
import java.util.List;

import static seoultech.capstone.menjil.global.common.dto.ApiResponse.success;
import static seoultech.capstone.menjil.global.exception.SuccessCode.ROOM_CREATED;

@Slf4j
@RequiredArgsConstructor
@RestController
@EnableWebMvc
@RequestMapping("/api/chat")
public class RoomController {

    private final RoomService roomService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /* 새로운 방을 생성한다 */
    @PostMapping(value = "/room", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<?>> createRoom(@Valid @RequestBody RoomDto roomDto) {

        int result = roomService.createRoom(roomDto);
        String roomId = roomDto.getRoomId();

        if (result == HttpStatus.CREATED.value()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(success(ROOM_CREATED, roomId));
        } else {
            throw new CustomException(ErrorCode.SERVER_ERROR);
        }
    }

    /* room Id를 통해 방으로 입장한다 */
    @GetMapping("/room/enter/{roomId}")
    public void enterTheRoom(@PathVariable("roomId") String roomId) {
        List<MessagesResponse> messageList = roomService.enterTheRoom(roomId);

        ResponseEntity<ApiResponse<List<MessagesResponse>>> messageResponse;
        if (messageList.size() == 1) {
            messageResponse = ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(SuccessCode.MESSAGE_CREATED, messageList));
        } else {
            messageResponse = ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(SuccessCode.MESSAGE_LOAD_SUCCESS, messageList));
        }

        // /queue/chat/room/{room id}로 메세지 보냄
        simpMessagingTemplate.convertAndSend("/queue/chat/room/" + roomId, messageResponse);
    }


    /* room Id를 통해 방의 데이터를 조회한다 */
    @GetMapping("/room/{roomId}")
    public RoomDto getRoomInfo(@PathVariable("roomId") String roomId) {

        return null;
    }

    /* 사용자의 전체 Room 목록을 불러온다 */
    @GetMapping("/rooms")
    public List<RoomDto> getAllRooms() {
        return null;
    }

    /* 사용자가 방에서 퇴장한다 */
    public void quitRoom() {

    }

}
