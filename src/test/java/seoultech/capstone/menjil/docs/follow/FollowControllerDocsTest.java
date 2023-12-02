package seoultech.capstone.menjil.docs.follow;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import seoultech.capstone.menjil.docs.RestDocsSupport;
import seoultech.capstone.menjil.domain.follow.api.FollowController;
import seoultech.capstone.menjil.domain.follow.api.dto.request.FollowCreateRequest;
import seoultech.capstone.menjil.domain.follow.application.FollowService;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;
import seoultech.capstone.menjil.global.exception.SuccessCode;

import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static seoultech.capstone.menjil.global.exception.SuccessIntValue.FOLLOW_CREATED;
import static seoultech.capstone.menjil.global.exception.SuccessIntValue.FOLLOW_DELETED;

@WebMvcTest(controllers = FollowController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
public class FollowControllerDocsTest extends RestDocsSupport {

    @MockBean
    private FollowService followService;

    @Autowired
    private Gson gson;

    private final String TEST_USER_NICKNAME = "user_nickname33";
    private final String TEST_FOLLOW_NICKNAME = "follow_nickname33";

    @Test
    @DisplayName("case 1: 팔로우가 생성된 경우")
    void followRequest_follow_created() throws Exception {
        // given
        FollowCreateRequest followCreateRequest = FollowCreateRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        String content = gson.toJson(followCreateRequest);

        // when
        Mockito.when(followService.createFollow(followCreateRequest.toServiceRequest())).thenReturn(FOLLOW_CREATED.getValue());

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/follow/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_CREATED.getCode())))
                .andDo(print())
                .andDo(document("api/follow/create/201/true",
                        requestFields(
                                fieldWithPath("userNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("followNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 팔로우 요청을 보내는 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("case 2: 팔로우가 제거된 경우")
    void followRequest_follow_deleted() throws Exception {
        // given
        FollowCreateRequest followCreateRequest = FollowCreateRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        String content = gson.toJson(followCreateRequest);

        // when
        Mockito.when(followService.createFollow(followCreateRequest.toServiceRequest())).thenReturn(FOLLOW_DELETED.getValue());

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/follow/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_DELETED.getCode())))
                .andDo(print())
                .andDo(document("api/follow/create/201/false",
                        requestFields(
                                fieldWithPath("userNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("followNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 팔로우 요청을 보내는 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("case 3: 서버 오류")
    void followRequest_INTERNAL_SERVER_ERROR() throws Exception {
        // given
        FollowCreateRequest followCreateRequest = FollowCreateRequest.of(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME);
        String content = gson.toJson(followCreateRequest);

        // when
        Mockito.when(followService.createFollow(followCreateRequest.toServiceRequest()))
                .thenThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/follow/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code", is(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus().value())))
                .andDo(print())
                .andDo(document("api/follow/create/500",
                        requestFields(
                                fieldWithPath("userNickname").type(JsonFieldType.STRING)
                                        .description("사용자 닉네임"),
                                fieldWithPath("followNickname").type(JsonFieldType.STRING)
                                        .description("사용자가 팔로우 요청을 보내는 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("case 1: follow가 이미 존재하는 경우")
    void checkFollowStatus_return_true() throws Exception {
        // given
        boolean FOLLOW_EXISTS = true;

        // when
        Mockito.when(followService.checkFollowStatus(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME)).thenReturn(FOLLOW_EXISTS);

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/follow/check-status/")
                        .queryParam("userNickname", TEST_USER_NICKNAME)
                        .queryParam("followNickname", TEST_FOLLOW_NICKNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_CHECK_SUCCESS.getCode())))
                .andDo(print())
                .andDo(document("api/follow/check-status/true",
                        requestParameters(
                                parameterWithName("userNickname").description("사용자 닉네임"),
                                parameterWithName("followNickname").description("사용자가 팔로우 요청을 보내는 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("응답 데이터")
                        )
                ));
    }

    @Test
    @DisplayName("case 2: follow가 존재하지 않는 경우")
    void checkFollowStatus_return_false() throws Exception {
        // given
        boolean FOLLOW_NOT_EXISTS = false;

        // when
        Mockito.when(followService.checkFollowStatus(TEST_USER_NICKNAME, TEST_FOLLOW_NICKNAME)).thenReturn(FOLLOW_NOT_EXISTS);

        // then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/follow/check-status/")
                        .queryParam("userNickname", TEST_USER_NICKNAME)
                        .queryParam("followNickname", TEST_FOLLOW_NICKNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is(SuccessCode.FOLLOW_CHECK_SUCCESS.getCode())))
                .andDo(print())
                .andDo(document("api/follow/check-status/false",
                        requestParameters(
                                parameterWithName("userNickname").description("사용자 닉네임"),
                                parameterWithName("followNickname").description("사용자가 팔로우 요청을 보내는 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("응답 데이터")
                        )
                ));
    }
}
