package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseTimeEntity {
    @Id // PK 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false, unique = true)
    private String email;

    private String password;
    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Ordering> orderList;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    public MemberResDto fromEntity() {
        return MemberResDto.builder().id(this.id).name(this.name).email(this.email).address(this.address).orderCount(this.orderList.size()).build();
    }

    public void updatePassword(String password) {
        this.password = password;
    }

}


