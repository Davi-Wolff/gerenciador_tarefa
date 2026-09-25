package com.a3testes.gerenciador_tarefa.controller;

import com.a3testes.gerenciador_tarefa.DTO.request.TarefaRequestDTO;
import com.a3testes.gerenciador_tarefa.DTO.response.TarefaResponseDTO;
import com.a3testes.gerenciador_tarefa.service.TarefaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarefa")
public class TarefaController {

    private TarefaService tarefaService;

    @GetMapping("user/{userId}")
    private ResponseEntity<TarefaResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok().body(tarefaService.findById(id));
    }

    @PostMapping("?userId={id}")
    private ResponseEntity<TarefaResponseDTO> postarTarefa(@PathVariable Long id, @RequestBody TarefaRequestDTO tarefa){
        tarefaService.save(userId,tarefa);
        return ResponseEntity.status(201).build();
    }

    @PutMapping
    private ResponseEntity<TarefaResponseDTO> editarTarefa(@RequestBody TarefaRequestDTO tarefa){
        TarefaResponseDTO tarefa = tarefaService.update(tarefa);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteTarefa(@PathVariable Long id){
        tarefaService.delete(id);
        return ResponseEntity.status(204).build();
    }
}
