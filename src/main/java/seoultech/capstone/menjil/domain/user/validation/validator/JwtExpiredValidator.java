package seoultech.capstone.menjil.domain.user.validation.validator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import seoultech.capstone.menjil.domain.user.validation.customAnnotation.Expired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.security.Key;
import java.util.Map;

public class JwtExpiredValidator implements ConstraintValidator<Expired, String> {

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    /**
     * 토큰 만료 여부를 검증한다.
     * 만료되었을 시, @Expired Annotation 을 사용
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        try {
            Map<String, Object> dataMap = decodeJwt(value);
        } catch (ExpiredJwtException e) {
            return false;   // token 만료 시 false
        }
        return true;
    }

    public Key jwtSecretKeyProvider(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Map<String, Object> decodeJwt(String jwt) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(jwtSecretKeyProvider(JWT_SECRET_KEY))
                .build().parseClaimsJws(jwt);
        return jws.getBody();
    }
}
