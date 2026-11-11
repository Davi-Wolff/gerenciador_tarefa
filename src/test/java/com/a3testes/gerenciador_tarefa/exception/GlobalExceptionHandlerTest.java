package com.a3testes.gerenciador_tarefa.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void usuarioInexistenteDeveRetornar404() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.naoEncontrado(new UserNotFoundException("Usuário não encontrado: id 99"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).containsEntry("mensagem", "Usuário não encontrado: id 99");
    }

    @Test
    void tarefaInexistenteDeveRetornar404() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.naoEncontrado(new TarefaNotFoundException("Tarefa não encontrada: id 7"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resposta.getBody()).containsEntry("status", 404);
    }

    @Test
    void emailDuplicadoDeveRetornar409() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.emailJaCadastrado(new EmailJaCadastradoException("Email já cadastrado: yuri@email.com"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).containsEntry("mensagem", "Email já cadastrado: yuri@email.com");
    }

    @Test
    void dadosInvalidosDevemRetornar400() {
        ResponseEntity<Map<String, Object>> resposta =
                handler.dadosInvalidos(new DadosInvalidosException("Informe um título para a tarefa."));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).containsEntry("erro", "Bad Request");
    }
}
