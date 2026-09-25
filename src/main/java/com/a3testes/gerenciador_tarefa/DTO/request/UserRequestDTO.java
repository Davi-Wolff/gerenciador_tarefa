package com.a3testes.gerenciador_tarefa.DTO.request;

import org.antlr.v4.runtime.misc.NotNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record UserRequestDTO(
    @NotNull @NotBlank
    String username,

    @NotBlank @NotNull @Email String email,

    @NotNull @Size(min = 6) String senha
){}