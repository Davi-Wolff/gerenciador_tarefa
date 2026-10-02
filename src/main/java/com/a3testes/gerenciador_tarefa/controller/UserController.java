package com.a3testes.gerenciador_tarefa.controller;

import com.a3testes.gerenciador_tarefa.DTO.request.UserRequestDTO;
import com.a3testes.gerenciador_tarefa.DTO.response.UserResponseDTO;
import com.a3testes.gerenciador_tarefa.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> cadastrar(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO criado = userService.cadastrar(dto);
        return ResponseEntity.status(201).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(userService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listarTodos() {
        return ResponseEntity.ok(userService.listarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        userService.deletar(id);
        return ResponseEntity.status(204).build();
    }
}