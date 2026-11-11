package com.a3testes.gerenciador_tarefa.service;

import com.a3testes.gerenciador_tarefa.DTO.request.UserRequestDTO;
import com.a3testes.gerenciador_tarefa.DTO.response.UserResponseDTO;
import com.a3testes.gerenciador_tarefa.exception.DadosInvalidosException;
import com.a3testes.gerenciador_tarefa.exception.EmailJaCadastradoException;
import com.a3testes.gerenciador_tarefa.exception.UserNotFoundException;
import com.a3testes.gerenciador_tarefa.model.User;
import com.a3testes.gerenciador_tarefa.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User novoUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername("yuri");
        user.setEmail(email);
        user.setSenha("hash-antigo");
        return user;
    }

    @Test
    void deveCadastrarUsuarioComSenhaCriptografada() {
        UserRequestDTO dto = new UserRequestDTO("yuri", "yuri@email.com", "123456");

        when(userRepository.existsByEmail("yuri@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash-123456");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User salvo = inv.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });

        UserResponseDTO resposta = userService.cadastrar(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getSenha()).isEqualTo("hash-123456");
        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.username()).isEqualTo("yuri");
        assertThat(resposta.email()).isEqualTo("yuri@email.com");
    }

    @Test
    void naoDeveCadastrarEmailDuplicado() {
        UserRequestDTO dto = new UserRequestDTO("yuri", "yuri@email.com", "123456");

        when(userRepository.existsByEmail("yuri@email.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.cadastrar(dto))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveBuscarUsuarioPorId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(novoUser(1L, "yuri@email.com")));

        UserResponseDTO resposta = userService.buscarPorId(1L);

        assertThat(resposta.email()).isEqualTo("yuri@email.com");
    }

    @Test
    void deveLancarExcecaoAoBuscarUsuarioInexistente() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.buscarPorId(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deveListarTodosOsUsuarios() {
        when(userRepository.findAll()).thenReturn(List.of(
                novoUser(1L, "a@email.com"),
                novoUser(2L, "b@email.com")));

        assertThat(userService.listarTodos()).hasSize(2);
    }

    @Test
    void naoDeveAtualizarParaEmailDeOutroUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(novoUser(1L, "yuri@email.com")));
        when(userRepository.existsByEmail("outro@email.com")).thenReturn(true);

        UserRequestDTO dto = new UserRequestDTO("yuri", "outro@email.com", "123456");

        assertThatThrownBy(() -> userService.atualizar(1L, dto))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveAtualizarMantendoOMesmoEmail() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(novoUser(1L, "yuri@email.com")));
        when(passwordEncoder.encode("nova-senha")).thenReturn("hash-nova-senha");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequestDTO dto = new UserRequestDTO("yuri trés", "yuri@email.com", "nova-senha");
        UserResponseDTO resposta = userService.atualizar(1L, dto);

        assertThat(resposta.username()).isEqualTo("yuri trés");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void naoDeveDeletarUsuarioInexistente() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deletar(99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deveDeletarUsuarioExistente() {
        User user = novoUser(1L, "yuri@email.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deletar(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void naoDeveCadastrarUsuarioSemNome() {
        UserRequestDTO dto = new UserRequestDTO("   ", "yuri@email.com", "123456");

        assertThatThrownBy(() -> userService.cadastrar(dto))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("Informe o nome do usuário.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarUsuarioComEmailInvalido() {
        UserRequestDTO dto = new UserRequestDTO("yuri", "email-sem-arroba", "123456");

        assertThatThrownBy(() -> userService.cadastrar(dto))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("Informe um e-mail válido, como nome@email.com.");

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void naoDeveCadastrarUsuarioComSenhaMenorQueSeisCaracteres() {
        UserRequestDTO dto = new UserRequestDTO("yuri", "yuri@email.com", "12345");

        assertThatThrownBy(() -> userService.cadastrar(dto))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("A senha precisa ter pelo menos 6 caracteres.");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveAceitarSenhaComExatamenteSeisCaracteres() {
        UserRequestDTO dto = new UserRequestDTO("yuri", "yuri@email.com", "123456");

        when(userRepository.existsByEmail("yuri@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(userService.cadastrar(dto).email()).isEqualTo("yuri@email.com");
    }

    @Test
    void naoDeveAtualizarUsuarioComEmailInvalido() {
        UserRequestDTO dto = new UserRequestDTO("yuri", "yuri@", "123456");

        assertThatThrownBy(() -> userService.atualizar(1L, dto))
                .isInstanceOf(DadosInvalidosException.class);

        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }
}
