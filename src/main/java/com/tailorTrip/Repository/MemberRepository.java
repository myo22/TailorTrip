package com.tailorTrip.Repository;

import com.tailorTrip.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    @EntityGraph(attributePaths = "roleSet")
    @Query("select m from Member m where m.mid = :mid and m.social = false ")
    Optional<Member> getWithRoles(String mid);

    @EntityGraph(attributePaths = "roleSet")
    Optional<Member> findByEmail(String email);

    //  @Query 애너테이션을 사용하면 기본적으로 JPA는 이 쿼리를 **읽기 쿼리(SELECT)**로 간주합니다. 이를 명확히 하기 위해 @Modifying을 사용
    @Modifying
    @Transactional
    @Query("update Member m set m.mpw = :mpw where m.mid = :mid")
    void updatePassword(@Param("mpw")String password, @Param("mid") String mid);
}
