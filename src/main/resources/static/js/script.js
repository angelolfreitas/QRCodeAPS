document.getElementById('btn-gerar').addEventListener('click', async () => {
    // capturando os elementos da tela
    const linkInput = document.getElementById('link-pdf').value;
    const areaResultado = document.getElementById('area-resultado');
    const imgQrCode = document.getElementById('qr-code-img');

    // validação simples para ver se o usuário digitou algo
    if (!linkInput) {
        alert("Por favor, insira o link do PDF!");
        return;
    }

    // verificando a segurança: Pegando o token que foi salvo no login ou cadastro
    const token = localStorage.getItem('spda_token');

    if (!token) {
        alert("Você precisa estar logado para gerar um QR Code!");
        window.location.href = "login.html";
        return;
    }

    try {
        // montando o DTO QRCodeRequest exatamente como o backend espera
        const qrCodeRequestDTO = {
            text: linkInput
        };

        /* enviando via POST para o backend; lembrete: Substituir 'http://localhost:8080' pela URL real onde o angelo ta rodando */

        // enviando via POST para o backend com o Token de Autorização
        const resposta = await fetch('http://localhost:8080/qr/admin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(qrCodeRequestDTO)
        });

        // tratando a resposta do backend
        if (resposta.ok) {
            // Extraindo o DTO QRCodeResponse
            const dados = await resposta.json();

            // pegando a URL da imagem e jogando na tag <img>
            imgQrCode.src = dados.url;
            
            // exibindo a div que estava oculta
            areaResultado.style.display = 'block'; 
        } else {
            alert("Erro no servidor ao tentar gerar o QR Code. Verifique suas permissões.");
        }

    } catch (erro) {
        console.error("Falha de conexão:", erro);
        alert("Não foi possível conectar ao backend. Ele está rodando?");
    }
});