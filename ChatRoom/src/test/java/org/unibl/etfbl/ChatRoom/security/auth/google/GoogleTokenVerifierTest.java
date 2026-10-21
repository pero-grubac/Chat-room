package org.unibl.etfbl.ChatRoom.security.auth.google;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Google is replaced by a mock server; nothing leaves the machine. */
class GoogleTokenVerifierTest {

    private static final String TOKEN_INFO = "https://oauth2.googleapis.com/tokeninfo?access_token=good-token";
    private static final String USER_INFO = "https://openidconnect.googleapis.com/v1/userinfo";

    private MockRestServiceServer google;
    private GoogleTokenVerifier verifier;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        google = MockRestServiceServer.bindTo(builder).build();
        verifier = new GoogleTokenVerifier(builder);
        ReflectionTestUtils.setField(verifier, "clientId", "our-client");
    }

    private void tokenIssuedTo(String audience) {
        google.expect(requestTo(TOKEN_INFO))
                .andRespond(withSuccess("{\"aud\":\"" + audience + "\",\"expires_in\":\"3599\"}", MediaType.APPLICATION_JSON));
    }

    @Test
    void validTokenOfThisAppGivesTheVerifiedProfile() {
        tokenIssuedTo("our-client");
        google.expect(requestTo(USER_INFO))
                .andExpect(header("Authorization", "Bearer good-token"))
                .andRespond(withSuccess("{\"email\":\"ana@gmail.com\",\"email_verified\":true,\"name\":\"Ana\"}", MediaType.APPLICATION_JSON));

        GoogleTokenVerifier.GoogleProfile profile = verifier.verify("good-token");

        assertThat(profile.email()).isEqualTo("ana@gmail.com");
        assertThat(profile.name()).isEqualTo("Ana");
        google.verify();
    }

    @Test
    void tokenIssuedToAnotherAppIsRefused() {
        tokenIssuedTo("someone-elses-client");

        assertThatThrownBy(() -> verifier.verify("good-token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("not issued to this application");
    }

    @Test
    void unverifiedEmailIsRefused() {
        tokenIssuedTo("our-client");
        google.expect(requestTo(USER_INFO))
                .andRespond(withSuccess("{\"email\":\"ana@gmail.com\",\"email_verified\":false,\"name\":\"Ana\"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> verifier.verify("good-token")).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void tokenGoogleRejectsIsRefused() {
        google.expect(requestTo(TOKEN_INFO)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> verifier.verify("good-token"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid Google token");
    }

    @Test
    void missingTokenOrClientIdNeverCallsGoogle() {
        assertThatThrownBy(() -> verifier.verify(" ")).isInstanceOf(BadCredentialsException.class);
        ReflectionTestUtils.setField(verifier, "clientId", "");
        assertThatThrownBy(() -> verifier.verify("good-token")).isInstanceOf(BadCredentialsException.class);
        google.verify();
    }
}
