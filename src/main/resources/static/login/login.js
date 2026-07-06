document.getElementById('loginFormulario').addEventListener('submit', async (evento) => {
    evento.preventDefault(); // impede o recarregamento padrão do formulário

    // pegando os dados digitados nos inputs
    const email = document.getElementById('email').value;
    const senha = document.getElementById('senha').value;

    if (!email || !senha) {
        alert("Preencha todos os campos!");
        return;
    }

    try {
        // montando o DTO LoginRequest
        const loginRequestDTO = {
            email: email,
            password: senha
        };

        // enviando para o backend
        const resposta = await fetch(`${API_BASE_URL}/auth/sign-in`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginRequestDTO)
        });

        // tratando a resposta
        if (resposta.ok) {
            // Recebendo o DTO LoginResponse (nome, token, role)
            const dados = await resposta.json();
            
            // salvando o Token no navegador para usar nas próximas requisições
            localStorage.setItem('spda_token', dados.token);
            localStorage.setItem('spda_role', dados.role);
            localStorage.setItem('spda_nome', dados.name);
            const nivelAcesso = dados.role.toUpperCase();
            // redireciona o usuário para o painel administrativo
            if (nivelAcesso === 'ADMIN') {
                window.location.href = "../admin/dashboard.html";
            } else if (nivelAcesso === 'MANAGER') {
                window.location.href = "../manager/dashboard.html"; // Mude para o caminho correto do manager
            } else {
                window.location.href = "../user/pontos.html"; // Mude para a tela inicial do inspetor (user)
            }
        } else {
            alert("E-mail ou senha incorretos. Tente novamente.");
        }

    } catch (erro) {
        console.error("Erro de conexão:", erro);
        alert("Não foi possível conectar ao servidor. O backend está rodando?");
    }
});