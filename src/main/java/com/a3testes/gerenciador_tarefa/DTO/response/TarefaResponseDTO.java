package com.a3testes.gerenciador_tarefa.DTO.response;

import java.time.LocalDateTime;

public record TarefaResponseDTO(
        Long id,
        String titulo,
        String descricao,
        boolean concluida,
        LocalDateTime dataCriacao,
        Long userId
){}
