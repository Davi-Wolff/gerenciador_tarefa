const API = window.location.protocol.startsWith('http') ? '' : 'http://localhost:8080';

const ICONES = {
    check: '<svg class="icone" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 6 9 17l-5-5"/></svg>',
    editar: '<svg class="icone" viewBox="0 0 24 24" aria-hidden="true"><path d="M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z"/></svg>',
    excluir: '<svg class="icone" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/></svg>'
};

const MENSAGENS = {
    nomeObrigatorio: 'Informe o nome do usuário.',
    emailInvalido: 'Informe um e-mail válido, como nome@email.com.',
    emailDuplicado: 'Este e-mail já está cadastrado. Use outro e-mail.',
    senhaCurta: 'A senha precisa ter pelo menos 6 caracteres.',
    tituloObrigatorio: 'Informe um título para a tarefa.'
};

const estado = {
    usuarios: [],
    usuarioAtual: null,
    tarefas: [],
    filtro: 'todas',
    editando: null
};

const el = (id) => document.getElementById(id);

let operacoesPendentes = 0;
let versaoUsuarios = 0;
let versaoTarefas = 0;

function marcarOcupado(delta) {
    operacoesPendentes += delta;
    document.body.dataset.ocupado = String(operacoesPendentes > 0);
}

async function emOperacao(acao) {
    marcarOcupado(1);
    try {
        return await acao();
    } finally {
        marcarOcupado(-1);
    }
}

function requisicao(caminho, opcoes = {}) {
    return emOperacao(() => executarRequisicao(caminho, opcoes));
}

async function executarRequisicao(caminho, opcoes) {
    let resposta;
    try {
        resposta = await fetch(API + caminho, {
            headers: { 'Content-Type': 'application/json' },
            ...opcoes
        });
    } catch (e) {
        const erro = new Error('A API não respondeu. Verifique se o projeto está rodando com mvn spring-boot:run.');
        erro.conexao = true;
        throw erro;
    }

    if (!resposta.ok) {
        let mensagem = '';
        try {
            mensagem = (await resposta.json()).mensagem || '';
        } catch (e) { }
        const erro = new Error(mensagem || `A API recusou a operação (erro ${resposta.status}). Confira os dados e tente de novo.`);
        erro.status = resposta.status;
        throw erro;
    }

    if (resposta.status === 204) return null;
    return resposta.json();
}

function mostrarAviso(texto, tipo = 'erro') {
    const aviso = el('aviso');
    aviso.textContent = texto;
    aviso.className = tipo === 'sucesso' ? 'aviso sucesso' : 'aviso';
    aviso.hidden = false;
    clearTimeout(mostrarAviso.timer);
    mostrarAviso.timer = setTimeout(() => (aviso.hidden = true), 5000);
}

function mostrarEstado(nome) {
    el('estado-carregando').hidden = nome !== 'carregando';
    el('estado-erro').hidden = nome !== 'erro';
    el('sem-usuario').hidden = nome !== 'vazio';
    el('conteudo-tarefas').hidden = nome !== 'conteudo';
}

function definirErro(input, mensagem) {
    const erro = el('erro-' + input.name);
    if (mensagem) {
        input.setAttribute('aria-invalid', 'true');
        erro.textContent = mensagem;
        erro.hidden = false;
    } else {
        input.removeAttribute('aria-invalid');
        erro.textContent = '';
        erro.hidden = true;
    }
    return !mensagem;
}

function validarCampoUsuario(input) {
    const valor = input.value.trim();

    if (input.name === 'username') {
        return definirErro(input, valor ? '' : MENSAGENS.nomeObrigatorio);
    }
    if (input.name === 'email') {
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor)) {
            return definirErro(input, MENSAGENS.emailInvalido);
        }
        const duplicado = estado.usuarios.some(u => u.email.toLowerCase() === valor.toLowerCase());
        return definirErro(input, duplicado ? MENSAGENS.emailDuplicado : '');
    }
    if (input.name === 'senha') {
        return definirErro(input, input.value.length >= 6 ? '' : MENSAGENS.senhaCurta);
    }
    return true;
}

function validarTitulo(input) {
    return definirErro(input, input.value.trim() ? '' : MENSAGENS.tituloObrigatorio);
}

function ligarValidacao(input, validar) {
    input.addEventListener('blur', (e) => {
        if (e.relatedTarget && e.relatedTarget.tagName === 'BUTTON') return;
        if (input.value !== '' || input.hasAttribute('aria-invalid')) validar(input);
    });
    input.addEventListener('input', () => {
        if (input.hasAttribute('aria-invalid')) validar(input);
    });
}

async function comBotaoOcupado(botao, textoOcupado, acao) {
    const original = botao.innerHTML;
    botao.disabled = true;
    botao.textContent = textoOcupado;
    marcarOcupado(1);
    try {
        await acao();
    } finally {
        botao.disabled = false;
        botao.innerHTML = original;
        marcarOcupado(-1);
    }
}

function formatarData(iso) {
    if (!iso) return '';
    const data = new Date(iso);
    return 'Criada em ' + data.toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })
        + ' às ' + data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function confirmarClique(botao, textoConfirmacao, acao) {
    if (botao.classList.contains('confirmando')) {
        acao();
        return;
    }
    const original = botao.innerHTML;
    botao.classList.add('confirmando');
    botao.textContent = textoConfirmacao;
    setTimeout(() => {
        if (!botao.isConnected) return;
        botao.classList.remove('confirmando');
        botao.innerHTML = original;
    }, 3000);
}

function renderizarSkeletonUsuarios() {
    const lista = el('lista-usuarios');
    lista.innerHTML = '';
    for (let i = 0; i < 3; i++) {
        const item = document.createElement('li');
        item.className = 'skeleton skeleton-usuario';
        lista.appendChild(item);
    }
}

function carregarUsuarios() {
    return emOperacao(carregarUsuariosAgora);
}

async function carregarUsuariosAgora() {
    const versao = ++versaoUsuarios;
    versaoTarefas++;
    mostrarEstado('carregando');
    renderizarSkeletonUsuarios();
    el('lista-usuarios').setAttribute('aria-busy', 'true');

    let usuarios;
    try {
        usuarios = await requisicao('/user');
    } catch (erro) {
        if (versao !== versaoUsuarios) return;
        el('lista-usuarios').innerHTML = '';
        el('lista-usuarios').setAttribute('aria-busy', 'false');
        mostrarEstado('erro');
        return;
    }
    if (versao !== versaoUsuarios) return;
    estado.usuarios = usuarios;

    if (!estado.usuarios.some(u => u.id === estado.usuarioAtual?.id)) {
        estado.usuarioAtual = estado.usuarios[0] || null;
    }
    renderizarUsuarios();
    await carregarTarefas();
}

function renderizarUsuarios() {
    const lista = el('lista-usuarios');
    lista.innerHTML = '';
    lista.setAttribute('aria-busy', 'false');

    estado.usuarios.forEach(usuario => {
        const item = document.createElement('li');
        const botao = document.createElement('button');
        botao.type = 'button';
        botao.dataset.testid = 'usuario-' + usuario.id;
        botao.textContent = usuario.username;
        const email = document.createElement('small');
        email.textContent = usuario.email;
        botao.appendChild(email);
        botao.setAttribute('aria-current', String(usuario.id === estado.usuarioAtual?.id));
        botao.addEventListener('click', () => selecionarUsuario(usuario));
        item.appendChild(botao);
        lista.appendChild(item);
    });

    if (!estado.usuarioAtual) {
        mostrarEstado('vazio');
        return;
    }

    el('nome-usuario').textContent = estado.usuarioAtual.username;
    el('email-usuario').textContent = estado.usuarioAtual.email;
}

async function selecionarUsuario(usuario) {
    estado.usuarioAtual = usuario;
    estado.editando = null;
    estado.filtro = 'todas';
    estado.tarefas = [];
    renderizarUsuarios();
    mostrarEstado('carregando');
    await carregarTarefas();
}

async function carregarTarefas() {
    if (!estado.usuarioAtual) return;
    const versao = ++versaoTarefas;
    const usuarioId = estado.usuarioAtual.id;
    try {
        const tarefas = await requisicao(`/tarefa/user/${usuarioId}`);
        if (versao !== versaoTarefas || estado.usuarioAtual?.id !== usuarioId) return;
        estado.tarefas = tarefas;
        mostrarEstado('conteudo');
        renderizarTarefas();
    } catch (erro) {
        if (versao !== versaoTarefas) return;
        if (erro.conexao) {
            mostrarEstado('erro');
        } else {
            mostrarAviso('Não foi possível carregar as tarefas deste usuário.');
        }
    }
}

function renderizarTarefas() {
    const pendentes = estado.tarefas.filter(t => !t.concluida);
    const concluidas = estado.tarefas.filter(t => t.concluida);

    el('qtd-todas').textContent = estado.tarefas.length;
    el('qtd-pendentes').textContent = pendentes.length;
    el('qtd-concluidas').textContent = concluidas.length;

    document.querySelectorAll('.filtro').forEach(b => {
        const ativo = b.dataset.filtro === estado.filtro;
        b.classList.toggle('ativo', ativo);
        b.setAttribute('aria-pressed', String(ativo));
    });

    const visiveis = {
        todas: [...pendentes, ...concluidas],
        pendentes,
        concluidas
    }[estado.filtro];

    const lista = el('lista-tarefas');
    lista.innerHTML = '';
    visiveis.forEach(tarefa => lista.appendChild(itemTarefa(tarefa)));

    const semTarefas = el('sem-tarefas');
    semTarefas.hidden = visiveis.length > 0;
    semTarefas.textContent = {
        todas: 'Nenhuma tarefa ainda. Preencha o título acima para adicionar a primeira.',
        pendentes: 'Nenhuma tarefa pendente.',
        concluidas: 'Nenhuma tarefa concluída ainda.'
    }[estado.filtro];
}

function itemTarefa(tarefa) {
    const item = document.createElement('li');
    item.className = 'tarefa' + (tarefa.concluida ? ' concluida' : '');
    item.dataset.testid = 'tarefa-' + tarefa.id;

    const check = document.createElement('button');
    check.type = 'button';
    check.className = 'check';
    check.dataset.testid = 'btn-concluir';
    check.innerHTML = ICONES.check;
    if (tarefa.concluida) {
        check.disabled = true;
        check.setAttribute('aria-label', 'Tarefa concluída: ' + tarefa.titulo);
    } else {
        check.setAttribute('aria-label', 'Concluir tarefa: ' + tarefa.titulo);
        check.addEventListener('click', () => concluirTarefa(tarefa, item, check));
    }
    item.appendChild(check);

    const corpo = document.createElement('div');

    if (estado.editando === tarefa.id) {
        corpo.appendChild(formEdicao(tarefa));
        item.appendChild(corpo);
        return item;
    }

    const titulo = document.createElement('span');
    titulo.className = 'tarefa-titulo';
    titulo.dataset.testid = 'tarefa-titulo';
    titulo.textContent = tarefa.titulo;
    corpo.appendChild(titulo);

    if (tarefa.descricao) {
        const descricao = document.createElement('p');
        descricao.className = 'tarefa-descricao';
        descricao.textContent = tarefa.descricao;
        corpo.appendChild(descricao);
    }

    const meta = document.createElement('p');
    meta.className = 'tarefa-meta';
    const status = document.createElement('span');
    status.className = 'status ' + (tarefa.concluida ? 'status-concluida' : 'status-pendente');
    status.dataset.testid = 'tarefa-status';
    status.textContent = tarefa.concluida ? 'Concluída' : 'Pendente';
    const data = document.createElement('span');
    data.textContent = formatarData(tarefa.dataCriacao);
    meta.append(status, data);
    corpo.appendChild(meta);
    item.appendChild(corpo);

    const acoes = document.createElement('div');
    acoes.className = 'tarefa-acoes';

    const editar = document.createElement('button');
    editar.type = 'button';
    editar.className = 'acao';
    editar.dataset.testid = 'btn-editar';
    editar.innerHTML = ICONES.editar + 'Editar';
    editar.setAttribute('aria-label', 'Editar tarefa: ' + tarefa.titulo);
    editar.addEventListener('click', () => {
        estado.editando = tarefa.id;
        renderizarTarefas();
    });

    const excluir = document.createElement('button');
    excluir.type = 'button';
    excluir.className = 'acao excluir';
    excluir.dataset.testid = 'btn-excluir';
    excluir.innerHTML = ICONES.excluir + 'Excluir';
    excluir.setAttribute('aria-label', 'Excluir tarefa: ' + tarefa.titulo);
    excluir.addEventListener('click', () => confirmarClique(excluir, 'Confirmar exclusão', () => excluirTarefa(tarefa)));

    acoes.append(editar, excluir);
    item.appendChild(acoes);

    return item;
}

function formEdicao(tarefa) {
    const form = document.createElement('form');
    form.className = 'form-edicao';
    form.noValidate = true;
    form.dataset.testid = 'form-edicao';

    const campoTitulo = document.createElement('div');
    campoTitulo.className = 'campo';
    campoTitulo.innerHTML = `
        <label for="editar-titulo">Título</label>
        <input type="text" id="editar-titulo" name="titulo" maxlength="255" aria-describedby="erro-titulo-edicao" data-testid="input-editar-titulo">
        <p class="erro-campo" id="erro-titulo-edicao" data-testid="erro-titulo-edicao" hidden></p>`;

    const campoDescricao = document.createElement('div');
    campoDescricao.className = 'campo';
    campoDescricao.innerHTML = `
        <label for="editar-descricao">Descrição <span class="opcional">(opcional)</span></label>
        <input type="text" id="editar-descricao" name="descricao" maxlength="255" data-testid="input-editar-descricao">`;

    const titulo = campoTitulo.querySelector('input');
    const descricao = campoDescricao.querySelector('input');
    titulo.value = tarefa.titulo;
    descricao.value = tarefa.descricao || '';

    const erroTitulo = campoTitulo.querySelector('.erro-campo');
    const validar = () => {
        const ok = titulo.value.trim() !== '';
        if (ok) titulo.removeAttribute('aria-invalid'); else titulo.setAttribute('aria-invalid', 'true');
        erroTitulo.textContent = ok ? '' : MENSAGENS.tituloObrigatorio;
        erroTitulo.hidden = ok;
        return ok;
    };
    titulo.addEventListener('input', () => titulo.hasAttribute('aria-invalid') && validar());

    const acoes = document.createElement('div');
    acoes.className = 'form-acoes';
    acoes.innerHTML = '<button type="submit" class="btn btn-secundario" data-testid="btn-salvar-edicao">Salvar alterações</button><button type="button" class="btn-link">Cancelar</button>';
    acoes.querySelector('.btn-link').addEventListener('click', () => {
        estado.editando = null;
        renderizarTarefas();
    });

    form.append(campoTitulo, campoDescricao, acoes);
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validar()) {
            titulo.focus();
            return;
        }
        const botao = acoes.querySelector('button[type="submit"]');
        await comBotaoOcupado(botao, 'Salvando…', async () => {
            try {
                const atualizada = await requisicao(`/tarefa/${tarefa.id}`, {
                    method: 'PUT',
                    body: JSON.stringify({ titulo: titulo.value.trim(), descricao: descricao.value.trim() })
                });
                Object.assign(tarefa, atualizada);
                estado.editando = null;
                renderizarTarefas();
                mostrarAviso('Alterações salvas.', 'sucesso');
            } catch (erro) {
                mostrarAviso(erro.message);
            }
        });
    });

    setTimeout(() => titulo.focus(), 0);
    return form;
}

async function concluirTarefa(tarefa, item, check) {
    check.disabled = true;
    try {
        const atualizada = await requisicao(`/tarefa/${tarefa.id}/concluir`, { method: 'PATCH' });
        Object.assign(tarefa, atualizada);
        item.classList.add('concluida');
        marcarOcupado(1);
        setTimeout(() => {
            renderizarTarefas();
            marcarOcupado(-1);
        }, 600);
    } catch (erro) {
        check.disabled = false;
        mostrarAviso(erro.message);
    }
}

async function excluirTarefa(tarefa) {
    try {
        await requisicao(`/tarefa/${tarefa.id}`, { method: 'DELETE' });
        estado.tarefas = estado.tarefas.filter(t => t.id !== tarefa.id);
        renderizarTarefas();
        mostrarAviso('Tarefa excluída.', 'sucesso');
    } catch (erro) {
        mostrarAviso(erro.message);
    }
}

const formTarefa = el('form-tarefa');
ligarValidacao(formTarefa.titulo, validarTitulo);

formTarefa.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validarTitulo(formTarefa.titulo)) {
        formTarefa.titulo.focus();
        return;
    }

    const botao = formTarefa.querySelector('button[type="submit"]');
    await comBotaoOcupado(botao, 'Adicionando…', async () => {
        try {
            const nova = await requisicao(`/tarefa?userId=${estado.usuarioAtual.id}`, {
                method: 'POST',
                body: JSON.stringify({
                    titulo: formTarefa.titulo.value.trim(),
                    descricao: formTarefa.descricao.value.trim()
                })
            });
            estado.tarefas.push(nova);
            formTarefa.reset();
            if (estado.filtro === 'concluidas') estado.filtro = 'todas';
            renderizarTarefas();
            mostrarAviso('Tarefa adicionada.', 'sucesso');
        } catch (erro) {
            mostrarAviso(erro.message);
        }
    });
    formTarefa.titulo.focus();
});

const formUsuario = el('form-usuario');
['username', 'email', 'senha'].forEach(nome => ligarValidacao(formUsuario[nome], validarCampoUsuario));

function abrirFormUsuario() {
    formUsuario.hidden = false;
    formUsuario.username.focus();
}

function fecharFormUsuario() {
    formUsuario.reset();
    ['username', 'email', 'senha'].forEach(nome => definirErro(formUsuario[nome], ''));
    formUsuario.hidden = true;
}

el('btn-novo-usuario').addEventListener('click', abrirFormUsuario);
el('btn-primeiro-usuario').addEventListener('click', abrirFormUsuario);
el('btn-cancelar-usuario').addEventListener('click', fecharFormUsuario);
el('btn-tentar-novamente').addEventListener('click', carregarUsuarios);

formUsuario.addEventListener('submit', async (e) => {
    e.preventDefault();
    const campos = ['username', 'email', 'senha'].map(nome => formUsuario[nome]);
    const validos = campos.map(validarCampoUsuario);
    const primeiroInvalido = campos[validos.indexOf(false)];
    if (primeiroInvalido) {
        primeiroInvalido.focus();
        return;
    }

    const botao = formUsuario.querySelector('button[type="submit"]');
    await comBotaoOcupado(botao, 'Cadastrando…', async () => {
        try {
            const novo = await requisicao('/user', {
                method: 'POST',
                body: JSON.stringify({
                    username: formUsuario.username.value.trim(),
                    email: formUsuario.email.value.trim(),
                    senha: formUsuario.senha.value
                })
            });
            fecharFormUsuario();
            estado.usuarioAtual = novo;
            estado.filtro = 'todas';
            await carregarUsuarios();
            mostrarAviso('Usuário cadastrado.', 'sucesso');
        } catch (erro) {
            mostrarAviso(erro.message);
        }
    });
});

el('btn-excluir-usuario').addEventListener('click', (e) => {
    confirmarClique(e.currentTarget, 'Confirmar exclusão', async () => {
        try {
            await requisicao(`/user/${estado.usuarioAtual.id}`, { method: 'DELETE' });
            estado.usuarioAtual = null;
            await carregarUsuarios();
            mostrarAviso('Usuário excluído.', 'sucesso');
        } catch (erro) {
            mostrarAviso(erro.message);
        }
    });
});

document.querySelectorAll('.filtro').forEach(botao => {
    botao.addEventListener('click', () => {
        estado.filtro = botao.dataset.filtro;
        estado.editando = null;
        renderizarTarefas();
    });
});

carregarUsuarios();
