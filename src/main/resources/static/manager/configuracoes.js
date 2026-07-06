const API_URL = `${API_BASE_URL}/api`;

function getToken() {
    const token = localStorage.getItem('spda_token');
    if (!token) {
        alert("Você precisa estar logado para acessar as configurações!");
        window.location.href = "../login/login.html";
    }
    return token;
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
    };
}

document.addEventListener("DOMContentLoaded", () => {
    getToken();

    atualizarTabelas();

    document.getElementById('formConfigCliente').addEventListener('submit', (e) => cadastrarItem(e, 'clientes', 'cfgNomeCliente', 'cfgSiglaCliente'));
    document.getElementById('formConfigArea').addEventListener('submit', (e) => cadastrarItem(e, 'areas', 'cfgNomeArea', 'cfgSiglaArea'));
    document.getElementById('formConfigTipo').addEventListener('submit', (e) => cadastrarItem(e, 'tipos', 'cfgNomeTipo', 'cfgSiglaTipo'));
});

async function cadastrarItem(evento, rota, idCampoNome, idCampoSigla) {
    evento.preventDefault();

    const nome = document.getElementById(idCampoNome).value;
    const sigla = document.getElementById(idCampoSigla).value;

    try {
        const resposta = await fetch(`${API_URL}/${rota}`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ nome, sigla })
        });

        if (resposta.ok) {
            evento.target.reset();
            atualizarTabelas();
        } else {
            const erro = await resposta.text();
            alert(`Erro ao cadastrar: ${erro}`);
        }
    } catch (erro) {
        console.error("Erro de conexão:", erro);
        alert("Não foi possível conectar ao servidor.");
    }
}

async function atualizarTabelas() {
    await carregarLista('clientes', 'lista-clientes');
    await carregarLista('areas', 'lista-areas');
    await carregarLista('tipos', 'lista-tipos');
}

async function carregarLista(rotaApi, idElementoTbody) {
    try {
        const resposta = await fetch(`${API_URL}/${rotaApi}`, {
            headers: authHeaders()
        });

        if (!resposta.ok) throw new Error('Falha ao carregar lista');

        const itens = await resposta.json();
        renderizarConfigLista(itens, idElementoTbody, rotaApi);
    } catch (erro) {
        console.error(`Erro ao carregar ${rotaApi}:`, erro);
    }
}

function renderizarConfigLista(itens, idElementoTbody, rotaApi) {
    const tbody = document.getElementById(idElementoTbody);
    tbody.innerHTML = '';

    if (itens.length === 0) {
        tbody.innerHTML = `<tr><td colspan="2" style="color:#888; font-size:12px;">Vazio.</td></tr>`;
        return;
    }

    itens.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${item.sigla}</strong> - ${item.nome}</td>
            <td style="text-align:right;">
                <button class="btn-deletar" data-id="${item.id}" data-rota="${rotaApi}">🗑️</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    vincularEventosDelecao();
}

function vincularEventosDelecao() {
    document.querySelectorAll('.btn-deletar').forEach(btn => {
        btn.onclick = function() {
            const id = this.getAttribute('data-id');
            const rota = this.getAttribute('data-rota');

            if(confirm("Deseja realmente remover esta sigla de parametrização?")) {
                efetuarExclusao(id, rota);
            }
        }
    });
}

async function efetuarExclusao(id, rota) {
    try {
        const resposta = await fetch(`${API_URL}/${rota}/${id}`, {
            method: 'DELETE',
            headers: authHeaders()
        });

        if (resposta.ok) {
            atualizarTabelas();
        } else {
            const erro = await resposta.text();
            alert(`Erro ao excluir: ${erro}`);
        }
    } catch (erro) {
        console.error("Erro na requisição: ", erro);
    }
}
