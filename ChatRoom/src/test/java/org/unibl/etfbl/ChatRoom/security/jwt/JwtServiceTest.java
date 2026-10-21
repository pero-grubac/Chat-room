package org.unibl.etfbl.ChatRoom.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String KEY = "dGVzdC1zaWduaW5nLWtleS1mb3ItY2hhdHJvb20tdW5pdC10ZXN0cy0xMjM0NTY3OA==";
    private static final String OTHER_KEY = "YW5vdGhlci1zaWduaW5nLWtleS10aGF0LWlzLWxvbmctZW5vdWdoLWZvci1oczI1Ng==";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey", KEY);
    }

    private static UserEntity user(String username, RoleEnum role, PermissionEnum... permissions) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setRole(role);
        List<PermissionEntity> list = new ArrayList<>();
        for (PermissionEnum p : permissions) {
            list.add(new PermissionEntity(p, user));
        }
        user.setPermissions(list);
        return user;
    }

    private static Claims parse(String token, String key) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Test
    void generatedTokenCarriesSubjectRoleAndPermissions() {
        String token = jwtService.generateToken(user("ana", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD, PermissionEnum.DELETE));

        Claims claims = parse(token, KEY);
        assertThat(claims.getSubject()).isEqualTo("ana");
        assertThat(claims.get("role")).isEqualTo("ROLE_MODERATOR");
        assertThat(claims.get("permissions", List.class)).containsExactly("ADD", "DELETE");
        assertThat(jwtService.extractUserName(token)).isEqualTo("ana");
    }

    @Test
    void tokenIsValidOnlyForItsOwner() {
        UserEntity ana = user("ana", RoleEnum.ROLE_KORISNIK);
        String token = jwtService.generateToken(ana);

        assertThat(jwtService.isTokenValid(token, ana)).isTrue();
        assertThat(jwtService.isTokenValid(token, user("marko", RoleEnum.ROLE_KORISNIK))).isFalse();
    }

    @Test
    void sessionLastsTheConfiguredTime() {
        String token = jwtService.generateToken(user("ana", RoleEnum.ROLE_KORISNIK));

        Claims claims = parse(token, KEY);
        long lifetime = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(lifetime).isBetween(Duration.ofHours(24).minusSeconds(1).toMillis(), Duration.ofHours(24).plusSeconds(1).toMillis());
    }

    @Test
    void sessionLengthIsConfigurable() {
        ReflectionTestUtils.setField(jwtService, "expiration", Duration.ofMinutes(30));

        Claims claims = parse(jwtService.generateToken(user("ana", RoleEnum.ROLE_KORISNIK)), KEY);
        long lifetime = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertThat(lifetime).isBetween(Duration.ofMinutes(30).minusSeconds(1).toMillis(), Duration.ofMinutes(30).plusSeconds(1).toMillis());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String forged = Jwts.builder()
                .setSubject("admin")
                .claim("role", "ROLE_ADMIN")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(OTHER_KEY)), SignatureAlgorithm.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUserName(forged)).isInstanceOf(SignatureException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = Jwts.builder()
                .setSubject("ana")
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(KEY)), SignatureAlgorithm.HS256)
                .compact();

        assertThatThrownBy(() -> jwtService.isTokenValid(expired, user("ana", RoleEnum.ROLE_KORISNIK)))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tamperedPayloadIsRejected() {
        String token = jwtService.generateToken(user("ana", RoleEnum.ROLE_KORISNIK));
        String[] parts = token.split("\\.");
        String adminPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"ana\",\"role\":\"ROLE_ADMIN\"}".getBytes());
        String tampered = parts[0] + "." + adminPayload + "." + parts[2];

        assertThatThrownBy(() -> jwtService.extractUserName(tampered)).isInstanceOf(SignatureException.class);
    }
}
