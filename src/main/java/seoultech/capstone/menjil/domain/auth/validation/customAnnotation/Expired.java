package seoultech.capstone.menjil.domain.auth.validation.customAnnotation;

import seoultech.capstone.menjil.domain.auth.validation.validator.JwtExpiredValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD) // 1
@Retention(RetentionPolicy.RUNTIME) // 2
@Constraint(validatedBy = JwtExpiredValidator.class)
public @interface Expired {
    String message() default "JWT 토큰이 만료되었습니다. 처음부터 다시 진행해 주세요";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
