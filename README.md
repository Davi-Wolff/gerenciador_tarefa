# Gerenciador de Tarefas

Aplicação web em que cada usuário cadastrado cria, edita, conclui, filtra e exclui suas próprias tarefas.
Projeto da A3 de Garantia da Qualidade de Software (Unisul, 2026/2): além do sistema, o repositório reúne
os testes unitários (JUnit 5 + Mockito) e os testes de interface (Selenium WebDriver).

Equipe: Yuri Trés, Davi Wolff, Beatriz Teixeira.

## Funcionalidades

- Cadastro, listagem e exclusão de usuários (senha armazenada com BCrypt)
- Cadastro, edição, conclusão e exclusão de tarefas por usuário
- Filtro de tarefas: todas, pendentes e concluídas
- Validação de dados no frontend e na API (nome, e-mail, senha com no mínimo 6 caracteres, título)

## Requisitos de ambiente

| Ferramenta | Versão usada |
| --- | --- |
| Java (JDK) | 21 |
| Maven | 3.9 (ou o `./mvnw` do projeto) |
| PostgreSQL | versão recente (só para rodar o sistema; os testes usam H2) |
| Google Chrome | 151 (para os testes Selenium) |
| Sistema operacional | Linux (Ubuntu); também funciona em Windows e macOS |

O chromedriver não precisa ser instalado: o Selenium Manager baixa a versão compatível com o Chrome automaticamente.

## Como rodar o sistema

1. Crie o banco no PostgreSQL:

   ```bash
   sudo -u postgres psql -c "ALTER USER postgres PASSWORD '123qwe';"
   sudo -u postgres psql -c "CREATE DATABASE a3qa;"
   ```

   Usuário, senha e nome do banco ficam em `src/main/resources/application.properties`.

2. Suba a aplicação:

   ```bash
   mvn spring-boot:run
   ```

3. Abra `http://localhost:8080`.

### API

| Método | Rota | Descrição |
| --- | --- | --- |
| POST | `/user` | Cadastra usuário (`username`, `email`, `senha`) |
| GET | `/user` | Lista usuários |
| GET | `/user/{id}` | Busca usuário |
| PUT | `/user/{id}` | Atualiza usuário |
| DELETE | `/user/{id}` | Exclui usuário e suas tarefas |
| POST | `/tarefa?userId={id}` | Cria tarefa (`titulo`, `descricao`) |
| GET | `/tarefa/user/{userId}` | Lista tarefas do usuário |
| GET | `/tarefa/{id}` | Busca tarefa |
| PUT | `/tarefa/{id}` | Edita título e descrição |
| PATCH | `/tarefa/{id}/concluir` | Marca como concluída |
| DELETE | `/tarefa/{id}` | Exclui tarefa |

Erros retornam JSON com `status`, `erro` e `mensagem`: 400 para dados inválidos, 404 para registro inexistente e 409 para e-mail já cadastrado.

## Testes

Os testes não usam o PostgreSQL nem dados reais: rodam com banco H2 em memória (`src/test/resources/application.properties`).
Nenhuma credencial pessoal é necessária.

| Comando | O que roda |
| --- | --- |
| `mvn clean test` | Todos os testes (unitários + Selenium) |
| `mvn test -Dtest='*ServiceTest,GlobalExceptionHandlerTest'` | Só os testes unitários |
| `mvn test -Dtest=GerenciadorTarefaE2ETest` | Só o Selenium, com o Chrome visível |
| `mvn test -Dtest=GerenciadorTarefaE2ETest -Dheadless=true` | Só o Selenium, sem abrir janela |
| `mvn surefire-report:report` | Gera relatório HTML em `target/reports/surefire.html` |

### Testes unitários (`src/test/java/.../service` e `.../exception`)

- `UserServiceTest`: cadastro, e-mail duplicado, criptografia da senha, validações de nome, e-mail e senha, atualização e exclusão.
- `TarefaServiceTest`: criação, título obrigatório, listagem, conclusão, edição sem alterar status e data, exclusão.
- `GlobalExceptionHandlerTest`: códigos HTTP 400, 404 e 409 devolvidos pela API.

Usam Mockito para isolar o banco de dados, então não dependem de serviços externos.

### Testes Selenium (`src/test/java/.../e2e/GerenciadorTarefaE2ETest.java`)

O teste sobe a aplicação numa porta livre (`@SpringBootTest` com `RANDOM_PORT`), abre o Chrome e usa a tela como um usuário:
preenche campos, clica e confere mensagens e estados visíveis. Cada cenário cadastra seu próprio usuário com e-mail único,
então a ordem de execução não importa.

| ID | Cenário |
| --- | --- |
| CT-01 | Página inicial abre com o título do sistema |
| CT-02 | Cadastrar usuário com dados válidos |
| CT-03 | Recusar cadastro com e-mail já existente |
| CT-04 | Recusar cadastro com e-mail inválido e senha curta |
| CT-05 | Adicionar tarefa |
| CT-06 | Não adicionar tarefa sem título |
| CT-07 | Concluir tarefa altera status e contadores |
| CT-08 | Filtros separam pendentes e concluídas |
| CT-09 | Editar título de uma tarefa |
| CT-10 | Excluir tarefa somente após confirmação |

Boas práticas aplicadas:

- Localizadores estáveis por atributo `data-testid`.
- Esperas explícitas com `WebDriverWait`, sem `Thread.sleep`. Após cada ação, o teste espera o atributo
  `data-ocupado="false"` no `<body>`, que a página só marca quando todas as requisições terminaram.
- Gerenciador de senhas do Chrome desativado no navegador de teste, para nenhum pop-up interromper a automação.

### Evidências

- Um print por cenário Selenium em `target/evidencias-selenium/<nome-do-teste>.png`, gerado a cada execução.
- Resultados por classe em `target/surefire-reports/` (texto e XML) e, após `mvn surefire-report:report`, em HTML.

## Estrutura

```
src/main/java/.../controller   Endpoints REST
src/main/java/.../service      Regras de negócio e validações
src/main/java/.../exception    Exceções e tratamento global de erros
src/main/resources/static      Frontend (HTML, CSS, JS)
src/test/java/.../service      Testes unitários
src/test/java/.../exception    Testes do tratamento de erros
src/test/java/.../e2e          Testes Selenium
```

## Limitações

- Não há login: as rotas da API são públicas e qualquer usuário pode ver as tarefas de outro.
- Uma tarefa concluída não pode voltar a pendente, pois não existe rota para isso.
- Os testes Selenium foram executados apenas no Google Chrome, em Linux.
- O frontend depende da fonte Instrument Sans (Google Fonts); sem internet, usa a fonte padrão do sistema.
