package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.request.UpdateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.dto.response.PropiedadResponse;
import com.practicasDeDesarrollo.backend.entity.Propiedad;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.repository.PropiedadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PropiedadRepository propiedadRepository;
    private final PropiedadService propiedadService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse createUsuario(@NonNull CreateUsuarioRequest request, RolUsuario rol) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        String passwordHasheada = passwordEncoder.encode(request.password());

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .email(request.email())
                .password(passwordHasheada)
                .icono(request.icono())
                .rol(rol)
                .build();

        usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario);

        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getIcono(),
                usuario.getRol().name()
        );

    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String token = jwtService.generateToken(usuario);

        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getIcono(),
                usuario.getRol().name()
        );
    }


    public UsuarioResponse actualizarUsuario(@NonNull UpdateUsuarioRequest request, Usuario usuario) {
        if (request.nombre() != null) {
            usuario.setNombre(request.nombre());
        }
        if (request.icono() != null) {
            usuario.setIcono(request.icono());
        }

        Usuario saved = usuarioRepository.save(usuario);
        return new UsuarioResponse(saved.getId(), saved.getNombre(), saved.getEmail(), saved.getIcono());
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> getUsuariosByRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol)
                .stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getIcono()))
                .toList();
    }

    public PropiedadResponse agregarFavorito(Long propiedadId, Usuario usuario) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new EntityNotFoundException("Propiedad no encontrada"));
        
        usuario.getFavoritos().add(propiedad);
        usuarioRepository.save(usuario);
        return propiedadService.mapToResponse(propiedad, usuario);
    }

    public void eliminarFavorito(Long propiedadId, Usuario usuario) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new EntityNotFoundException("Propiedad no encontrada"));
        
        usuario.getFavoritos().remove(propiedad);
        usuarioRepository.save(usuario);
    }

    public List<PropiedadResponse> obtenerFavoritos(Usuario usuario) {
        return usuario.getFavoritos().stream()
                .map(propiedad -> propiedadService.mapToResponse(propiedad, usuario))
                .collect(Collectors.toList());
    }
}