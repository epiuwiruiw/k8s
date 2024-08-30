package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.MemberLoginDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.dto.MemberSaveReqDto;
import com.beyond.ordersystem.member.dto.ResetPasswordDto;
import com.beyond.ordersystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@Transactional
public class MemberService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    public Member createMember(MemberSaveReqDto dto) {
        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다");
        }
        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));
        memberRepository.save(member);
        return member;
    }

    public Page<MemberResDto> listMember(Pageable pageable) {
        Page<Member> members = memberRepository.findAll(pageable);
        return members.map(a->a.fromEntity());
    }

    public MemberResDto myInfo() {
        Member member = memberRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 이메일입니다."));
        return member.fromEntity();
    }


    public Member login(MemberLoginDto dto) {
        // email 존재 여부
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 이메일입니다."));
        // password 일치여부
        if(!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }


    public void resetPassword(ResetPasswordDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(()->new EntityNotFoundException("이메일이 존재하지 않습니다."));
        if(!passwordEncoder.matches(dto.getAsIsPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        member.updatePassword(passwordEncoder.encode(dto.getToBePassword()));
    }
}
