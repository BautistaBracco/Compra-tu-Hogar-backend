package com.practicasDeDesarrollo.backend.service;

import com.practicasDeDesarrollo.backend.dto.response.CompraResponse;
import com.practicasDeDesarrollo.backend.dto.response.UsuarioResponse;
import com.practicasDeDesarrollo.backend.entity.Compra;
import com.practicasDeDesarrollo.backend.entity.Publicacion;
import com.practicasDeDesarrollo.backend.entity.Usuario;
import com.practicasDeDesarrollo.backend.exception.ConflictException;
import com.practicasDeDesarrollo.backend.mapper.PublicacionMapper;
import com.practicasDeDesarrollo.backend.mapper.UsuarioMapper;
import com.practicasDeDesarrollo.backend.repository.CompraRepository;
import com.practicasDeDesarrollo.backend.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompraService {

    private final CompraRepository compraRepository;
    private final PublicacionRepository publicacionRepository;

    private final PublicacionMapper publicacionMapper;
    private final UsuarioMapper usuarioMapper;

    @Transactional(readOnly = true)
    public List<CompraResponse> obtenerVentas(Usuario usuario) {
        List<Compra> ventas = compraRepository.findByPublicacionInmobiliaria(usuario);

        return ventas.stream()
                .map(compra -> new CompraResponse(
                        compra.getId(),
                        compra.getPrecioCompra(),
                        compra.getCreadoEn(),
                        usuarioMapper.toResponse(compra.getComprador()),
                        publicacionMapper.toResponse(compra.getPublicacion(), false)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerClientes(Usuario usuario) {
        List<Compra> ventas = compraRepository.findByPublicacionInmobiliaria(usuario);

        return ventas.stream()
                .map(compra -> usuarioMapper.toResponse(compra.getComprador()))
                .distinct()
                .toList();
    }

    public void comprar(Long publicacionId, Usuario comprador) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new RuntimeException("Publicación no encontrada")); // O tu ResourceNotFoundException

        if (publicacion.getPropiedad().getVendida()) {
            throw new ConflictException("Esta propiedad ya ha sido vendida.");
        }

        Compra nuevaCompra = Compra.builder()
                .precioCompra(publicacion.getPrecio())
                .comprador(comprador)
                .publicacion(publicacion)
                .build();

        compraRepository.save(nuevaCompra);

        publicacion.getPropiedad().setVendida(true);

        publicacionRepository.save(publicacion);
    }

    public List<CompraResponse> obtenerCompras(Usuario usuario) {
        List<Compra> compras = compraRepository.findByComprador(usuario);
        return compras.stream()
                .map(compra -> new CompraResponse(
                        compra.getId(),
                        compra.getPrecioCompra(),
                        compra.getCreadoEn(),
                        usuarioMapper.toResponse(compra.getComprador()),
                        publicacionMapper.toResponse(compra.getPublicacion(), false)
                ))
                .toList();

    }
}