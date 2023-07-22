package seoultech.capstone.menjil.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;
import seoultech.capstone.menjil.domain.auth.jwt.TokenStatus;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final String HEADER_DATA_VALUE = "None";
    private final String RE_LOGIN_VALUE = "Re-login";

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info(">> Enter the JwtAuthenticationFilter");
        String[] jwtTokenArr = resolveAuthorizationBearer(request);
        String accessToken = null;
        String refreshToken = null;

        /* case 1: Header 에 Authorization 값이 존재하지 않거나, Authorization 이 Bearer 타입이 아닌 경우 */
        if (jwtTokenArr == null) {
            ConcurrentHashMap<String, Object> detailsMap = responseJson(403,
                    "Header의 Authorization 값이 존재하지 않거나, " +
                            "혹은 Authorization에서 Bearer 타입이 존재하지 않습니다", HEADER_DATA_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(response.getWriter(), detailsMap);
            return;
        }
        /* case 2: Authorization 에서 Bearer 값이 2개(Access Token, Refresh Token)이 아닌 경우,
        혹은 공백으로 분리되어 있지 않은 경우  */
        else if (jwtTokenArr.length != 2) {
            ConcurrentHashMap<String, Object> detailsMap = responseJson(403,
                    "Authorization의 Bearer 값이 두 개가 아니거나," +
                            "전송 형식이 잘못되었습니다", HEADER_DATA_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(response.getWriter(), detailsMap);
            return;
        } else {
            /* case 3: 정상적으로 Access Token, Refresh Token 이 들어온 경우 */
            /* 1. Access Token 검증 */
            accessToken = jwtTokenArr[0];
            refreshToken = jwtTokenArr[1];

            TokenStatus accessTokenIsAvailable = jwtTokenProvider.validateAccessToken(accessToken);

            if (accessTokenIsAvailable != TokenStatus.RELIABLE) {
                /* 사용자 로그아웃 및 재로그인 유도 */
                ConcurrentHashMap<String, Object> detailsMap = responseJson(403,
                        "Access Token 값이 유효하지 않습니다. 재로그인 해주세요", RE_LOGIN_VALUE);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                objectMapper.writeValue(response.getWriter(), detailsMap);
                return;
            } else {
                /* Access Token 이 유효한 경우, Refresh Token 을 검증한다 */
                TokenStatus refreshTokenIsAvailable = jwtTokenProvider.validateRefreshToken(refreshToken);

                if (refreshTokenIsAvailable != TokenStatus.RELIABLE) {
                    /* 사용자 로그아웃 및 재로그인 유도  */
                    ConcurrentHashMap<String, Object> detailsMap = responseJson(403,
                            "Refresh Token 값이 유효하지 않습니다. 재로그인 해주세요", RE_LOGIN_VALUE);
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    objectMapper.writeValue(response.getWriter(), detailsMap);
                } else {
                    /* 정상 요청 */
                    filterChain.doFilter(request, response);
                }
            }
        }
    }

    private String[] resolveAuthorizationBearer(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
            // Access Token, Refresh Token 모두 담는다.
            return headerAuth.substring(7).split(" ");
        }
        return null;
    }

    public ConcurrentHashMap<String, Object> responseJson(int code, String message, Object data) {
        ConcurrentHashMap<String, Object> errorDetails = new ConcurrentHashMap<>();
        errorDetails.put("code", code);    // 403: forbidden
        errorDetails.put("message", message);
        errorDetails.put("data", data);

        return errorDetails;
    }
}
