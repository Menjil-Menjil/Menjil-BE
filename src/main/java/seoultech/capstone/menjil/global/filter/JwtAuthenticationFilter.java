package seoultech.capstone.menjil.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import seoultech.capstone.menjil.domain.auth.jwt.JwtTokenProvider;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

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

        if (jwtTokenArr == null) {
            ConcurrentHashMap<String, Object> detailsMap = responseJson(403, "Header의 Authorization 값이 존재하지 않거나, " +
                    "혹은 Authorization에서 Bearer 타입이 존재하지 않습니다");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(response.getWriter(), detailsMap);
            return;
        } else if (jwtTokenArr.length != 2) {
            ConcurrentHashMap<String, Object> detailsMap = responseJson(403, "Authorization의 Bearer 값이 두 개가 아니거나," +
                    "전송 형식이 잘못되었습니다");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            objectMapper.writeValue(response.getWriter(), detailsMap);
            return;
        } else {
            // 올바르게 access, refresh token 이 들어온 경우
            // 1. access token 검증
            accessToken = jwtTokenArr[0];
            refreshToken = jwtTokenArr[1];

            boolean accessTokenAvailable = jwtTokenProvider.validateAccessToken(accessToken);
            if (accessTokenAvailable) {
                // access token 이 유효한 경우
                // refresh token 은 만료 기간이 4일 전에 갱신되므로, access token 이 유효한데 refresh token 이 만료되는 경우는 존재 X
                filterChain.doFilter(request, response);

            } else {
                // access token 이 유효하지 않은 경우, refresh token 도 검증 필요
                Map<String, Object> valiDateRefreshTokenMap = jwtTokenProvider.validateRefreshToken(refreshToken);

                if (Boolean.parseBoolean(valiDateRefreshTokenMap.get("tokenStatus").toString())
                        && !Boolean.parseBoolean(valiDateRefreshTokenMap.get("refreshStatus").toString())) {
                    // 1. refresh token 이 유효하고, 만료 기간이 4일보다 많이 남은 경우
                    // access token 만 재발급해서 클라이언트로 응답
                    log.info(">> Access Token 재발급 진행! in JwtAuthenticationFilter");

                    // 응답 시 필요없는 key-value 제거
                    valiDateRefreshTokenMap.remove("tokenStatus");
                    valiDateRefreshTokenMap.remove("refreshStatus");

                    response.setStatus(HttpStatus.CREATED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    objectMapper.writeValue(response.getWriter(), valiDateRefreshTokenMap);
                    return;
                } else if (Boolean.parseBoolean(valiDateRefreshTokenMap.get("tokenStatus").toString())
                        && Boolean.parseBoolean(valiDateRefreshTokenMap.get("refreshStatus").toString())) {
                    // 2. refresh token 이 유효하나, 만료 기간이 4일 미만으로 남은 경우
                    // access, refresh token 재발급 후 클라이언트로 응답
                    log.info(">> Access & Refresh Token 재발급 진행! in JwtAuthenticationFilter");

                    // 응답 시 필요없는 key-value 제거
                    valiDateRefreshTokenMap.remove("tokenStatus");
                    valiDateRefreshTokenMap.remove("refreshStatus");

                    response.setStatus(HttpStatus.CREATED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    objectMapper.writeValue(response.getWriter(), valiDateRefreshTokenMap);
                    return;
                } else {
                    // 3. refresh token 이 만료된 경우: 클라이언트에서 로그아웃 처리를 수행하도록 요청한다.
                    // 3-1. refresh token 변조된 경우: 클라이언트에서 로그아웃 처리를 수행하도록 요청한다.

                    // 응답 시 필요없는 key-value 제거
                    valiDateRefreshTokenMap.remove("tokenStatus");
                    valiDateRefreshTokenMap.remove("refreshStatus");
                    valiDateRefreshTokenMap.remove("accessToken");
                    valiDateRefreshTokenMap.remove("refreshToken");

                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    objectMapper.writeValue(response.getWriter(), valiDateRefreshTokenMap);
                    return;
                }
            }
        }
    }

    private String[] resolveAuthorizationBearer(HttpServletRequest request) {
        String headerAuth = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(BEARER_PREFIX)) {
            // access, refresh token 모두 담는다.
            return headerAuth.substring(7).split(" ");
        }
        return null;
    }

    public ConcurrentHashMap<String, Object> responseJson(int status, String message) {
        ConcurrentHashMap<String, Object> errorDetails = new ConcurrentHashMap<>();
        errorDetails.put("status", status);    // 403: forbidden
        errorDetails.put("message", message);
        return errorDetails;
    }
}
