package com.a3testes.gerenciador_tarefa.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table
@Entity
@TableGenerator(name = "Tarefa")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String titulo;

    @Column
    private String descricao;

    @Column
    private boolean concluido;

    @Column
    private LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
