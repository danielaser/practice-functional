package com.practice.functional.practice.functional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductDTO(


    String id,

    @NotBlank(message = "El nombre no puede estar vacio")
    String name,

    @Positive(message = "El precio debe ser mayor a 0")
    Double price
) {
}
