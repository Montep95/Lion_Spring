package com.example.jwtexam.jwt.filter;

import com.example.jwtexam.jwt.exception.JwtExceptionCode;
import com.example.jwtexam.jwt.token.JwtAuthenticationToken;
import com.example.jwtexam.jwt.util.JwtTokenizer;
import com.example.jwtexam.security.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter 실행!!");
        //request로부터 토큰을 얻어옴
        String token = getToken(request);

        if(StringUtils.hasText(token)){
            try{
                Authentication authentication = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }catch (ExpiredJwtException e){ // 예외 추가 (JwtExceptionEnum으로부터)
                request.setAttribute("exception", JwtExceptionCode.EXPIRED_TOKEN.getCode());
                log.error("Expired Token : {}",token,e);
                SecurityContextHolder.clearContext();
                throw new BadCredentialsException("Expired token exception", e);
            }catch (UnsupportedJwtException e){
                request.setAttribute("exception", JwtExceptionCode.UNSUPPORTED_TOKEN.getCode());
                log.error("Unsupported Token: {}", token, e);
                SecurityContextHolder.clearContext();
                throw new BadCredentialsException("Unsupported token exception", e);
            } catch (MalformedJwtException e) {
                request.setAttribute("exception", JwtExceptionCode.INVALID_TOKEN.getCode());
                log.error("Invalid Token: {}", token, e);

                SecurityContextHolder.clearContext();

                throw new BadCredentialsException("Invalid token exception", e);
            } catch (IllegalArgumentException e) {
                request.setAttribute("exception", JwtExceptionCode.NOT_FOUND_TOKEN.getCode());
                log.error("Token not found: {}", token, e);

                SecurityContextHolder.clearContext();

                throw new BadCredentialsException("Token not found exception", e);
            }catch (Exception e){
                log.error("JWT Filter - Internal Error: {}", token, e);
                SecurityContextHolder.clearContext();
                throw new BadCredentialsException("JWT Filter - Internal Error");
            }
        }

        filterChain.doFilter(request, response);
    }

    private Authentication getAuthentication(String token){
        Claims claims = jwtTokenizer.parseAccessToken(token);
        String email = claims.getSubject();
        Long userId = claims.get("userId", Long.class);
        String name = claims.get("name", String.class);
        String username = claims.get("username", String.class);

        // 권한 정보 가져오기
        List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(claims);

        // UserDetails 객체 사용 (Spring Security가 제공)
        CustomUserDetails customUserDetails = new CustomUserDetails(username, "", name, grantedAuthorities);
        return new JwtAuthenticationToken(grantedAuthorities, customUserDetails, null); // 대체로 credentials에는 비밀번호가 들어감 JWT에서는 비밀번호 사용 X
    }

    private List<GrantedAuthority> getGrantedAuthorities(Claims claims){
        List<String> roles = (List<String>)claims.get("roles");
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private String getToken(HttpServletRequest request){
        // 1. 헤더를 통해서 토큰을 찾음
        String authorization = request.getHeader("Authorization");
        if(StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")){
            return authorization.substring(7);
        }

        // 2. 헤더에 토큰이 없다면 쿠키에서 토큰을 찾음
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if("accessToken".equals(cookie.getName())){
                    return cookie.getValue();
                }
            }
        }

        // 헤더, 쿠키 둘다 토큰이 없었다면 null을 반환
        return null;
    }
}
