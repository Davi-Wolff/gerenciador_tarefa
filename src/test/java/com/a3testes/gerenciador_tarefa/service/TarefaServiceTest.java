package com.a3testes.gerenciador_tarefa.service;

import com.a3testes.gerenciador_tarefa.DTO.request.TarefaRequestDTO;
import com.a3testes.gerenciador_tarefa.DTO.response.TarefaResponseDTO;
import com.a3testes.gerenciador_tarefa.exception.DadosInvalidosException;
import com.a3testes.gerenciador_tarefa.exception.TarefaNotFoundException;
import com.a3testes.gerenciador_tarefa.exception.UserNotFoundException;
import com.a3testes.gerenciador_tarefa.model.Tarefa;
import com.a3testes.gerenciador_tarefa.model.User;
import com.a3testes.gerenciador_tarefa.repository.TarefaRepository;
import com.a3testes.gerenciador_tarefa.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TarefaService tarefaService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("yuri");
        user.setEmail("yuri@email.com");
    }

    private Tarefa novaTarefa(Long id) {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(id);
        tarefa.setTitulo("Estudar");
        tarefa.setDescricao("Prova de QA");
        tarefa.setConcluido(false);
        tarefa.setDataCriacao(LocalDateTime.of(2026, 11, 1, 10, 0));
        tarefa.setUser(user);
        return tarefa;
    }

    @Test
    void deveCriarTarefaNaoConcluida() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponseDTO resposta = tarefaService.criar(new TarefaRequestDTO("Estudar", "Prova de QA"), 1L);

        assertThat(resposta.titulo()).isEqualTo("Estudar");
        assertThat(resposta.descricao()).isEqualTo("Prova de QA");
        assertThat(resposta.concluida()).isFalse();
        assertThat(resposta.dataCriacao()).isNotNull();
        assertThat(resposta.userId()).isEqualTo(1L);
    }

    @Test
    void naoDeveCriarTarefaParaUsuarioInexistente() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.criar(new TarefaRequestDTO("X", "Y"), 99L))
                .isInstanceOf(UserNotFoundException.class);

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void deveListarTarefasDoUsuario() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(tarefaRepository.findByUserId(1L)).thenReturn(List.of(novaTarefa(1L), novaTarefa(2L)));

        List<TarefaResponseDTO> tarefas = tarefaService.listarPorUsuario(1L);

        assertThat(tarefas).hasSize(2);
    }

    @Test
    void deveRetornarListaVaziaQuandoUsuarioNaoTemTarefas() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(tarefaRepository.findByUserId(1L)).thenReturn(List.of());

        assertThat(tarefaService.listarPorUsuario(1L)).isEmpty();
    }

    @Test
    void naoDeveListarTarefasDeUsuarioInexistente() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> tarefaService.listarPorUsuario(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoBuscarTarefaInexistente() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.buscarPorId(99L))
                .isInstanceOf(TarefaNotFoundException.class);
    }

    @Test
    void deveMarcarTarefaComoConcluida() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(novaTarefa(1L)));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponseDTO resposta = tarefaService.marcarComoConcluida(1L);

        assertThat(resposta.concluida()).isTrue();
    }

    @Test
    void atualizarNaoDeveAlterarStatusNemDataDeCriacao() {
        Tarefa tarefa = novaTarefa(1L);
        tarefa.setConcluido(true);
        LocalDateTime dataOriginal = tarefa.getDataCriacao();

        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> inv.getArgument(0));

        TarefaResponseDTO resposta = tarefaService.atualizar(1L, new TarefaRequestDTO("Novo titulo", "Nova descricao"));

        assertThat(resposta.titulo()).isEqualTo("Novo titulo");
        assertThat(resposta.descricao()).isEqualTo("Nova descricao");
        assertThat(resposta.concluida()).isTrue();
        assertThat(resposta.dataCriacao()).isEqualTo(dataOriginal);
    }

    @Test
    void naoDeveDeletarTarefaInexistente() {
        when(tarefaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarefaService.deletar(99L))
                .isInstanceOf(TarefaNotFoundException.class);

        verify(tarefaRepository, never()).delete(any());
    }

    @Test
    void naoDeveCriarTarefaComTituloEmBranco() {
        assertThatThrownBy(() -> tarefaService.criar(new TarefaRequestDTO("   ", "Descricao"), 1L))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("Informe um título para a tarefa.");

        verify(userRepository, never()).findById(any());
        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarTarefaSemTitulo() {
        assertThatThrownBy(() -> tarefaService.criar(new TarefaRequestDTO(null, "Descricao"), 1L))
                .isInstanceOf(DadosInvalidosException.class);

        verify(tarefaRepository, never()).save(any());
    }

    @Test
    void naoDeveAtualizarTarefaParaTituloVazio() {
        assertThatThrownBy(() -> tarefaService.atualizar(1L, new TarefaRequestDTO("", "Nova descricao")))
                .isInstanceOf(DadosInvalidosException.class);

        verify(tarefaRepository, never()).findById(any());
        verify(tarefaRepository, never()).save(any());
    }
}
