package seoultech.capstone.menjil.domain.auth.validation.validator;

import io.jsonwebtoken.ExpiredJwtException;
import seoultech.capstone.menjil.domain.auth.validation.customAnnotation.Expired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;

import static seoultech.capstone.menjil.global.common.JwtUtils.decodeJwt;

public class JwtExpiredValidator implements ConstraintValidator<Expired, String> {

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
}
