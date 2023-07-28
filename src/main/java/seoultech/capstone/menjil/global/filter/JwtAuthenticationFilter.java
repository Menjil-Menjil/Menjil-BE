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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final String HEADER_DATA_VALUE = "None";
    private final String ACCESS_TOKEN_IS_NOT_VALID = "Access_Token_is_not_valid";
    private final String REFRESH_TOKEN_IS_NOT_VALID = "Refresh_Token_is_not_valid";

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        response.setCharacterEncoding("UTF-8");

        log.info(">> Enter the JwtAuthenticationFilter");
        String[] jwtTokenArr = resolveAuthorizationBearer(request);
        String accessToken;
        String refreshToken;

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
        else if (jwtTokenArr.length > 2) {
            ConcurrentHashMap<String, Object> detailsMap = responseJson(403,
                    "Authorization의 Bearer 값이 잘못되었습니다", HEADER_DATA_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(response.getWriter(), detailsMap);
            return;
        }
        /* case 3: Access Token 과 함께 요청이 들어오는 경우 */
        else if (jwtTokenArr.length == 1) {
            accessToken = jwtTokenArr[0];
            TokenStatus accessTokenIsAvailable = jwtTokenProvider.validateAccessToken(accessToken);

            if (accessTokenIsAvailable == TokenStatus.RELIABLE) {
                /* 정상 요청 */
                filterChain.doFilter(request, response);
            } else {
                /* Refresh Token을 담아서 보내도록 클라이언트에게 요청 */
                ConcurrentHashMap<String, Object> detailsMap = responseJson(403,
                        "Access Token 값이 유효하지 않습니다", ACCESS_TOKEN_IS_NOT_VALID);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                objectMapper.writeValue(response.getWriter(), detailsMap);
                return;
            }
        }
        /* case 4: Access Token, Refresh Token 이 모두 담겨서 요청이 들어오는 경우 */
        else if (jwtTokenArr.length == 2) {
            accessToken = jwtTokenArr[0];
            refreshToken = jwtTokenArr[1];

            TokenStatus refreshTokenIsAvailable = jwtTokenProvider.validateRefreshToken(refreshToken);
            if (refreshTokenIsAvailable == TokenStatus.RELIABLE) {
                /* Access Token 재발급 */
                String userId = jwtTokenProvider.getUserId(refreshToken);
                String newAccessToken = jwtTokenProvider.generateAccessToken(userId, LocalDateTime.now());

                Map<String, String> map = new ConcurrentHashMap<>();
                map.put("accessToken", newAccessToken);

                ConcurrentHashMap<String, Object> detailsMap = responseJson(201,
                        "Access Token이 재발급 되었습니다", map);
                response.setStatus(HttpStatus.CREATED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                objectMapper.writeValue(response.getWriter(), detailsMap);
            } else {
                /* 재로그인 요청 */
                ConcurrentHashMap<String, Object> detailsMap = responseJson(403,
                        "Refresh Token 값이 유효하지 않습니다. 재로그인 해주세요", REFRESH_TOKEN_IS_NOT_VALID);
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                objectMapper.writeValue(response.getWriter(), detailsMap);
            }
            return;
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
