package com.a3testes.gerenciador_tarefa.service;

import com.a3testes.gerenciador_tarefa.DTO.request.UserRequestDTO;
import com.a3testes.gerenciador_tarefa.DTO.response.UserResponseDTO;
import com.a3testes.gerenciador_tarefa.exception.EmailJaCadastradoException;
import com.a3testes.gerenciador_tarefa.exception.UserNotFoundException;
import com.a3testes.gerenciador_tarefa.model.User;
import com.a3testes.gerenciador_tarefa.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO cadastrar(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailJaCadastradoException("Email já cadastrado: " + dto.email());
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setSenha(passwordEncoder.encode(dto.senha()));

        User salvo = userRepository.save(user);
        return toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO buscarPorId(Long id) {
        User user = buscarEntidadePorId(id);
        return toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listarTodos() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public UserResponseDTO atualizar(Long id, UserRequestDTO dto) {
        User user = buscarEntidadePorId(id);

        boolean emailMudou = !user.getEmail().equals(dto.email());
        if (emailMudou && userRepository.existsByEmail(dto.email())) {
            throw new EmailJaCadastradoException("Email já cadastrado: " + dto.email());
        }

        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setSenha(passwordEncoder.encode(dto.senha()));

        User atualizado = userRepository.save(user);
        return toResponseDTO(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        User user = buscarEntidadePorId(id);
        userRepository.delete(user);
    }

    private User buscarEntidadePorId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: id " + id));
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail());
    }
}