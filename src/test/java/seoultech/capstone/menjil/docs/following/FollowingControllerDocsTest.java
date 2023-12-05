package seoultech.capstone.menjil.docs.following;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import seoultech.capstone.menjil.docs.RestDocsSupport;
import seoultech.capstone.menjil.domain.following.api.FollowingController;
import seoultech.capstone.menjil.domain.following.application.FollowingService;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingQaDto;
import seoultech.capstone.menjil.domain.following.application.dto.FollowingUserInfoDto;
import seoultech.capstone.menjil.domain.following.application.dto.response.FollowingUserInfoResponse;
import seoultech.capstone.menjil.global.config.WebConfig;
import seoultech.capstone.menjil.global.exception.CustomException;
import seoultech.capstone.menjil.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FollowingController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebConfig.class)
        })
public class FollowingControllerDocsTest extends RestDocsSupport {

        @MockBean
        private FollowingService followingService;

        @Autowired
        private Gson gson;

        @Test
        @DisplayName("case 1: 정상 로직] 사용자의 정보와 질문답변 내용이 모두 존재한다")
        void getFollowUserInfo() throws Exception {
                // given
                String followNickname = "userA";
                Long answersCount = 2L;
                List<FollowingQaDto> answers = new ArrayList<>();
                answers.add(FollowingQaDto.builder()
                        .questionOrigin("원본 질문1")
                        .questionSummary("원본 요약1")
                        .answer("답변1")
                        .answerTime(LocalDateTime.now())
                        .likes(2L)
                        .views(11L)
                        .build());
                answers.add(FollowingQaDto.builder()
                        .questionOrigin("원본 질문2")
                        .questionSummary("원본 요약2")
                        .answer("답변2")
                        .answerTime(LocalDateTime.now().plusSeconds(5000))
                        .likes(4L)
                        .views(10L)
                        .build());

                FollowingUserInfoResponse response = createTestFollowingUserInfoResponse(followNickname,
                        answersCount, answers);

                // when
                Mockito.when(followingService.getFollowUserInfo(followNickname)).thenReturn(response);

                // then
                mockMvc.perform(RestDocumentationRequestBuilders.get("/api/following/info")
                                .queryParam("followNickname", followNickname))
                        .andExpect(status().isOk())
                        .andDo(document("api/following/info/200-ok",
                                requestParameters(
                                        parameterWithName("followNickname").description("조회 요청을 보내는 닉네임")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                        fieldWithPath("message").type(STRING).description("응답 메시지"),
                                        fieldWithPath("data.followingUserInfoDto.nickname").description("닉네임"),
                                        fieldWithPath("data.followingUserInfoDto.company").description("재직 중인 회사. 회사 정보가 없는 경우 null이 될 수 있습니다."),
                                        fieldWithPath("data.followingUserInfoDto.field").description("관심 분아. 여러 개면 ','로 구분] 프론트엔드, 백엔드, ..."),
                                        fieldWithPath("data.followingUserInfoDto.school").description("The school the user attended."),
                                        fieldWithPath("data.followingUserInfoDto.major").description("The major of the user."),
                                        fieldWithPath("data.followingUserInfoDto.subMajor").optional().description("복수전공. null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.minor").optional().description("부전공. null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.techStack").description("기술 스택. 여러 개면 ','로 구분"),
                                        fieldWithPath("data.followingUserInfoDto.imgUrl").description("프로필 img url"),
                                        fieldWithPath("data.followingUserInfoDto.career").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.certificate").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.awards").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.activity").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.answersCount").description("멘토의 답변 수"),
                                        fieldWithPath("data.answers[].questionOrigin").description("원본 질문"),
                                        fieldWithPath("data.answers[].questionSummary").description("ChatGPT로 원본 질문을 3줄 요약한 결과"),
                                        fieldWithPath("data.answers[].answer").description("요약된 질문에 대한 답변"),
                                        fieldWithPath("data.answers[].answerTime").optional().description("답변 생성 시간"),
                                        fieldWithPath("data.answers[].views").description("조회수"),
                                        fieldWithPath("data.answers[].likes").description("좋아요수")
                                )
                        ));
        }


        @Test
        @DisplayName("case 1-1: 정상 로직] 사용자의 정보만 존재하며, 질문답변 내역은 존재하지 않는다.")
        void getFollowUserInfo_qaObject_is_not_existed() throws Exception {
                // given
                String followNickname = "userA";
                Long answersCount = 0L;
                List<FollowingQaDto> answers = new ArrayList<>();

                FollowingUserInfoResponse response = createTestFollowingUserInfoResponse(followNickname,
                        answersCount, answers);

                // when
                Mockito.when(followingService.getFollowUserInfo(followNickname)).thenReturn(response);

                // then
                mockMvc.perform(RestDocumentationRequestBuilders.get("/api/following/info")
                                .queryParam("followNickname", followNickname))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andDo(document("api/following/info/200-only-userinfo",
                                requestParameters(
                                        parameterWithName("followNickname").description("조회 요청을 보내는 닉네임")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                        fieldWithPath("message").type(STRING).description("응답 메시지"),
                                        fieldWithPath("data.followingUserInfoDto.nickname").description("닉네임"),
                                        fieldWithPath("data.followingUserInfoDto.company").description("재직 중인 회사. 회사 정보가 없는 경우 null이 될 수 있습니다."),
                                        fieldWithPath("data.followingUserInfoDto.field").description("관심 분아. 여러 개면 ','로 구분] 프론트엔드, 백엔드, ..."),
                                        fieldWithPath("data.followingUserInfoDto.school").description("The school the user attended."),
                                        fieldWithPath("data.followingUserInfoDto.major").description("The major of the user."),
                                        fieldWithPath("data.followingUserInfoDto.subMajor").optional().description("복수전공. null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.minor").optional().description("부전공. null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.techStack").description("기술 스택. 여러 개면 ','로 구분"),
                                        fieldWithPath("data.followingUserInfoDto.imgUrl").description("프로필 img url"),
                                        fieldWithPath("data.followingUserInfoDto.career").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.certificate").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.awards").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.followingUserInfoDto.activity").optional().description("null 값을 허용합니다"),
                                        fieldWithPath("data.answersCount").description("멘토의 답변 수"),
                                        fieldWithPath("data.answers").description("질문 답변 객체 Array")
                                )
                        ));
        }

        @Test
        @DisplayName("case 2: 요청한 사용자의 닉네임이 데이터베이스에 없는 경우")
        void getFollowUserInfo_user_not_in_db() throws Exception {
                // given
                String followNickname = "user333_not_in_db";

                // when
                Mockito.when(followingService.getFollowUserInfo(followNickname))
                        .thenThrow(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

                // then
                mockMvc.perform(RestDocumentationRequestBuilders.get("/api/following/info")
                                .queryParam("followNickname", followNickname))
                        .andExpect(status().is5xxServerError())
                        .andDo(print())
                        .andDo(document("api/following/info/500-error",
                                requestParameters(
                                        parameterWithName("followNickname").description("조회 요청을 보내는 닉네임")
                                ),
                                responseFields(
                                        fieldWithPath("code").type(JsonFieldType.NUMBER).description("응답 코드"),
                                        fieldWithPath("message").type(STRING).description("응답 메시지"),
                                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터")
                                )
                        ));
        }

        private FollowingUserInfoResponse createTestFollowingUserInfoResponse(String followNickname,
                                                                              Long answersCount,
                                                                              List<FollowingQaDto> answers) {
                FollowingUserInfoDto followingUserInfoDto = FollowingUserInfoDto.builder()
                        .nickname(followNickname)
                        .company("Google")
                        .field("백엔드")
                        .school("서울과학기술대학교")
                        .major("컴퓨터공학과")
                        .subMajor(null)
                        .minor(null)
                        .techStack("Spring Boot, AWS")
                        .imgUrl("https://...")
                        .career(null)
                        .certificate(null)
                        .awards(null)
                        .activity(null)
                        .build();

                return FollowingUserInfoResponse.of(followingUserInfoDto, answersCount, answers);
        }
}
