// =========================================================
// 1. CONTROLE DE VISIBILIDADE DA SENHA (OLHO)
// =========================================================
const btnAlternarSenha = document.getElementById('alternar-mostrar');
const inputSenha = document.getElementById('senha-cadastro');
const iconeOlho = document.getElementById('icone-olho');

if (btnAlternarSenha && inputSenha) {
    btnAlternarSenha.addEventListener('click', function() {
        if (inputSenha.type === 'password') {
            inputSenha.type = 'text';
            if (iconeOlho) {
                iconeOlho.classList.remove('fa-eye');
                iconeOlho.classList.add('fa-eye-slash');
            }
        } else {
            inputSenha.type = 'password';
            if (iconeOlho) {
                iconeOlho.classList.remove('fa-eye-slash');
                iconeOlho.classList.add('fa-eye');
            }
        }
    });
}

// =========================================================
// 2. INTEGRAÇÃO COM O BACKEND (ENVIO DO CADASTRO)
// =========================================================
const formCadastro = document.getElementById('form-cadastro');

if (formCadastro) {
    formCadastro.addEventListener('submit', async function(evento) {
        evento.preventDefault();

        const nomeInput = document.getElementById('nome-cadastro').value;
        const emailInput = document.getElementById('email-cadastro').value;
        const senhaInput = document.getElementById('senha-cadastro').value;
        const equipeInput = document.getElementById('equipe-cadastro').value;
        const creaInput = document.getElementById('crea-cadastro').value;

        // O backend provavelmente espera um campo 'role' (mesmo que assuma default, é bom garantir)
        const registerRequestDTO = {
            name: nomeInput,
            email: emailInput,
            password: senhaInput,
            equipe: equipeInput,
            crea: creaInput,
            role: "USER" // Força o nível de acesso como usuário padrão
        };

        try {
            const btnSubmit = formCadastro.querySelector('button[type="submit"]');
            const textoOriginal = btnSubmit.innerText;
            btnSubmit.innerText = "Cadastrando...";
            btnSubmit.disabled = true;

            const resposta = await fetch(`${API_BASE_URL}/auth/sign-up`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(registerRequestDTO)
            });

            if (resposta.ok) {
                alert("Conta criada com sucesso!");
                window.location.href = "../login/login.html";
            } else {
                const erro = await resposta.text();
                alert(`Erro ao cadastrar: ${erro}`);
            }

            btnSubmit.innerText = textoOriginal;
            btnSubmit.disabled = false;

        } catch (erro) {
            console.error("Erro de conexão:", erro);
            alert("Não foi possível conectar ao servidor.");
            const btnSubmit = formCadastro.querySelector('button[type="submit"]');
            btnSubmit.innerText = "Cadastrar";
            btnSubmit.disabled = false;
        }
    });
}