package com.tailorTrip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass // 이 클래스를 상속받는 엔티티들이 해당 필드를 자동으로 상속받게 해줍니다. 데이터베이스 테이블로 매핑되지 않으며, 하위 클래스에서만 사용됩니다.
@EntityListeners(value = {AuditingEntityListener.class}) // JPA의 감사(auditing) 기능을 활성화하여, 엔티티의 생성일(@CreatedDate)과 수정일(@LastModifiedDate)을 자동으로 관리합니다.
@Getter
public class BaseEntity {

    @CreatedDate
    @Column(name = "regdate",updatable = false) // 필드가 엔티티 업데이트 시 수정되지 않음을 의미합니다.
    private LocalDateTime regDate;

    @LastModifiedDate
    @Column(name = "moddate")
    private LocalDateTime modDate;
}
