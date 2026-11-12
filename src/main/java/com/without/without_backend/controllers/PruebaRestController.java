package com.without.without_backend.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
@RequestMapping("/prueba")
public class PruebaRestController {

    @GetMapping
    public String prueba() {
        return "Acceso autorizado, para todo el público";
    }
}