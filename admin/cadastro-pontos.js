const API_URL = `${API_BASE_URL}/api`;

function getToken() {
    const token = localStorage.getItem('spda_token');
    if (!token) {
        alert("Você precisa estar logado para acessar esta página!");
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

    carregarSelects();
    carregarPontosCadastrados();

    document.getElementById('formCadastroPontoCompleto').addEventListener('submit', cadastrarPonto);
});
async function carregarSelects() {
    await preencherUsuarios();
}

async function preencherUsuarios() {
    const select = document.getElementById('pontoSelectResponsavel');
    try {
        const resposta = await fetch(`${API_URL}/usuarios`, { headers: authHeaders() });
        const usuarios = await resposta.json();

        select.innerHTML = `<option value="" disabled selected>Selecione o responsável</option>`;

        if (usuarios.length === 0) {
            select.innerHTML = `<option value="" disabled selected>Nenhum usuário cadastrado</option>`;
            return;
        }

        usuarios.forEach(usuario => {
            const option = document.createElement('option');
            option.value = usuario.id;
            option.textContent = `${usuario.username} - ${usuario.email}`;
            select.appendChild(option);
        });
    } catch (erro) {
        console.error("Erro ao carregar usuários:", erro);
        select.innerHTML = `<option value="" disabled selected>Erro ao carregar</option>`;
    }
}

async function preencherSelect(rota, idSelect, placeholder) {
    const select = document.getElementById(idSelect);
    try {
        const resposta = await fetch(`${API_URL}/${rota}`, { headers: authHeaders() });
        const itens = await resposta.json();

        select.innerHTML = `<option value="" disabled selected>${placeholder}</option>`;

        if (itens.length === 0) {
            select.innerHTML = `<option value="" disabled selected>Nenhum cadastrado ainda</option>`;
            return;
        }

        itens.forEach(item => {
            const option = document.createElement('option');
            option.value = item.id;
            option.textContent = `${item.sigla} - ${item.nome}`;
            select.appendChild(option);
        });
    } catch (erro) {
        console.error(`Erro ao carregar ${rota}:`, erro);
        select.innerHTML = `<option value="" disabled selected>Erro ao carregar</option>`;
    }
}

async function cadastrarPonto(evento) {
    evento.preventDefault();

    const corpo = {
        responsavelId: document.getElementById('pontoSelectResponsavel').value,
        areaId: document.getElementById('pontoArea').value,
        localizacao: document.getElementById('pontoLocalizacao').value,
        criticidade: document.getElementById('pontoCriticidade').value,
        descricao: document.getElementById('pontoDescricao').value
    };

    try {
        const resposta = await fetch(`${API_URL}/pontos`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(corpo)
        });

        if (resposta.ok) {
            const ponto = await resposta.json();
            alert(`Ponto ${ponto.codigo} cadastrado com sucesso! QR Code gerado.`);
            evento.target.reset();
            carregarPontosCadastrados();
        } else {
            const erro = await resposta.text();
            alert(`Erro ao cadastrar ponto: ${erro}`);
        }
    } catch (erro) {
        console.error("Erro de conexão:", erro);
        alert("Não foi possível conectar ao servidor.");
    }
}

async function carregarPontosCadastrados() {
    try {
        const resposta = await fetch(`${API_URL}/pontos`, { headers: authHeaders() });
        const pontos = await resposta.json();
        renderizarTabelaPontos(pontos);
    } catch (erro) {
        console.error("Erro ao carregar pontos:", erro);
    }
}

function renderizarTabelaPontos(pontos) {
    const tbody = document.getElementById('lista-pontos-ativos');
    tbody.innerHTML = '';

    if (pontos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:#888;">Nenhum ponto registrado.</td></tr>`;
        return;
    }

    pontos.forEach(ponto => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${ponto.codigo}</strong>${ponto.qrCodeUrl ? ` <a href="${ponto.qrCodeUrl}" target="_blank">(ver QR)</a>` : ''}</td>
            <td>${ponto.localizacao}</td>
            <td><span class="badge-criticidade">${ponto.criticidade}</span></td>
            <td style="text-align: center;">
                <a href="../user/inspecao.html?codigo=${ponto.codigo}" class="btn-abrir-ficha">📋 Abrir Ficha</a>
                <button class="btn-deletar" data-id="${ponto.id}">🗑️ Remover</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    configurarExclusaoPontos();
}

function configurarExclusaoPontos() {
    document.querySelectorAll('#lista-pontos-ativos .btn-deletar').forEach(btn => {
        btn.onclick = function() {
            const idPonto = this.getAttribute('data-id');

            if (confirm("Atenção: Deletar este ponto removerá o histórico de medições e invalidará o QR Code gerado. Continuar?")) {
                fetch(`${API_URL}/pontos/${idPonto}`, { method: 'DELETE', headers: authHeaders() })
                    .then(res => {
                        if (res.ok) {
                            alert('Ponto removido com sucesso do banco de dados.');
                            carregarPontosCadastrados();
                        } else {
                            alert('Erro ao processar exclusão no servidor.');
                        }
                    }).catch(err => console.error("Erro crítico: ", err));
            }
        }
    });
}