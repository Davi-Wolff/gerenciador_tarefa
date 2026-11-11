package com.a3testes.gerenciador_tarefa.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GerenciadorTarefaE2ETest {

    private static final Path PASTA_EVIDENCIAS = Path.of("target", "evidencias-selenium");
    private static final String SENHA_TESTE = "Qa#Tarefas2026";

    @Value("${local.server.port}")
    private int porta;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.getBoolean("headless")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1280,900");
        options.addArguments("--disable-features=PasswordLeakDetection,PasswordCheck");
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false
        ));

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:" + porta);
        aguardarTelaPronta();
    }

    @AfterEach
    void fecharNavegador(TestInfo testInfo) throws IOException {
        if (driver == null) {
            return;
        }
        try {
            Files.createDirectories(PASTA_EVIDENCIAS);
            File print = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String nome = testInfo.getTestMethod().map(m -> m.getName()).orElse("teste");
            Files.copy(print.toPath(), PASTA_EVIDENCIAS.resolve(nome + ".png"), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            driver.quit();
        }
    }

    private void aguardarTelaPronta() {
        wait.until(ExpectedConditions.attributeToBe(By.tagName("body"), "data-ocupado", "false"));
    }

    private By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    private WebElement elemento(String id) {
        return driver.findElement(testId(id));
    }

    private String emailUnico() {
        return "teste" + System.nanoTime() + "@email.com";
    }

    private By tarefa(String titulo) {
        return By.xpath("//li[starts-with(@data-testid,'tarefa-')]"
                + "[.//span[@data-testid='tarefa-titulo' and normalize-space()='" + titulo + "']]");
    }

    private void preencherFormUsuario(String nome, String email, String senha) {
        elemento("btn-novo-usuario").click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(testId("form-usuario")));
        elemento("input-nome").sendKeys(nome);
        elemento("input-email").sendKeys(email);
        elemento("input-senha").sendKeys(senha);
        elemento("btn-cadastrar-usuario").click();
        aguardarTelaPronta();
    }

    private void cadastrarUsuario(String nome, String email) {
        preencherFormUsuario(nome, email, SENHA_TESTE);
        wait.until(ExpectedConditions.textToBe(testId("nome-usuario"), nome));
    }

    private void adicionarTarefa(String titulo, String descricao) {
        elemento("input-titulo").sendKeys(titulo);
        elemento("input-descricao").sendKeys(descricao);
        elemento("btn-adicionar-tarefa").click();
        aguardarTelaPronta();
        wait.until(ExpectedConditions.visibilityOfElementLocated(tarefa(titulo)));
    }

    private void concluir(String titulo) {
        driver.findElement(tarefa(titulo)).findElement(testId("btn-concluir")).click();
        aguardarTelaPronta();
        wait.until(ExpectedConditions.attributeContains(tarefa(titulo), "class", "concluida"));
    }

    private List<String> titulosVisiveis() {
        return driver.findElements(testId("tarefa-titulo"))
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    @Test
    @DisplayName("CT-01: página inicial abre com o título do sistema")
    void deveAbrirAPaginaInicial() {
        assertThat(driver.getTitle()).isEqualTo("Gerenciador de Tarefas");
        assertThat(driver.findElement(By.tagName("h1")).getText()).isEqualTo("Gerenciador de Tarefas");
    }

    @Test
    @DisplayName("CT-02: cadastrar usuário com dados válidos")
    void deveCadastrarUsuario() {
        String email = emailUnico();

        cadastrarUsuario("Maria Souza", email);

        assertThat(elemento("email-usuario").getText()).isEqualTo(email);
        assertThat(elemento("lista-usuarios").getText()).contains("Maria Souza");
        wait.until(ExpectedConditions.textToBe(testId("aviso"), "Usuário cadastrado."));
    }

    @Test
    @DisplayName("CT-03: recusar cadastro com e-mail já existente")
    void naoDeveCadastrarEmailDuplicado() {
        String email = emailUnico();
        cadastrarUsuario("Joao Silva", email);

        preencherFormUsuario("Outro Joao", email, SENHA_TESTE);

        wait.until(ExpectedConditions.visibilityOfElementLocated(testId("erro-email")));
        assertThat(elemento("erro-email").getText()).isEqualTo("Este e-mail já está cadastrado. Use outro e-mail.");
        assertThat(elemento("nome-usuario").getText()).isEqualTo("Joao Silva");
        assertThat(elemento("lista-usuarios").getText()).doesNotContain("Outro Joao");
    }

    @Test
    @DisplayName("CT-04: recusar cadastro com e-mail inválido e senha curta")
    void naoDeveCadastrarUsuarioComDadosInvalidos() {
        preencherFormUsuario("Usuario Invalido", "email-sem-arroba", "123");

        wait.until(ExpectedConditions.visibilityOfElementLocated(testId("erro-email")));
        assertThat(elemento("erro-email").getText()).isEqualTo("Informe um e-mail válido, como nome@email.com.");
        assertThat(elemento("erro-senha").getText()).isEqualTo("A senha precisa ter pelo menos 6 caracteres.");
        assertThat(elemento("form-usuario").isDisplayed()).isTrue();
        assertThat(elemento("lista-usuarios").getText()).doesNotContain("Usuario Invalido");
    }

    @Test
    @DisplayName("CT-05: adicionar tarefa para o usuário selecionado")
    void deveAdicionarTarefa() {
        cadastrarUsuario("Ana Lima", emailUnico());

        adicionarTarefa("Estudar Selenium", "Ler a documentação do WebDriver");

        WebElement item = driver.findElement(tarefa("Estudar Selenium"));
        assertThat(item.getText()).contains("Ler a documentação do WebDriver");
        assertThat(item.findElement(testId("tarefa-status")).getText()).isEqualTo("Pendente");
        assertThat(elemento("qtd-pendentes").getText()).isEqualTo("1");
    }

    @Test
    @DisplayName("CT-06: não adicionar tarefa sem título")
    void naoDeveAdicionarTarefaSemTitulo() {
        cadastrarUsuario("Rafael Costa", emailUnico());

        elemento("input-descricao").sendKeys("Só a descrição");
        elemento("btn-adicionar-tarefa").click();
        aguardarTelaPronta();

        wait.until(ExpectedConditions.visibilityOfElementLocated(testId("erro-titulo")));
        assertThat(elemento("erro-titulo").getText()).isEqualTo("Informe um título para a tarefa.");
        assertThat(elemento("qtd-todas").getText()).isEqualTo("0");
        assertThat(elemento("sem-tarefas").isDisplayed()).isTrue();
    }

    @Test
    @DisplayName("CT-07: concluir tarefa altera status e contadores")
    void deveConcluirTarefa() {
        cadastrarUsuario("Carlos Dias", emailUnico());
        adicionarTarefa("Entregar relatório", "A3 de testes");

        concluir("Entregar relatório");

        wait.until(ExpectedConditions.textToBe(testId("qtd-concluidas"), "1"));
        WebElement item = driver.findElement(tarefa("Entregar relatório"));
        assertThat(item.findElement(testId("tarefa-status")).getText()).isEqualTo("Concluída");
        assertThat(elemento("qtd-pendentes").getText()).isEqualTo("0");
    }

    @Test
    @DisplayName("CT-08: filtros separam tarefas pendentes e concluídas")
    void filtrosDevemSepararPendentesEConcluidas() {
        cadastrarUsuario("Beatriz Melo", emailUnico());
        adicionarTarefa("Tarefa pendente", "");
        adicionarTarefa("Tarefa feita", "");
        concluir("Tarefa feita");
        wait.until(ExpectedConditions.textToBe(testId("qtd-concluidas"), "1"));

        elemento("filtro-pendentes").click();
        assertThat(titulosVisiveis()).containsExactly("Tarefa pendente");

        elemento("filtro-concluidas").click();
        assertThat(titulosVisiveis()).containsExactly("Tarefa feita");

        elemento("filtro-todas").click();
        assertThat(titulosVisiveis()).containsExactlyInAnyOrder("Tarefa pendente", "Tarefa feita");
    }

    @Test
    @DisplayName("CT-09: editar título de uma tarefa")
    void deveEditarTarefa() {
        cadastrarUsuario("Pedro Alves", emailUnico());
        adicionarTarefa("Titulo antigo", "Descricao antiga");

        driver.findElement(tarefa("Titulo antigo")).findElement(testId("btn-editar")).click();

        WebElement campo = wait.until(ExpectedConditions.visibilityOfElementLocated(testId("input-editar-titulo")));
        campo.clear();
        campo.sendKeys("Titulo novo");
        elemento("btn-salvar-edicao").click();
        aguardarTelaPronta();

        wait.until(ExpectedConditions.visibilityOfElementLocated(tarefa("Titulo novo")));
        assertThat(driver.findElements(tarefa("Titulo antigo"))).isEmpty();
        assertThat(driver.findElement(tarefa("Titulo novo")).getText()).contains("Descricao antiga");
    }

    @Test
    @DisplayName("CT-10: excluir tarefa somente após confirmação")
    void deveExcluirTarefaAposConfirmar() {
        cadastrarUsuario("Lucas Rocha", emailUnico());
        adicionarTarefa("Tarefa para excluir", "");

        WebElement excluir = driver.findElement(tarefa("Tarefa para excluir")).findElement(testId("btn-excluir"));
        excluir.click();

        assertThat(excluir.getText()).isEqualTo("Confirmar exclusão");
        assertThat(driver.findElements(tarefa("Tarefa para excluir"))).hasSize(1);

        excluir.click();
        aguardarTelaPronta();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(tarefa("Tarefa para excluir")));
        assertThat(elemento("sem-tarefas").getText())
                .isEqualTo("Nenhuma tarefa ainda. Preencha o título acima para adicionar a primeira.");
    }
}
