package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.ModuloDTO;
import com.softcorp.SoftCorpBackend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Genera y valida los tokens JWT
@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(Usuario usuario, List<ModuloDTO> modulos) {
        // Datos que viajan dentro del token (el frontend puede leerlos)
        Map<String, Object> claims = new HashMap<>();
        claims.put("personaId", usuario.getPersona().getIdPersona());
        claims.put("names", usuario.getPersona().getNombres());
        claims.put("lastNames", usuario.getPersona().getApellidoPaterno() + " " + usuario.getPersona().getApellidoMaterno());
        claims.put("role", usuario.getRol().getCodigo());
        claims.put("roleDescription", usuario.getRol().getDescripcion());
        claims.put("modules", modulos);   // opciones del menú según el rol

        return Jwts.builder()
                .claims(claims)
                .subject(usuario.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Devuelve el usuario del token. Lanza JwtException si el token es inválido o expiró.
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, Usuario usuario) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject().equals(usuario.getUsername())
                && claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
