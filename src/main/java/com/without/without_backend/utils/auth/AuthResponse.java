package com.without.without_backend.utils.auth;

import com.fasterxml.jackson.annotation.JsonProperty;


public record AuthResponse(
    @JsonProperty("access_token") String accessToken
){
    
}
