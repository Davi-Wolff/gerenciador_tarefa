package com.a3testes.gerenciador_tarefa.model;

import jakarta.persistence.*;
import lombok.*;

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

    private String descricao;
}
