package com.beyond.ordersystem.common.domain;

import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
//기본적으로 엔티티는 상속 관계가 불가능하여 해당 어노테이션을 붙여야 상속 관계 성립 가능
@Getter
//이것만가지고 객체를 만들지 못하도록 추상클래스로 해준다
public abstract class BaseTimeEntity {

    //  id도 일원화해서 사용할 수 있지만 복잡하고 좀 제약조건이 있다. 그렇게 추천하지는 않는다.
    @CreationTimestamp
    private LocalDateTime createdTime;
    @CreationTimestamp
    private LocalDateTime updateTime;

}
