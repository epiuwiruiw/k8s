package com.beyond.ordersystem.member.controller;

import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import com.beyond.ordersystem.common.dto.CommonErrorDto;
import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.*;
import com.beyond.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
public class MemberController {


    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public MemberController(@Qualifier("2") RedisTemplate redisTemplate,
                            MemberService memberService,
                            JwtTokenProvider jwtTokenProvider){
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/member/create")
    public ResponseEntity<Object> memberCreate(@Valid @RequestBody MemberSaveReqDto dto) {
        try {
            Member member = memberService.createMember(dto);
            CommonResDto commonResDto = new CommonResDto(HttpStatus.CREATED, "member is successfully created", member.getId());
            return new ResponseEntity<>(commonResDto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());
            return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        }
    }

    // admin만 회원목록 전체 조회 가능
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/list")
    public ResponseEntity<Object> memberRead(Pageable pageable) {
        Page<MemberResDto> dtos = memberService.listMember(pageable);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "list is successfully found", dtos);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    //  본인은 본인 회원 정보만 조회 가능
    //  /member/myinfo. MemberResDto


    @GetMapping("/member/myinfo")
    public ResponseEntity<Object> myInfo() {
        MemberResDto dto = memberService.myInfo();
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "member is found", dto);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }


    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto) {

        // email, password가 일치하는지 검증
        Member member = memberService.login(dto);

        // 일치할 경우 accessToken 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().toString());

        // redis에 email과 rt를 key:value로 하여 저장
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS); // 240시간
        // 생성된 토큰을 CommentResDto에 담아 사용자에게 return
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "login is successful", loginInfo);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);

    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto) {
        String rt = dto.getRefreshToken();
        Claims claims = null;
        try {
            // 코드를 통해 rt 검증
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(),  "invalid refresh token"), HttpStatus.BAD_REQUEST);
        }
        String email = claims.getSubject();
        String role = claims.get("role").toString();

        // redis를 조회하면 rt 추가 검증
        Object obj = redisTemplate.opsForValue().get(email);
        if(obj==null || (!obj.toString().equals(rt))) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "invalid refresh token"), HttpStatus.BAD_REQUEST);
        }
        String newAt = jwtTokenProvider.createToken(email, role);

        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "at is renewed", info);
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }

    @PatchMapping("member/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto dto) {
        memberService.resetPassword(dto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "password is renewed", "ok");
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}
