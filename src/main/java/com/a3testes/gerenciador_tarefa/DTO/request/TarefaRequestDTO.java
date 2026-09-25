package com.a3testes.gerenciador_tarefa.DTO.request;

import javax.validation.constraints.NotBlank;

public record TarefaRequestDTO(
  @NotBlank String titulo,
  String descricao
){}
