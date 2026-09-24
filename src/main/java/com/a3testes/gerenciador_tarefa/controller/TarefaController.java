package com.a3testes.gerenciador_tarefa.controller;

import com.a3testes.gerenciador_tarefa.DTO.request.TarefaRequest;
import com.a3testes.gerenciador_tarefa.DTO.response.TarefaDto;
import com.a3testes.gerenciador_tarefa.service.TarefaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class TarefaController {

    private TarefaService tarefaService;

    @GetMapping("/{id}")
    private ResponseEntity<TarefaDto> findById(@PathVariable Long id){
        return ResponseEntity.ok().body(tarefaService.findById(id));
    }

    @PostMapping
    private ResponseEntity<TarefaDto> postarTarefa(@RequestBody TarefaRequest tarefa){
        tarefaService.save(tarefa);
        return ResponseEntity.status(201).build();
    }

    @PutMapping
    private ResponseEntity<TarefaDto> editarTarefa(@RequestBody TarefaRequest tarefa){
        TarefaDto tarefa = tarefaService.update(tarefa);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteTarefa(@PathVariable Long id){
        tarefaService.delete(id);
        return ResponseEntity.status(204).build();
    }
}
