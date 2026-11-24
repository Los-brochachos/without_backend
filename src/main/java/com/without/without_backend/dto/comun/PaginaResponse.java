package com.without.without_backend.dto.comun;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Respuesta paginada estable para el frontend.
 * Evita exponer la estructura interna de Spring Data (Page/Pageable) en la API.
 *
 * @param contenido      registros de la página solicitada
 * @param pagina         número de página actual (empieza en 0)
 * @param tamanio        cantidad máxima de registros por página
 * @param totalElementos total de registros que cumplen el filtro
 * @param totalPaginas   total de páginas disponibles
 * @param primera        true si es la primera página
 * @param ultima         true si es la última página
 */
public record PaginaResponse<T>(
    List<T> contenido,
    int pagina,
    int tamanio,
    long totalElementos,
    int totalPaginas,
    boolean primera,
    boolean ultima
) {
    public static <T> PaginaResponse<T> de(Page<T> page) {
        return new PaginaResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
