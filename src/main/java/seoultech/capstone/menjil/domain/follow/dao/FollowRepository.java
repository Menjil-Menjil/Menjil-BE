package seoultech.capstone.menjil.domain.follow.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import seoultech.capstone.menjil.domain.follow.domain.Follow;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findFollowByUserNicknameAndFollowNickname(String userNickname, String followNickname);

    List<Follow> findFollowsByUserNickname(String userNickname);

}
