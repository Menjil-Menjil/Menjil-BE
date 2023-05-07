package seoultech.capstone.menjil.domain.user.dto;

public interface OAuthUserDto {

    String getId();

    String getEmail();

    String getName();

    String getProvider();   // google, kakao, ...

}
