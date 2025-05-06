package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    // ✅ 토큰 생성
    public String generateToken(String userId, String role) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    // ✅ 토큰 유효성 검사 (중복 제거!)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    // ✅ 토큰에서 userId 추출
    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    // ✅ 토큰에서 role 추출
    public String getRoleFromToken(String token) {
        Claims claims = parseClaims(token);
        return (String) claims.get("role");
    }

    // ✅ Claims 파싱
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ 권한 정보 생성
    public Collection<? extends GrantedAuthority> getAuthorities(String role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
