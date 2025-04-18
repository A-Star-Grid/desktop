package org.example.core.services;

import org.example.core.clients.ServerClient;
import org.example.core.models.User;
import org.example.core.models.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock ServerClient serverClient;
    @Mock PreferencesStorage preferencesStorage;

    AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(serverClient, preferencesStorage);
    }

    @Test
    @DisplayName("authenticate – успешный логин: токены сохраняются, 200 OK")
    void authenticate_success() {
        var loginResponse = new LoginResponse("access‑123", "refresh‑456");
        when(serverClient.login("alice", "secret"))
                .thenReturn(Mono.just(loginResponse));

        var response = service.authenticate("alice", "secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access‑123", response.getBody());

        verify(preferencesStorage).saveTokens("access‑123", "refresh‑456");
        verifyNoMoreInteractions(preferencesStorage);
    }

    @Test
    @DisplayName("authenticate – Mono.empty(): возвращается 401, токены не сохраняются")
    void authenticate_failure() {
        when(serverClient.login("bob", "wrong"))
                .thenReturn(Mono.empty());                // block() вернёт null

        var response = service.authenticate("bob", "wrong");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication failed", response.getBody());

        verify(preferencesStorage, never()).saveTokens(any(), any());
    }

    @Test
    @DisplayName("getUser – проксирует результат от ServerClient")
    void getUser_returnsValueFromServerClient() {
        var expected = new User("Eve");
        when(serverClient.getUser()).thenReturn(Mono.just(expected));

        var actual = service.getUser();

        assertSame(expected, actual);
        verify(serverClient).getUser();
    }
}
