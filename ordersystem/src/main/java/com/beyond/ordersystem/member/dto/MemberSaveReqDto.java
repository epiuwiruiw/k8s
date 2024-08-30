package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSaveReqDto {
    private String name;
    @NotEmpty(message="email is essential")
    private String email;
    @NotEmpty(message="password is essential")
    @Size(min = 8, message = "password minimum length is 8")
    private String password;
    private Address address;
    private Role role = Role.USER;

    public Member toEntity(String password) {
        Member member = Member.builder()
                .name(this.name)
                .email(this.email)
                .password(password)
                .role(this.role)
//                .address(Address.builder()
//                        .city(this.city)
//                        .street(this.street)
//                        .zipcode(this.zipcode)
//                        .build())
                .address(this.address)
                .build();
        return member;
    }
}
