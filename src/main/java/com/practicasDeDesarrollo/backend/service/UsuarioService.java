package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.CreateUsuarioRequest;
import com.practicasDeDesarrollo.backend.dto.UpdateUsuarioRequest;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.entity.enums.RolUsuario;
import com.practicasDeDesarrollo.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario createUsuario(CreateUsuarioRequest request, RolUsuario rol) {
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

        return usuarioRepository.save(usuario);
    }


    public Usuario actualizarUsuario(Long id, UpdateUsuarioRequest request) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        Usuario usuarioActualizado = usuarioExistente.toBuilder()
                .nombre(request.nombre())
                .icono(request.icono())
                // El email, password y rol NO se tocan aquí
                .build();

        return usuarioRepository.save(usuarioActualizado);
    }


}