package com.softcorp.SoftCorpBackend.security;

import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.UsuarioRepository;
import com.softcorp.SoftCorpBackend.service.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UsuarioRepository usuarioRepository,
            @Qualifier("handlerExceptionResolver") @Lazy HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

                if (usuario != null && jwtUtil.isTokenValid(jwt, usuario)) {
                    var permisos = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getCodigo()));
                    var authToken = new UsernamePasswordAuthenticationToken(usuario, null, permisos);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request, response, null, ex);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
