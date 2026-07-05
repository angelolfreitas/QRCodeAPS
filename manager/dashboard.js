document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem('spda_token');
    const role = localStorage.getItem('spda_role');
    // 1. Verifica se está logado
    if (!token) {
        alert("Você precisa estar logado para acessar o painel!");
        window.location.href = "../login/login.html";
        return;
    }

    // 2. Verifica se é ADMIN
    if (role !== 'MANAGER') {
        alert("Acesso Negado! Apenas administradores podem acessar esta página.");
        window.location.href = "../login/login.html";
        return;
    }

    
    carregarIndicadores(token); // ou atualizarTabelas(); no caso do config.js
});

async function carregarIndicadores(token) {
    try {
        const headers = { 'Authorization': `Bearer ${token}` };

        // Dispara as três requisições simultaneamente para otimizar o tempo de resposta
        const [respTotal, respVerificados, respNaoVerificados] = await Promise.all([
            fetch(`${API_BASE_URL}/resume/count/points`, { headers }),
            fetch(`${API_BASE_URL}/resume/count/points/verificados`, { headers }),
            fetch(`${API_BASE_URL}/resume/count/points/nao-verificados`, { headers })
        ]);

        // Verifica se alguma das rotas foi bloqueada (ex: erro 403 de permissão) ou falhou
        if (!respTotal.ok || !respVerificados.ok || !respNaoVerificados.ok) {
            throw new Error('Falha ao buscar os indicadores de resume. Verifique as permissões.');
        }

        // Como o Spring retorna um número direto (Long), o .json() converte perfeitamente
        const total = await respTotal.json();
        const verificados = await respVerificados.json();
        const naoVerificados = await respNaoVerificados.json();

        // Injeta os valores no HTML
        document.getElementById('ind-cadastrados').innerText = total;
        document.getElementById('ind-conformes').innerText = verificados;
        document.getElementById('ind-nao-conformes').innerText = naoVerificados;

        // Atualiza o gráfico do Chart.js
        renderizarGrafico(verificados, naoVerificados);

    } catch (erro) {
        console.error("Erro ao carregar dashboard:", erro);
        // Em caso de erro, coloca o "-" para não desconfigurar o visual
        document.getElementById('ind-cadastrados').innerText = "-";
        document.getElementById('ind-conformes').innerText = "-";
        document.getElementById('ind-nao-conformes').innerText = "-";
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
                label: 'Pontos por situação de conformidade',
                data: [conformes, naoConformes],
                backgroundColor: ['#2ecc71', '#e74c3c']
            }]
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
        }
    });
}
