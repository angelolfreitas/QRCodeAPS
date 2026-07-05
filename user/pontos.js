// Substitua pelo caminho correto da sua API se necessário (ex: baseado no seu config.js)
const API_URL = `${API_BASE_URL}/api`;

function getToken() {
    const token = localStorage.getItem('spda_token');
    if (!token) {
        alert("Você precisa estar logado!");
        window.location.href = "../login/login.html";
    }
    return token;
}

document.addEventListener("DOMContentLoaded", () => {
    // Exibe o nome do usuário logado no menu lateral
    const nomeUnsuario = localStorage.getItem('spda_nome');
    if (nomeUnsuario) {
        document.getElementById('nomeInspetor').innerText = nomeUnsuario;
    }

    carregarPontosParaInspetor();
});

async function carregarPontosParaInspetor() {
    try {
        const resposta = await fetch(`${API_URL}/pontos`, {
            headers: {
                'Authorization': `Bearer ${getToken()}`
            }
        });

        if (resposta.ok) {
            const pontos = await resposta.json();
            renderizarTabela(pontos);
        } else {
            console.error("Erro ao buscar pontos do servidor");
        }
    } catch (erro) {
        console.error("Erro de conexão:", erro);
    }
}

function renderizarTabela(pontos) {
    const tbody = document.getElementById('lista-pontos-inspetor');
    tbody.innerHTML = '';

    if (pontos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:#888;">Nenhum ponto cadastrado no sistema.</td></tr>`;
        return;
    }

    pontos.forEach(ponto => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>${ponto.codigo}</strong></td>
            <td>${ponto.localizacao || 'Não informada'}</td>
            <td><span class="badge-criticidade ${ponto.criticidade ? ponto.criticidade.toLowerCase() : ''}">${ponto.criticidade || 'Média'}</span></td>
            <td style="text-align: center;">
                <a href="inspecao.html?codigo=${ponto.codigo}" class="btn-abrir-ficha" style="padding: 6px 12px; background: #6366f1; color: white; border-radius: 4px; text-decoration: none; font-size: 13px;">📋 Abrir Ficha</a>
            </td>
        `;
        tbody.appendChild(tr);
    });
}