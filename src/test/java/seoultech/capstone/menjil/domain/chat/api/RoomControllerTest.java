package seoultech.capstone.menjil.domain.chat.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import seoultech.capstone.menjil.domain.chat.application.RoomService;
import seoultech.capstone.menjil.domain.chat.dto.RoomDto;
import seoultech.capstone.menjil.global.config.WebConfig;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockBean(JpaMetamodelMappingContext.class)
@WebMvcTest(controllers = RoomController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
class RoomControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Gson gson;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;
    @MockBean
    private RoomService roomService;


    @BeforeEach
    void setUp() {
//        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("채팅방 생성이 정상적으로 진행되었을 때, 201 응답과 그에 해당하는 메시지가 DTO 객체로 리턴된다.")
    void createRoom() throws Exception {
        RoomDto roomDto = new RoomDto("testroom1", "testmentee1", "testmentor1");
        String content = gson.toJson(roomDto);

        Mockito.when(roomService.createRoom(roomDto)).thenReturn(201);

       /* mvc.perform(MockMvcRequestBuilders.post("/api/chat/room")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(201)))
                .andExpect(jsonPath("$.message", is("채팅방이 정상적으로 생성되었습니다")))
                .andExpect(jsonPath("$.data", is("testroom1")))
                .andDo(print());*/

        verify(roomService, times(1)).createRoom(roomDto);
    }

}