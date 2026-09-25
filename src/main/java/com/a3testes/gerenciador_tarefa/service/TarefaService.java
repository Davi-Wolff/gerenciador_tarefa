package com.a3testes.gerenciador_tarefa.service;


import com.a3testes.gerenciador_tarefa.DTO.request.TarefaRequestDTO;
import com.a3testes.gerenciador_tarefa.DTO.response.TarefaResponseDTO;
import com.a3testes.gerenciador_tarefa.exception.TarefaNotFoundException;
import com.a3testes.gerenciador_tarefa.exception.UserNotFoundException;
import com.a3testes.gerenciador_tarefa.model.Tarefa;
import com.a3testes.gerenciador_tarefa.model.User;
import com.a3testes.gerenciador_tarefa.repository.TarefaRepository;
import com.a3testes.gerenciador_tarefa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UserRepository userRepository;

    public TarefaService(TarefaRepository tarefaRepository, UserRepository userRepository) {
        this.tarefaRepository = tarefaRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TarefaResponseDTO criar(TarefaRequestDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: id " + userId));

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());
        tarefa.setConcluido(false);
        tarefa.setDataCriacao(LocalDateTime.now());
        tarefa.setUser(user);

        Tarefa salva = tarefaRepository.save(tarefa);
        return toResponseDTO(salva);
    }

    @Transactional(readOnly = true)
    public List<TarefaResponseDTO> listarPorUsuario(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuário não encontrado: id " + userId);
        }

        return tarefaRepository.findByUserId(userId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TarefaResponseDTO buscarPorId(Long id) {
        Tarefa tarefa = buscarEntidadePorId(id);
        return toResponseDTO(tarefa);
    }

    @Transactional
    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO dto) {
        Tarefa tarefa = buscarEntidadePorId(id);
        tarefa.setTitulo(dto.titulo());
        tarefa.setDescricao(dto.descricao());

        Tarefa atualizada = tarefaRepository.save(tarefa);
        return toResponseDTO(atualizada);
    }

    @Transactional
    public TarefaResponseDTO marcarComoConcluida(Long id) {
        Tarefa tarefa = buscarEntidadePorId(id);
        tarefa.setConcluido(true);

        Tarefa atualizada = tarefaRepository.save(tarefa);
        return toResponseDTO(atualizada);
    }

    @Transactional
    public void deletar(Long id) {
        Tarefa tarefa = buscarEntidadePorId(id);
        tarefaRepository.delete(tarefa);
    }

    private Tarefa buscarEntidadePorId(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new TarefaNotFoundException("Tarefa não encontrada: id " + id));
    }

    private TarefaResponseDTO toResponseDTO(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.isConcluido(),
                tarefa.getDataCriacao(),
                tarefa.getUser().getId()
        );
    }
}