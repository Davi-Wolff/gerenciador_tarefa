package com.a3testes.gerenciador_tarefa.DTO.response;

import org.antlr.v4.runtime.misc.NotNull;

public record UserDto (
        @NotNull
        String username,
        @NotNull
        String email)
{}
