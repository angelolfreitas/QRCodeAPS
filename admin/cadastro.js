// Verifica se há token logado (Proteção do Admin)
const tokenAdmin = localStorage.getItem('spda_token');
if (!tokenAdmin) {
    alert("Acesso negado. Você precisa estar logado como Administrador.");
    window.location.href = "../login/login.html";
}

// =========================================================
// 1. CONTROLE DO CAMPO DINÂMICO (CREA)
// =========================================================
const selectPerfil = document.getElementById('perfil-cadastro');
const blocoCrea = document.getElementById('bloco-crea');
const inputCrea = document.getElementById('crea-cadastro');

if (selectPerfil) {
    selectPerfil.addEventListener('change', function() {
        if (selectPerfil.value === 'USER') {
            blocoCrea.style.display = 'block';
            inputCrea.setAttribute('required', 'true');
        } else {
            blocoCrea.style.display = 'none';
            inputCrea.removeAttribute('required');
            inputCrea.value = '';
        }
    });
}

// =========================================================
// 2. INTEGRAÇÃO COM O BACKEND (CRIANDO COM TOKEN)
// =========================================================
const formCadastro = document.getElementById('form-cadastro');

if (formCadastro) {
    formCadastro.addEventListener('submit', async function(evento) {
        evento.preventDefault();

        const registerRequestDTO = {
            name: document.getElementById('nome-cadastro').value,
            email: document.getElementById('email-cadastro').value,
            password: document.getElementById('senha-cadastro').value,
            equipe: document.getElementById('equipe-cadastro').value,
            crea: document.getElementById('crea-cadastro').value,
            role: document.getElementById('perfil-cadastro').value
        };

        try {
            const btnSubmit = formCadastro.querySelector('button[type="submit"]');
            const textoOriginal = btnSubmit.innerText;
            btnSubmit.innerText = "Processando...";
            btnSubmit.disabled = true;

            const resposta = await fetch(`${API_BASE_URL}/qr/admin/sign-up`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${tokenAdmin}` // <- Aqui vai a chave mestra do admin
                },
                body: JSON.stringify(registerRequestDTO)
            });

            if (resposta.ok) {
                alert("Membro cadastrado com sucesso na base de dados!");
                formCadastro.reset(); // Limpa o formulário para o próximo
                blocoCrea.style.display = 'none';
            } else {
                const erro = await resposta.text();
                alert(`Falha na autorização ou dados incorretos: ${erro}`);
            }

            btnSubmit.innerText = textoOriginal;
            btnSubmit.disabled = false;

        } catch (erro) {
            console.error("Erro de conexão:", erro);
            alert("Não foi possível conectar ao servidor.");
            const btnSubmit = formCadastro.querySelector('button[type="submit"]');
            btnSubmit.innerText = "Criar Conta";
            btnSubmit.disabled = false;
        }
    });
}