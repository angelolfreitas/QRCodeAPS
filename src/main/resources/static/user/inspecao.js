let codigoPontoAtual = null;

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem('spda_token');

    // Se não tiver token, manda pro login
    if (!token) {
        alert("Sua sessão expirou ou você não está logado.");
        window.location.href = "../login/login.html";
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    codigoPontoAtual = urlParams.get('codigo');

    if (!codigoPontoAtual) {
        document.getElementById('codigoPonto').innerText = "Nenhum código informado";
        return;
    }

    buscarDadosDoPonto(codigoPontoAtual);
    document.getElementById('formularioInspecao').addEventListener('submit', salvarInspecao);
});

function alternarTelas(tela) {
    const ficha = document.getElementById('telaFicha');
    const formulario = document.getElementById('telaFormulario');
    
    if (tela === 'formulario') {
        ficha.style.display = 'none';
        formulario.style.display = 'block';
        window.scrollTo(0, 0);
    } else {
        ficha.style.display = 'block';
        formulario.style.display = 'none';
        window.scrollTo(0, 0);
    }
}

async function buscarDadosDoPonto(codigo) {
    try {
        const resposta = await fetch(`${API_BASE_URL}/api/pontos/${codigo}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('spda_token')}` }
        });

        if (!resposta.ok) {
            document.getElementById('codigoPonto').innerText = "Ponto não encontrado";
            document.getElementById('statusPonto').innerText = "-";
            return;
        }

        const data = await resposta.json();

        document.getElementById('codigoPonto').innerText = data.codigo;
        document.getElementById('pontoCliente').innerText = data.cliente;
        document.getElementById('pontoArea').innerText = data.area;
        document.getElementById('pontoTipo').innerText = data.tipo;
        document.getElementById('pontoLocalizacao').innerText = data.localizacao;
        document.getElementById('pontoDescricao').innerText = data.descricao || '-';
        document.getElementById('statusPonto').innerText = data.status;

        const badge = document.getElementById('badgeCriticidade');
        badge.innerText = data.criticidade || '-';
        badge.className = 'criticidade ' + (data.criticidade ? data.criticidade.toLowerCase() : '');

        renderizarHistorico(data.historico || []);
    } catch (erro) {
        console.error("Erro ao buscar ponto:", erro);
        document.getElementById('codigoPonto').innerText = "Erro ao conectar ao servidor";
    }
}

function renderizarHistorico(historico) {
    const container = document.getElementById('containerHistorico');

    if (!historico || historico.length === 0) {
        container.innerHTML = '<p class="msg-vazio">Nenhum registro encontrado para este ponto.</p>';
        return;
    }

    container.innerHTML = historico.map(item => {
        const data = new Date(item.dataInspecao).toLocaleString('pt-BR');
        const conformeTexto = item.conforme ? 'Conforme' : 'Não Conforme';
        return `
            <div class="item-historico">
                <p><strong>${data}</strong> — ${item.responsavel || '-'} — <em>${conformeTexto}</em></p>
                <p>Resistência: ${item.resistenciaAterramento ?? '-'} Ω | Continuidade: ${item.continuidadeEletrica ?? '-'} mΩ | Condição: ${item.condicaoVisual ?? '-'}</p>
                ${item.observacoes ? `<p>Obs: ${item.observacoes}</p>` : ''}
            </div>
        `;
    }).join('');
}

async function salvarInspecao(event) {
    event.preventDefault();

    if (!codigoPontoAtual) {
        alert("Não há um ponto selecionado.");
        return;
    }

    // 1. Coleta os dados que o usuário digitou (como você já fazia)
    const dadosInspecao = {
        responsavel: document.getElementById('formResponsavel').value,
        resistenciaAterramento: parseFloat(document.getElementById('formResistencia').value),
        continuidadeEletrica: parseInt(document.getElementById('formContinuidade').value),
        condicaoVisual: document.getElementById('formCondicao').value,
        possuiOxidacao: document.getElementById('formOxidacao').checked,
        necessitaCorrecao: document.getElementById('formCorrecao').checked,
        conforme: document.getElementById('formConformidade').value === "true",
        observacoes: document.getElementById('formObservacoes').value
    };

    try {
        // Mudar o botão para "Gerando PDF..."
        const btnSubmit = document.querySelector('.btn-principal.salvar');
        const txtOriginal = btnSubmit.innerText;
        btnSubmit.innerText = "Gerando Laudo em PDF...";
        btnSubmit.disabled = true;

        // 2. Configura e gera o PDF da tela inteira do formulário
        const elementoParaPDF = document.getElementById('telaFormulario'); // Ou 'telaFicha' se preferir a ficha de histórico

        const opcoesPDF = {
            margin:       10,
            filename:     `inspecao-${codigoPontoAtual}.pdf`,
            image:        { type: 'jpeg', quality: 0.98 },
            html2canvas:  { scale: 2, useCORS: true },
            jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
        };

        // Transforma o HTML em um "Blob" (Arquivo bruto em memória)
        const pdfBlob = await html2pdf().set(opcoesPDF).from(elementoParaPDF).output('blob');

        // 3. Empacota o PDF e os Dados JSON juntos no FormData
        const formData = new FormData();
        formData.append("arquivoPdf", pdfBlob, `inspecao-${codigoPontoAtual}.pdf`);
        formData.append("dadosInspecao", new Blob([JSON.stringify(dadosInspecao)], { type: "application/json" }));

        // 4. Envia tudo para o Spring Boot
        btnSubmit.innerText = "Salvando na Nuvem...";

        const resposta = await fetch(`${API_BASE_URL}/api/pontos/codigo/${codigoPontoAtual}/inspecoes`, {
            method: 'POST',
            headers: {
                // ATENÇÃO: Quando enviamos FormData (arquivos), NÃO colocamos 'Content-Type'.
                // O navegador faz isso automaticamente com um "boundary".
                'Authorization': `Bearer ${localStorage.getItem('spda_token')}`
            },
            body: formData
        });

        if (resposta.ok) {
            const qrCodeDados = await resposta.json();

            alert('Inspeção salva com sucesso!\nO Laudo em PDF e o QR Code foram gerados na AWS.');

            // Opcional: Se quiser mostrar a imagem do QRCode pro usuário na hora
            console.log("Link da imagem do QR Code:", qrCodeDados.url);

            event.target.reset();
            await buscarDadosDoPonto(codigoPontoAtual);
            alternarTelas('ficha');
        } else {
            const erro = await resposta.text();
            alert(`Erro ao registrar inspeção: ${erro}`);
        }
    } catch (erro) {
        console.error("Erro de conexão:", erro);
        alert("Não foi possível conectar ao servidor para enviar o PDF.");
    } finally {
        // Restaura o botão
        const btnSubmit = document.querySelector('.btn-principal.salvar');
        if (btnSubmit) {
            btnSubmit.innerText = "Salvar Registro";
            btnSubmit.disabled = false;
        }
    }
}
