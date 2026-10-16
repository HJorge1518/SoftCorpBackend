package com.softcorp.SoftCorpBackend.service;

import com.softcorp.SoftCorpBackend.dto.AuthResponseDTO;
import com.softcorp.SoftCorpBackend.dto.LoginRequestDTO;
import com.softcorp.SoftCorpBackend.dto.ModuloDTO;
import com.softcorp.SoftCorpBackend.dto.PerfilResponseDTO;
import com.softcorp.SoftCorpBackend.dto.PersonaRequest;
import com.softcorp.SoftCorpBackend.dto.RegisterRequestDTO;
import com.softcorp.SoftCorpBackend.model.Persona;
import com.softcorp.SoftCorpBackend.model.Rol;
import com.softcorp.SoftCorpBackend.model.Usuario;
import com.softcorp.SoftCorpBackend.repository.ModuloRepository;
import com.softcorp.SoftCorpBackend.repository.PersonaRepository;
import com.softcorp.SoftCorpBackend.repository.RolRepository;
import com.softcorp.SoftCorpBackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final RolRepository rolRepository;
    private final ModuloRepository moduloRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Crea la persona y su usuario, y devuelve un token
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        String username = request.getUsername().trim();
        if (usuarioRepository.existsByUsername(username))
            throw new IllegalArgumentException("Ya existe un usuario con ese nombre");

        PersonaRequest personaDTO = request.getPersona();
        if (personaRepository.existsByNumDocumento(personaDTO.getNumDocumento()))
            throw new IllegalArgumentException("Ya existe una persona con ese número de documento");

        Rol rol = rolRepository.findByCodigo(request.getCodigoRol().trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Rol no válido. Use JEFE, TECNICO o SISTEMAS"));

        Persona persona = personaRepository.save(PersonaRequest.toEntity(personaDTO));

        Usuario usuario = Usuario.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .persona(persona)
                .rol(rol)
                .build();
        usuarioRepository.save(usuario);

        return crearRespuesta(usuario);
    }

    // Valida usuario y contraseña, y devuelve un token
    public AuthResponseDTO login(LoginRequestDTO request) {
        // Mismo mensaje si falla el usuario o la contraseña, para no dar pistas
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername().trim())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash()))
            throw new BadCredentialsException("Usuario o contraseña incorrectos");

        return crearRespuesta(usuario);
    }

    // Datos del usuario que inició sesión (se obtiene del token)
    public PerfilResponseDTO perfil(Usuario usuario) {
        Persona persona = usuario.getPersona();
        return PerfilResponseDTO.builder()
                .username(usuario.getUsername())
                .nombres(persona.getNombres())
                .apellidos(persona.getApellidoPaterno() + " " + persona.getApellidoMaterno())
                .rol(usuario.getRol().getCodigo())
                .rolDescripcion(usuario.getRol().getDescripcion())
                .modulos(modulosDelRol(usuario.getRol()))
                .build();
    }

    private AuthResponseDTO crearRespuesta(Usuario usuario) {
        String token = jwtUtil.generateToken(usuario, modulosDelRol(usuario.getRol()));
        return AuthResponseDTO.builder().token(token).build();
    }

    private List<ModuloDTO> modulosDelRol(Rol rol) {
        return moduloRepository.findByRolId(rol.getIdRol()).stream()
                .map(ModuloDTO::fromEntity)
                .toList();
    }
}
