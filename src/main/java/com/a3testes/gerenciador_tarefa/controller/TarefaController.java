package com.a3testes.gerenciador_tarefa.controller;

import com.a3testes.gerenciador_tarefa.DTO.request.TarefaRequestDTO;
import com.a3testes.gerenciador_tarefa.DTO.response.TarefaResponseDTO;
import com.a3testes.gerenciador_tarefa.service.TarefaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tarefa")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    public ResponseEntity<TarefaResponseDTO> postarTarefa(@RequestParam Long userId,
                                                          @RequestBody TarefaRequestDTO tarefa) {
        TarefaResponseDTO criada = tarefaService.criar(tarefa, userId);
        return ResponseEntity.status(201).body(criada);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TarefaResponseDTO>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(tarefaService.listarPorUsuario(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> editarTarefa(@PathVariable Long id,
                                                          @RequestBody TarefaRequestDTO tarefa) {
        return ResponseEntity.ok(tarefaService.atualizar(id, tarefa));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<TarefaResponseDTO> concluirTarefa(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.marcarComoConcluida(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarefa(@PathVariable Long id) {
        tarefaService.deletar(id);
        return ResponseEntity.status(204).build();
    }
}