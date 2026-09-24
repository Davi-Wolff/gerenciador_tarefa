package com.a3testes.gerenciador_tarefa.service;

import com.a3testes.gerenciador_tarefa.DTO.request.TarefaRequest;
import com.a3testes.gerenciador_tarefa.DTO.response.TarefaDto;
import com.a3testes.gerenciador_tarefa.model.Tarefa;
import com.a3testes.gerenciador_tarefa.repository.TarefaRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class TarefaService {
    
    private TarefaRepository tarefaRepository;
    
    private TarefaDto findById(Long id){
        return tarefaRepository.findById(id);
    }


    public TarefaDto save(TarefaRequest tarefa) {
        return tarefaRepository.save(tarefa);
    }

    public TarefaDto update(TarefaRequest tarefa) {

    }

    public void delete(Long id) {
    }
}
