package com.a3testes.gerenciador_tarefa.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Table
@Entity
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
    private String username;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 100)
    private String senha;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Tarefa> tarefas;
}
