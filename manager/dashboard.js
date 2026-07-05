const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem('spda_token');
    const role = localStorage.getItem('spda_role');

    // 1. Verifica se está logado
    if (!token) {
        alert("Sessão expirada. Faça login novamente.");
        window.location.href = "../login/login.html";
        return;
    }

    // 2. Trava de Segurança: Aceita MANAGER ou ADMIN
    if (role !== 'MANAGER' && role !== 'ADMIN') {
        alert("Acesso Negado! Você não tem permissão de Gestor.");
        window.location.href = "../login/login.html";
        return;
    }

    // Se passou pelas travas, carrega os dados
    carregarIndicadores(token);
});

async function carregarIndicadores(token) {
    try {
        const resposta = await fetch(`${API_BASE_URL}/api/dashboard`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!resposta.ok) throw new Error('Falha ao carregar indicadores');

        const dados = await resposta.json();

        document.getElementById('ind-cadastrados').innerText = dados.totalPontos || 0;
        document.getElementById('ind-conformes').innerText = dados.totalConformes || 0;
        document.getElementById('ind-nao-conformes').innerText = dados.totalNaoConformes || 0;

        renderizarGrafico(dados.totalConformes || 0, dados.totalNaoConformes || 0);
    } catch (erro) {
        console.error("Erro no dashboard:", erro);
    }
}

function renderizarGrafico(conformes, naoConformes) {
    const ctx = document.getElementById('graficoConformidade');
    if (!ctx || typeof Chart === 'undefined') return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Conformes', 'Não Conformes'],
            datasets: [{
                label: 'Status dos Pontos',
                data: [conformes, naoConformes],
                backgroundColor: ['#2ecc71', '#e74c3c']
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } }
        }
    });
}