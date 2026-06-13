package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.response.AuthResponse;
import com.practicasDeDesarrollo.backend.dto.request.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.request.UpdateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.dto.response.PublicacionResponse;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.mapper.UsuarioMapper;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
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

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final PublicacionMapper publicacionMapper;

    public AuthResponse createUsuario(@NonNull CreateUsuarioRequest request, RolUsuario rol) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está en uso");
        }

        Usuario usuario = Usuario.builder().nombre(request.nombre()).email(request.email()).password(passwordEncoder.encode(request.password())).icono(request.icono()).rol(rol).build();

        usuarioRepository.save(usuario);
        String token = jwtService.generateToken(usuario);

        return buildAuthResponse(usuario, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String token = jwtService.generateToken(usuario);

        return buildAuthResponse(usuario, token);
    }

    public UsuarioResponse actualizarUsuario(@NonNull UpdateUsuarioRequest request, Usuario usuario) {
        if (request.nombre() != null) {
            usuario.setNombre(request.nombre());
        }
        if (request.icono() != null) {
            usuario.setIcono(request.icono());
        }

        Usuario saved = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> getUsuariosByRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol).stream().map(usuarioMapper::toResponse).toList();
    }

    public PublicacionResponse agregarFavorito(Long publicacionId, Usuario usuario) {
        Usuario attachedUsuario = obtenerUsuarioAutenticado(usuario.getId());
        Publicacion publicacion = publicacionRepository.findById(publicacionId).orElseThrow(() -> new EntityNotFoundException("Publicacion no encontrada"));

        attachedUsuario.getFavoritos().add(publicacion);

        return publicacionMapper.toResponse(publicacion, true);
    }

    public void eliminarFavorito(Long publicacionId, Usuario usuario) {
        Usuario attachedUsuario = obtenerUsuarioAutenticado(usuario.getId());
        Publicacion publicacionProxy = publicacionRepository.getReferenceById(publicacionId);

        attachedUsuario.getFavoritos().remove(publicacionProxy);
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> obtenerFavoritos(Usuario usuario) {
        Usuario attachedUsuario = obtenerUsuarioAutenticado(usuario.getId());

        return attachedUsuario.getFavoritos().stream().map(p -> publicacionMapper.toResponse(p, true)).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarUsuarioQueGuardaronPublicacion(Long publicacionId) {
        return usuarioRepository.findByFavoritos_Id(publicacionId).stream().map(usuarioMapper::toResponse).toList();
    }

    // --- MÉTODOS PRIVADOS DE SOPORTE ---

    private AuthResponse buildAuthResponse(Usuario usuario, String token) {
        return new AuthResponse(token, usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getIcono(), usuario.getRol().name());
    }

    private Usuario obtenerUsuarioAutenticado(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

}