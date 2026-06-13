package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.response.*;
import com.practicasDeDesarrollo.backend.mapper.PropiedadMapper;
import com.practicasDeDesarrollo.backend.mapper.UsuarioMapper;
import com.practicasDeDesarrollo.backend.repository.CompraRepository;
import com.practicasDeDesarrollo.backend.repository.ResenaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final CompraRepository compraRepository;
    private final ResenaRepository resenaRepository;
    private final UsuarioMapper usuarioMapper;
    private final PropiedadMapper propiedadMapper;

    public List<TopUserResponse> getTopUsuarios() {
        return compraRepository.findTop5UsuariosPorCompra(PageRequest.of(0, 5))
                .stream()
                .map(projection -> new TopUserResponse(
                        usuarioMapper.toResponse(projection.getUsuario()),
                        projection.getComprasCount()
                ))
                .toList();
    }

    public List<TopPropertyResponse> getTopPublicaciones() {
        return resenaRepository.findTop5PublicacionesPorRating(PageRequest.of(0, 5))
                .stream()
                .map(projection -> new TopPropertyResponse(
                        propiedadMapper.toResponse(projection.getPropiedad()),
                        projection.getAverageRating()
                ))
                .toList();
    }

    public List<TopAgencyResponse> getTopInmobiliarias() {
        return compraRepository.findTop5InmobiliariasPorVenta(PageRequest.of(0, 5))
                .stream()
                .map(projection -> new TopAgencyResponse(
                        usuarioMapper.toResponse(projection.getAgencia()),
                        projection.getVentasCount()
                ))
                .toList();
    }
}
