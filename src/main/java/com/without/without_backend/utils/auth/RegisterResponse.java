package com.without.without_backend.utils.auth;

import com.without.without_backend.dto.cliente.ClienteResponse;

public record RegisterResponse(
    ClienteResponse cliente,
    String status,
    String message
) {

}
