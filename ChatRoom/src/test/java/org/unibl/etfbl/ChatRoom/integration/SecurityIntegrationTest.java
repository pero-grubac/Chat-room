package org.unibl.etfbl.ChatRoom.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.unibl.etfbl.ChatRoom.enums.PermissionEnum;
import org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum;
import org.unibl.etfbl.ChatRoom.enums.RoleEnum;
import org.unibl.etfbl.ChatRoom.models.entities.ForumRoomEntity;
import org.unibl.etfbl.ChatRoom.models.entities.PermissionEntity;
import org.unibl.etfbl.ChatRoom.models.entities.SecurityEventEntity;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.CommentEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.ForumRoomEntityRepository;
import org.unibl.etfbl.ChatRoom.repositories.SecurityEventRepository;
import org.unibl.etfbl.ChatRoom.security.auth.google.GoogleTokenVerifier;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.jwt.JwtService;
import org.unibl.etfbl.ChatRoom.services.EmailService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests of the security chain: real Spring context, real JWT filter and session cookie,
 * method security, WAF validation, error mapping and an in-memory database. Only outgoing email
 * is mocked, so the emailed code can be read.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    private static final String PASSWORD = "Secret#123";
    private static final String COOKIE = "CHATROOM_SESSION";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private UserEntityRepository users;
    @Autowired
    private ForumRoomEntityRepository rooms;
    @Autowired
    private CommentEntityRepository comments;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Value("${token.signing.key}")
    private String signingKey;

    @Autowired
    private SecurityEventRepository securityEvents;

    @MockBean
    private EmailService emailService;
    @MockBean
    private GoogleTokenVerifier googleTokenVerifier;

    private ForumRoomEntity science;
    private ForumRoomEntity music;

    @BeforeEach
    void resetDatabase() {
        jdbc.execute("DELETE FROM logger");
        jdbc.execute("DELETE FROM comment");
        jdbc.execute("DELETE FROM permission");
        jdbc.execute("DELETE FROM user_has_room");
        jdbc.execute("DELETE FROM `user`");
        jdbc.execute("DELETE FROM forumroom");
        science = rooms.saveAndFlush(ForumRoomEntity.builder().name("Science").build());
        music = rooms.saveAndFlush(ForumRoomEntity.builder().name("Music").build());
    }

    // ── helpers ────────────────────────────────────────────────────

    private UserEntity createUser(String username, RoleEnum role, PermissionEnum... permissions) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setIsApproved(role == null ? null : (byte) 1);
        user.setRole(role);
        user.setSource(RegristrationSourceEnum.LOCAL);
        List<PermissionEntity> list = new ArrayList<>();
        for (PermissionEnum p : permissions) {
            list.add(new PermissionEntity(p, user));
        }
        user.setPermissions(list);
        UserEntity saved = users.saveAndFlush(user);
        // Moderators are members of Science, so they moderate it (and only it)
        if (role == RoleEnum.ROLE_MODERATOR) {
            joinRoom(saved, science);
        }
        return saved;
    }

    private void joinRoom(UserEntity user, ForumRoomEntity room) {
        jdbc.update("INSERT INTO user_has_room (IdUser, IdForumRoom) VALUES (?, ?)", user.getIdUser(), room.getIdForumRoom());
    }

    private List<String> recordedEvents() {
        return securityEvents.findAll().stream().map(SecurityEventEntity::getType).toList();
    }

    private int writePendingComment(ForumRoomEntity room, String text) throws Exception {
        String jwt = signIn(users.getUserEntityByUsername("writer").orElseGet(() -> createUser("writer", RoleEnum.ROLE_KORISNIK)));
        call(post("/comments/create").content(json(Map.of("text", text, "idForumRoom", room.getIdForumRoom()))), jwt)
                .andExpect(status().isOk());
        return comments.findAllByForumRoom_IdForumRoom(room.getIdForumRoom()).stream()
                .filter(c -> text.equals(c.getText())).findFirst().orElseThrow().getIdComment();
    }

    /** Signs a user in directly, the same way /auth/token does: the JWT is issued and stored. */
    private String signIn(UserEntity user) {
        String jwt = jwtService.generateToken(user);
        user.setJWT(jwt);
        users.saveAndFlush(user);
        return jwt;
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    /** Sends the request with the session cookie, as the browser does. */
    private ResultActions call(MockHttpServletRequestBuilder request, String jwt) throws Exception {
        if (jwt != null) {
            request.cookie(new Cookie(COOKIE, jwt));
        }
        return mvc.perform(request.contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions postJson(String url, Object body) throws Exception {
        return call(post(url).content(json(body)), null);
    }

    private String emailedCode() {
        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).sendEmail(anyString(), anyString(), anyString(), code.capture());
        return code.getValue();
    }

    private static String sessionCookie(MvcResult result) {
        return result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    }

    private static String cookieValue(String setCookie) {
        return setCookie.substring(setCookie.indexOf('=') + 1, setCookie.indexOf(';'));
    }

    private String tokenSignedWith(String key, String subject, long expiresInMillis) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiresInMillis))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key)), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── two-factor login ───────────────────────────────────────────

    @Nested
    class TwoFactorLogin {

        @Test
        void passwordThenEmailedCodeStartsAnHttpOnlySession() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);

            postJson("/auth/login", Map.of("username", "ana", "password", PASSWORD)).andExpect(status().isOk());

            MvcResult result = postJson("/auth/token", Map.of("username", "ana", "password", PASSWORD, "token", emailedCode()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("ana"))
                    .andExpect(jsonPath("$.role").value("ROLE_KORISNIK"))
                    .andExpect(jsonPath("$.token").doesNotExist())
                    .andReturn();

            String cookie = sessionCookie(result);
            assertThat(cookie).startsWith(COOKIE + "=").contains("HttpOnly", "Secure", "SameSite=Strict", "Path=/");
            call(get("/forumrooms"), cookieValue(cookie)).andExpect(status().isOk());
        }

        @Test
        void codeWorksOnlyOnce() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);
            postJson("/auth/login", Map.of("username", "ana", "password", PASSWORD));
            Map<String, String> tokenRequest = Map.of("username", "ana", "password", PASSWORD, "token", emailedCode());

            postJson("/auth/token", tokenRequest).andExpect(status().isOk());
            postJson("/auth/token", tokenRequest).andExpect(status().isUnauthorized());
        }

        @Test
        void expiredCodeIsRejected() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);
            postJson("/auth/login", Map.of("username", "ana", "password", PASSWORD));
            UserEntity ana = users.getUserEntityByUsername("ana").orElseThrow();
            ana.setTwoFactorTokenExpiresAt(Instant.now().minusSeconds(1));
            users.saveAndFlush(ana);

            postJson("/auth/token", Map.of("username", "ana", "password", PASSWORD, "token", emailedCode()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid or expired code"));
        }

        @Test
        void wrongPasswordIsRejectedAndNoCodeIsSent() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);

            postJson("/auth/login", Map.of("username", "ana", "password", "wrong"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid credentials"));
            verifyNoInteractions(emailService);
        }

        @Test
        void unknownUserLooksLikeAWrongPassword() throws Exception {
            postJson("/auth/login", Map.of("username", "nobody", "password", PASSWORD))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid credentials"));
        }

        @Test
        void wrongCodeIsRejected() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);
            postJson("/auth/login", Map.of("username", "ana", "password", PASSWORD));

            postJson("/auth/token", Map.of("username", "ana", "password", PASSWORD, "token", "guess"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void passwordAloneDoesNotSkipTheSecondFactor() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);

            postJson("/auth/token", Map.of("username", "ana", "password", PASSWORD)).andExpect(status().isUnauthorized());
        }

        @Test
        void emailAloneDoesNotSignInThroughTheOAuthEndpoint() throws Exception {
            createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values());

            postJson("/auth/oauth/token", Map.of("email", "admin@example.com", "type", "LOCAL"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void pendingAccountIsToldItIsNotApproved() throws Exception {
            createUser("newbie", null);

            postJson("/auth/login", Map.of("username", "newbie", "password", PASSWORD))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("Account is not approved yet"));
            verifyNoInteractions(emailService);
        }
    }

    // ── session handling ───────────────────────────────────────────

    @Nested
    class Sessions {

        @Test
        void requestWithoutSessionGetsJson401() throws Exception {
            call(get("/forumrooms"), null)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.detail").value("Authentication required"));
        }

        @Test
        void meReturnsTheSignedInUser() throws Exception {
            String jwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD, PermissionEnum.DELETE));

            call(get("/auth/me"), jwt)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("mod"))
                    .andExpect(jsonPath("$.role").value("ROLE_MODERATOR"))
                    .andExpect(jsonPath("$.permissions.length()").value(2));
        }

        @Test
        void meWithoutSessionIs401() throws Exception {
            call(get("/auth/me"), null).andExpect(status().isUnauthorized());
        }

        @Test
        void bearerHeaderWorksForApiClients() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            mvc.perform(get("/forumrooms").header("Authorization", "Bearer " + jwt)).andExpect(status().isOk());
        }

        @Test
        void logoutEndsTheSessionAndClearsTheCookie() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            MvcResult result = call(post("/auth/logout"), jwt)
                    .andExpect(status().isNoContent())
                    .andReturn();

            assertThat(sessionCookie(result)).contains("Max-Age=0");
            assertThat(users.getUserEntityByUsername("ana").orElseThrow().getJWT()).isNull();
            call(get("/forumrooms"), jwt).andExpect(status().isUnauthorized());
        }

        @Test
        void logoutWithoutSessionStillClearsTheCookie() throws Exception {
            MvcResult result = call(post("/auth/logout"), null)
                    .andExpect(status().isNoContent())
                    .andReturn();

            assertThat(sessionCookie(result)).contains("Max-Age=0");
        }

        @Test
        void replacedJwtIsRejected() throws Exception {
            UserEntity ana = createUser("ana", RoleEnum.ROLE_KORISNIK);
            String oldJwt = signIn(ana);
            Thread.sleep(1100); // iat has one-second resolution, so wait for a different token
            signIn(users.findById(ana.getIdUser()).orElseThrow());

            call(get("/forumrooms"), oldJwt)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Session ended"));
        }

        @Test
        void forgedJwtIsRejectedWithoutAnError() throws Exception {
            createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values());
            String forged = tokenSignedWith("YW5vdGhlci1zaWduaW5nLWtleS10aGF0LWlzLWxvbmctZW5vdWdoLWZvci1oczI1Ng==", "admin", 60_000);

            MvcResult result = call(get("/users"), forged)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Invalid session"))
                    .andReturn();
            assertThat(sessionCookie(result)).contains("Max-Age=0");
        }

        @Test
        void expiredJwtIsReportedAsExpired() throws Exception {
            UserEntity ana = createUser("ana", RoleEnum.ROLE_KORISNIK);
            String expired = tokenSignedWith(signingKey, "ana", -60_000);
            ana.setJWT(expired);
            users.saveAndFlush(ana);

            call(get("/forumrooms"), expired)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("JWT expired"));
        }

        @Test
        void maliciousInputRevokesTheSession() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            call(post("/comments/create")
                    .content(json(Map.of("text", "1; DROP TABLE comment", "idForumRoom", science.getIdForumRoom()))), jwt)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Blocked by the web application firewall"));

            assertThat(users.getUserEntityByUsername("ana").orElseThrow().getJWT()).isNull();
            call(get("/forumrooms"), jwt).andExpect(status().isUnauthorized());
        }

        @Test
        void maliciousInputWithoutSessionIsRejectedCleanly() throws Exception {
            postJson("/auth/login", Map.of("username", "<script>x</script>", "password", PASSWORD))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void ordinarySentenceIsNotMistakenForAnAttack() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            call(post("/comments/create")
                    .content(json(Map.of("text", "Rock and roll, or should we update the playlist?", "idForumRoom", science.getIdForumRoom()))), jwt)
                    .andExpect(status().isOk());
        }
    }

    // ── role and permission checks ────────────────────────────────

    @Nested
    class Authorization {

        @Test
        void registrationIsPublicAndCreatesAPendingAccount() throws Exception {
            postJson("/users/add", Map.of("username", "newbie", "password", PASSWORD, "email", "newbie@example.com", "role", "ROLE_ADMIN"))
                    .andExpect(status().isOk());

            UserEntity saved = users.getUserEntityByUsername("newbie").orElseThrow();
            assertThat(saved.getIsApproved()).isNull();
            assertThat(saved.getRole()).isNull();
            assertThat(saved.getPassword()).isNotEqualTo(PASSWORD);
        }

        @Test
        void registrationEnforcesThePasswordRules() throws Exception {
            postJson("/users/add", Map.of("username", "newbie", "password", "1", "email", "newbie@example.com"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("password")));
            postJson("/users/add", Map.of("username", "newbie", "password", PASSWORD, "email", "not-an-email"))
                    .andExpect(status().isBadRequest());
            assertThat(users.existsByUsername("newbie")).isFalse();
        }

        @Test
        void adminApprovesARegistrationWithEveryRole() throws Exception {
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));
            UserEntity asAdmin = createUser("second-admin", null);
            UserEntity asModerator = createUser("new-mod", null);
            UserEntity asUser = createUser("new-user", null);

            call(put("/users/approve").content(json(Map.of("id", asAdmin.getIdUser(), "isApproved", true,
                    "role", "ROLE_ADMIN", "permissions", List.of("ADD", "UPDATE", "DELETE"), "roomIds", List.of()))), adminJwt)
                    .andExpect(status().isOk());
            call(put("/users/approve").content(json(Map.of("id", asModerator.getIdUser(), "isApproved", true,
                    "role", "ROLE_MODERATOR", "permissions", List.of("ADD"), "roomIds", List.of(music.getIdForumRoom())))), adminJwt)
                    .andExpect(status().isOk());
            call(put("/users/approve").content(json(Map.of("id", asUser.getIdUser(), "isApproved", true,
                    "role", "ROLE_KORISNIK", "permissions", List.of(), "roomIds", List.of()))), adminJwt)
                    .andExpect(status().isOk());

            assertThat(users.findById(asAdmin.getIdUser()).orElseThrow().getRole()).isEqualTo(RoleEnum.ROLE_ADMIN);
            assertThat(users.findRoomIds(asModerator.getIdUser())).containsExactly(music.getIdForumRoom());
            assertThat(users.findById(asUser.getIdUser()).orElseThrow().isEnabled()).isTrue();
        }

        @Test
        void moderatorCannotBeApprovedWithoutARoom() throws Exception {
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));
            UserEntity pending = createUser("new-mod", null);

            call(put("/users/approve").content(json(Map.of("id", pending.getIdUser(), "isApproved", true,
                    "role", "ROLE_MODERATOR", "permissions", List.of("ADD"), "roomIds", List.of()))), adminJwt)
                    .andExpect(status().isConflict());
        }

        @Test
        void registrationCannotOverwriteAnExistingAccount() throws Exception {
            UserEntity admin = createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values());

            postJson("/users/add", Map.of("idUser", admin.getIdUser(), "username", "eve", "password", "Hacked123",
                    "email", "eve@example.com"));

            UserEntity stored = users.findById(admin.getIdUser()).orElseThrow();
            assertThat(stored.getUsername()).isEqualTo("admin");
            assertThat(stored.getRole()).isEqualTo(RoleEnum.ROLE_ADMIN);
            assertThat(stored.isEnabled()).isTrue();
        }

        @Test
        void creatingARoomCannotOverwriteAnExistingOne() throws Exception {
            String jwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(post("/forumrooms/create").content(json(Map.of("id", science.getIdForumRoom(), "name", "Hijacked"))), jwt);

            assertThat(rooms.findById(science.getIdForumRoom()).orElseThrow().getName()).isEqualTo("Science");
        }

        @Test
        void regularUserCannotManageUsers() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            call(get("/users"), jwt).andExpect(status().isForbidden()).andExpect(jsonPath("$.detail").value("Access denied"));
            call(get("/users/requests"), jwt).andExpect(status().isForbidden());
        }

        @Test
        void adminCanListUsers() throws Exception {
            String jwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));

            call(get("/users"), jwt).andExpect(status().isOk());
        }

        @Test
        void regularUserCannotCreateRooms() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            call(post("/forumrooms/create").content(json(Map.of("name", "Movies"))), jwt)
                    .andExpect(status().isForbidden());
        }

        @Test
        void moderatorCanCreateRooms() throws Exception {
            String jwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(post("/forumrooms/create").content(json(Map.of("name", "Movies"))), jwt)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Movies"));
        }

        @Test
        void moderatorNeedsTheDeletePermissionToDeleteComments() throws Exception {
            String jwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(delete("/comments/delete/1"), jwt).andExpect(status().isForbidden());
        }

        @Test
        void regularUserCannotSeeTheModerationQueue() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            call(get("/comments/requests/" + science.getIdForumRoom()), jwt).andExpect(status().isForbidden());
        }
    }

    // ── error mapping ──────────────────────────────────────────────

    @Nested
    class Errors {

        @Test
        void unknownRoomIs404() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));

            call(get("/forumrooms/99999"), jwt)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Not Found"));
        }

        @Test
        void duplicateRoomIs409() throws Exception {
            String jwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(post("/forumrooms/create").content(json(Map.of("name", "Science"))), jwt)
                    .andExpect(status().isConflict());
        }

        @Test
        void duplicateUsernameIs409() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);

            postJson("/users/add", Map.of("username", "ana", "password", PASSWORD, "email", "x@example.com"))
                    .andExpect(status().isConflict());
        }

        @Test
        void malformedJsonIs400() throws Exception {
            mvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON).content("{not json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void unknownCommentIs404() throws Exception {
            String jwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(put("/comments/approve").content(json(Map.of("id", 99999, "isApproved", 1))), jwt)
                    .andExpect(status().isNotFound());
        }
    }

    // ── data integrity ─────────────────────────────────────────────

    @Nested
    class DataIntegrity {

        private void writeComment(String jwt, String text) throws Exception {
            call(post("/comments/create")
                    .content(json(Map.of("text", text, "idForumRoom", science.getIdForumRoom()))), jwt)
                    .andExpect(status().isOk());
        }

        @Test
        void deletingAUserKeepsTheirRoomsAndRemovesTheirComments() throws Exception {
            UserEntity ana = createUser("ana", RoleEnum.ROLE_KORISNIK);
            joinRoom(ana, science);
            writeComment(signIn(ana), "Hello");
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));

            call(delete("/users/delete/" + ana.getIdUser()), adminJwt).andExpect(status().isOk());

            assertThat(users.existsByUsername("ana")).isFalse();
            assertThat(rooms.existsByIdForumRoom(science.getIdForumRoom())).isTrue();
            assertThat(comments.findAllByForumRoom_IdForumRoom(science.getIdForumRoom())).isEmpty();
        }

        @Test
        void moderatorEditKeepsTheOriginalAuthor() throws Exception {
            writeComment(signIn(createUser("ana", RoleEnum.ROLE_KORISNIK)), "Helo");
            Integer id = comments.findAllByForumRoom_IdForumRoom(science.getIdForumRoom()).get(0).getIdComment();
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.UPDATE));

            call(put("/comments/update").content(json(Map.of("idComment", id, "text", "Hello", "idForumRoom", science.getIdForumRoom()))), modJwt)
                    .andExpect(status().isOk());

            String author = jdbc.queryForObject(
                    "SELECT u.Username FROM comment c JOIN `user` u ON u.IdUser = c.IdUser WHERE c.IdComment = ?", String.class, id);
            String text = jdbc.queryForObject("SELECT Text FROM comment WHERE IdComment = ?", String.class, id);
            assertThat(author).isEqualTo("ana");
            assertThat(text).isEqualTo("Hello");
        }
    }

    // ── PDF: permissions per room, publish or forbid comments ──────

    @Nested
    class RoomScopedModeration {

        @Test
        void moderatorSeesOnlyTheQueueOfTheirRooms() throws Exception {
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(get("/comments/requests/" + science.getIdForumRoom()), modJwt).andExpect(status().isOk());
            call(get("/comments/requests/" + music.getIdForumRoom()), modJwt).andExpect(status().isForbidden());
        }

        @Test
        void moderatorCannotModerateAnotherRoom() throws Exception {
            int id = writePendingComment(music, "Jazz tonight");
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD, PermissionEnum.DELETE));

            call(put("/comments/approve").content(json(Map.of("id", id, "isApproved", 1))), modJwt).andExpect(status().isForbidden());
            call(delete("/comments/delete/" + id), modJwt).andExpect(status().isForbidden());
            assertThat(comments.findById(id).orElseThrow().getIsAllowed()).isNull();
            assertThat(recordedEvents()).contains("ACCESS_DENIED");
        }

        @Test
        void adminModeratesEveryRoom() throws Exception {
            int id = writePendingComment(music, "Jazz tonight");
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));

            call(put("/comments/approve").content(json(Map.of("id", id, "isApproved", 1))), adminJwt).andExpect(status().isOk());
        }

        @Test
        void forbiddenCommentLeavesTheQueueAndNeverAppears() throws Exception {
            int id = writePendingComment(science, "Spam spam");
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(put("/comments/approve").content(json(Map.of("id", id, "isApproved", 0))), modJwt).andExpect(status().isOk());

            call(get("/comments/requests/" + science.getIdForumRoom()), modJwt)
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
            call(get("/comments/" + science.getIdForumRoom()), modJwt)
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void commentCanBeAdjustedBeforePublishing() throws Exception {
            int id = writePendingComment(science, "Helo all");
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD, PermissionEnum.UPDATE));

            call(put("/comments/update").content(json(Map.of("idComment", id, "text", "Hello all", "idForumRoom", science.getIdForumRoom()))), modJwt)
                    .andExpect(status().isOk());
            call(put("/comments/approve").content(json(Map.of("id", id, "isApproved", 1))), modJwt).andExpect(status().isOk());

            call(get("/comments/" + science.getIdForumRoom()), modJwt)
                    .andExpect(jsonPath("$[0].text").value("Hello all"))
                    .andExpect(jsonPath("$[0].user.username").value("writer"))
                    .andExpect(jsonPath("$[0].createdAt").exists());
        }

        @Test
        void meListsTheRoomsOfTheUser() throws Exception {
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(get("/auth/me"), modJwt).andExpect(jsonPath("$.rooms[0]").value(science.getIdForumRoom()));
        }
    }

    // ── PDF: the admin grants and revokes access and permissions ──

    @Nested
    class AccessManagement {

        @Test
        void blockedUserIsSignedOutAndCannotSignInAgain() throws Exception {
            UserEntity ana = createUser("ana", RoleEnum.ROLE_KORISNIK);
            String anaJwt = signIn(ana);
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));

            call(put("/users/access").content(json(Map.of("id", ana.getIdUser(), "allowed", false))), adminJwt)
                    .andExpect(status().isOk());

            call(get("/forumrooms"), anaJwt).andExpect(status().isUnauthorized());
            postJson("/auth/login", Map.of("username", "ana", "password", PASSWORD)).andExpect(status().isForbidden());
            verify(emailService).sendEmail(eq("ana@example.com"), eq("ana"), anyString(), contains("zabranjen"));
            assertThat(recordedEvents()).contains("USER_BLOCKED", "LOGIN_NOT_APPROVED");
        }

        @Test
        void unblockedUserCanSignInWithTheSameRole() throws Exception {
            UserEntity ana = createUser("ana", RoleEnum.ROLE_KORISNIK);
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));
            call(put("/users/access").content(json(Map.of("id", ana.getIdUser(), "allowed", false))), adminJwt);

            call(put("/users/access").content(json(Map.of("id", ana.getIdUser(), "allowed", true))), adminJwt)
                    .andExpect(status().isOk());

            postJson("/auth/login", Map.of("username", "ana", "password", PASSWORD)).andExpect(status().isOk());
        }

        @Test
        void adminCannotLockThemselvesOut() throws Exception {
            UserEntity admin = createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values());

            call(put("/users/access").content(json(Map.of("id", admin.getIdUser(), "allowed", false))), signIn(admin))
                    .andExpect(status().isConflict());
        }

        @Test
        void onlyAdminsManageAccess() throws Exception {
            UserEntity ana = createUser("ana", RoleEnum.ROLE_KORISNIK);
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.values()));

            call(put("/users/access").content(json(Map.of("id", ana.getIdUser(), "allowed", false))), modJwt)
                    .andExpect(status().isForbidden());
        }

        @Test
        void removedPermissionStopsWorkingAtOnce() throws Exception {
            UserEntity mod = createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD, PermissionEnum.DELETE);
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));
            call(put("/users/changeRole").content(json(Map.of("id", mod.getIdUser(), "role", "ROLE_MODERATOR",
                    "permissions", List.of("ADD"), "roomIds", List.of(science.getIdForumRoom())))), adminJwt)
                    .andExpect(status().isOk());

            int id = writePendingComment(science, "Delete me");
            call(delete("/comments/delete/" + id), signIn(users.findById(mod.getIdUser()).orElseThrow()))
                    .andExpect(status().isForbidden());
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM permission WHERE IdUser = ?", Integer.class, mod.getIdUser()))
                    .isEqualTo(1);
        }

        @Test
        void adminAssignsTheRoomsAModeratorManages() throws Exception {
            UserEntity mod = createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD);
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));

            call(put("/users/changeRole").content(json(Map.of("id", mod.getIdUser(), "role", "ROLE_MODERATOR",
                    "permissions", List.of("ADD"), "roomIds", List.of(music.getIdForumRoom())))), adminJwt)
                    .andExpect(status().isOk());

            String modJwt = signIn(users.findById(mod.getIdUser()).orElseThrow());
            call(get("/comments/requests/" + music.getIdForumRoom()), modJwt).andExpect(status().isOk());
            call(get("/comments/requests/" + science.getIdForumRoom()), modJwt).andExpect(status().isForbidden());
            call(get("/users/approved/1"), adminJwt)
                    .andExpect(jsonPath("$[?(@.username == 'mod')].roomIds[0]").value(music.getIdForumRoom()));
        }
    }

    // ── PDF: SIEM logs every security-sensitive request ───────────

    @Nested
    class Siem {

        @Test
        void failedSignInIsRecordedWithTheClientAddress() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);

            mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("username", "ana", "password", "wrong")))
                    .with(request -> { request.setRemoteAddr("198.51.100.4"); return request; }));

            SecurityEventEntity event = securityEvents.findAll().get(0);
            assertThat(event.getType()).isEqualTo("LOGIN_FAILED");
            assertThat(event.getUsername()).isEqualTo("ana");
            assertThat(event.getIpAddress()).isEqualTo("198.51.100.4");
            assertThat(event.getCreatedAt()).isNotNull();
        }

        @Test
        void fullSignInLeavesAnAuditTrail() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);
            postJson("/auth/login", Map.of("username", "ana", "password", PASSWORD));
            MvcResult result = postJson("/auth/token", Map.of("username", "ana", "password", PASSWORD, "token", emailedCode())).andReturn();
            call(post("/auth/logout"), cookieValue(sessionCookie(result)));

            assertThat(recordedEvents()).containsSubsequence("LOGIN_CODE_SENT", "LOGIN_SUCCESS", "LOGOUT");
        }

        @Test
        void attacksAndForgedSessionsAreRecorded() throws Exception {
            String jwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));
            call(post("/comments/create").content(json(Map.of("text", "<script>x</script>", "idForumRoom", science.getIdForumRoom()))), jwt);
            call(get("/forumrooms"), "not.a.jwt");

            assertThat(recordedEvents()).contains("WAF_BLOCKED", "SESSION_REJECTED");
        }

        @Test
        void adminReadsTheSecurityLogOthersCannot() throws Exception {
            createUser("ana", RoleEnum.ROLE_KORISNIK);
            postJson("/auth/login", Map.of("username", "ana", "password", "wrong"));
            String adminJwt = signIn(createUser("admin", RoleEnum.ROLE_ADMIN, PermissionEnum.values()));
            String userJwt = signIn(createUser("bob", RoleEnum.ROLE_KORISNIK));

            call(get("/security-events?type=LOGIN_FAILED"), adminJwt)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].type").value("LOGIN_FAILED"))
                    .andExpect(jsonPath("$[0].username").value("ana"));
            call(get("/security-events"), userJwt).andExpect(status().isForbidden());
        }
    }

    // ── PDF: sign-in with an external OAuth2 account ──────────────

    @Nested
    class GoogleSignIn {

        private void googleSays(String token, String email, String name) {
            when(googleTokenVerifier.verify(token)).thenReturn(new GoogleTokenVerifier.GoogleProfile(email, name));
        }

        @Test
        void firstGoogleSignInRegistersAPendingAccount() throws Exception {
            googleSays("token", "ana@gmail.com", "Ana");

            postJson("/auth/oauth", Map.of("accessToken", "token"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("ana@gmail.com"));

            UserEntity ana = users.findByEmail("ana@gmail.com").orElseThrow();
            assertThat(ana.isEnabled()).isFalse();
            postJson("/auth/oauth", Map.of("accessToken", "token")).andExpect(status().isForbidden());
        }

        @Test
        void approvedGoogleAccountSignsInWithTheEmailedCode() throws Exception {
            UserEntity ana = createUser("ana", RoleEnum.ROLE_KORISNIK);
            ana.setEmail("ana@gmail.com");
            ana.setSource(org.unibl.etfbl.ChatRoom.enums.RegristrationSourceEnum.GOOGLE);
            users.saveAndFlush(ana);
            googleSays("token", "ana@gmail.com", "Ana");

            postJson("/auth/oauth", Map.of("accessToken", "token")).andExpect(status().isOk());
            MvcResult result = postJson("/auth/oauth/token", Map.of("email", "ana@gmail.com", "token", emailedCode(), "type", "GOOGLE"))
                    .andExpect(status().isOk())
                    .andReturn();

            call(get("/forumrooms"), cookieValue(sessionCookie(result))).andExpect(status().isOk());
        }

        @Test
        void tokenGoogleDoesNotConfirmIsRefused() throws Exception {
            when(googleTokenVerifier.verify("forged")).thenThrow(new BadCredentialsException("Invalid Google token"));

            postJson("/auth/oauth", Map.of("accessToken", "forged")).andExpect(status().isUnauthorized());
            assertThat(users.findByEmail("victim@gmail.com")).isEmpty();
        }
    }

    // ── comment moderation ─────────────────────────────────────────

    @Nested
    class Moderation {

        private JsonNode commentsIn(ForumRoomEntity room, String jwt) throws Exception {
            String body = call(get("/comments/" + room.getIdForumRoom()), jwt)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            return objectMapper.readTree(body);
        }

        @Test
        void newCommentIsHiddenUntilAModeratorApprovesIt() throws Exception {
            String userJwt = signIn(createUser("ana", RoleEnum.ROLE_KORISNIK));
            String modJwt = signIn(createUser("mod", RoleEnum.ROLE_MODERATOR, PermissionEnum.ADD));

            call(post("/comments/create")
                    .content(json(Map.of("text", "Hello everyone", "idForumRoom", science.getIdForumRoom()))), userJwt)
                    .andExpect(status().isOk());
            assertThat(commentsIn(science, userJwt)).isEmpty();

            String queue = call(get("/comments/requests/" + science.getIdForumRoom()), modJwt)
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            int idComment = objectMapper.readTree(queue).get(0).get("idComment").asInt();

            call(put("/comments/approve").content(json(Map.of("id", idComment, "isApproved", 1))), modJwt)
                    .andExpect(status().isOk());

            JsonNode visible = commentsIn(science, userJwt);
            assertThat(visible).hasSize(1);
            assertThat(visible.get(0).get("text").asText()).isEqualTo("Hello everyone");
            assertThat(visible.get(0).get("user").get("username").asText()).isEqualTo("ana");
        }
    }
}
