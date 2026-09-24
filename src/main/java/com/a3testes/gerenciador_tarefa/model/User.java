package com.a3testes.gerenciador_tarefa.model;

import jakarta.persistence.*;
import lombok.*;

@Table
@Entity
@TableGenerator(name = "User")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String nome;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String senha;
}
