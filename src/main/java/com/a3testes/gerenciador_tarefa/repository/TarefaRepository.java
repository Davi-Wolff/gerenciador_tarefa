package com.a3testes.gerenciador_tarefa.repository;

import com.a3testes.gerenciador_tarefa.model.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa,Long> {
}
