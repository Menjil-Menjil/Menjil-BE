package seoultech.capstone.menzil.domain.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import seoultech.capstone.menzil.domain.user.domain.SocialLoginType;

@Configuration
public class SocialLoginTypeConverter implements Converter<String, SocialLoginType> {
    @Override
    public SocialLoginType convert(String str) {
        return SocialLoginType.valueOf(str.toUpperCase());
    }
}
