package com.softcorp.SoftCorpBackend.security;

import com.softcorp.SoftCorpBackend.exception.GlobalExceptionHandler;
import com.softcorp.SoftCorpBackend.repository.UsuarioRepository;
import com.softcorp.SoftCorpBackend.service.JwtUtil;
import com.softcorp.SoftCorpBackend.util.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtExceptionHandlingTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    // Verifica que el filtro delegue un token expirado al HandlerExceptionResolver (en vez de devolver 500)
    @Test
    public void testFilterDelegatesExpiredJwtExceptionToResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        request.addHeader("Authorization", "Bearer expired_jwt_token");

        ExpiredJwtException expiredException = new ExpiredJwtException(null, null, "JWT expired");
        when(jwtUtil.extractUsername("expired_jwt_token")).thenThrow(expiredException);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), eq(expiredException));
    }

    // Verifica que un token con firma inválida también pase al resolver global
    @Test
    public void testFilterDelegatesInvalidJwtExceptionToResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        request.addHeader("Authorization", "Bearer invalid_jwt_token");

        JwtException jwtException = new JwtException("Invalid token signature");
        when(jwtUtil.extractUsername("invalid_jwt_token")).thenThrow(jwtException);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), eq(null), eq(jwtException));
    }

    // Valida la respuesta JSON 401 para ExpiredJwtException
    @Test
    public void testGlobalExceptionHandlerExpiredJwt() {
        ExpiredJwtException ex = new ExpiredJwtException(null, null, "Token expired");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleExpiredJwt(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("El token JWT ha expirado", response.getBody().getMessage());
    }

    // Valida la respuesta JSON 401 para credenciales incorrectas
    @Test
    public void testGlobalExceptionHandlerBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Contraseña incorrecta");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAuthentication(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Contraseña incorrecta", response.getBody().getMessage());
    }

    // Valida la traducción de errores FK de PostgreSQL a 400 Bad Request
    @Test
    public void testGlobalExceptionHandlerDataIntegrityViolationForeignKey() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException("ERROR: update or delete on table \"incidencia\" violates foreign key constraint \"fk_informe_incidencia\" on table \"informe_tecnico\"")
        );
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(
                "No se puede eliminar ni modificar el registro porque está siendo referenciado en otras tablas.",
                response.getBody().getMessage()
        );
    }
}
