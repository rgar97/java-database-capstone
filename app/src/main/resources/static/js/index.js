const API_BASE_URL = "/api";

window.selectRole = function (role) {
    if (role === "patient") {
        localStorage.setItem("userRole", "patient");
        window.location.href = "/pages/patientDashboard.html";
        return;
    }

    if (role === "admin" || role === "doctor") {
        showLoginForm(role);
    }
};

function showLoginForm(role) {
    const modal = document.getElementById("modal");
    const modalBody = document.getElementById("modal-body");
    if (!modal || !modalBody) return;

    const isAdmin = role === "admin";
    const identifierLabel = isAdmin ? "Usuario" : "Correo electrónico";
    const identifierName = isAdmin ? "username" : "identifier";

    modalBody.innerHTML = `
        <h2>Iniciar sesión como ${isAdmin ? "administrador" : "doctor"}</h2>
        <form id="roleLoginForm">
            <label for="roleIdentifier">${identifierLabel}</label>
            <input id="roleIdentifier" name="${identifierName}" type="${isAdmin ? "text" : "email"}" required>
            <label for="rolePassword">Contraseña</label>
            <input id="rolePassword" name="password" type="password" required>
            <button type="submit">Iniciar sesión</button>
            <p id="roleLoginError" role="alert"></p>
        </form>
    `;
    modal.style.display = "flex";
    document.body.classList.add("modal-open");

    document.getElementById("closeModal").onclick = closeLoginForm;
    document.getElementById("roleLoginForm").addEventListener("submit", (event) => {
        event.preventDefault();
        loginByRole(role, event.currentTarget);
    });
}

async function loginByRole(role, form) {
    const error = document.getElementById("roleLoginError");
    const formData = new FormData(form);
    const credentials = Object.fromEntries(formData.entries());
    const endpoint = role === "admin" ? `${API_BASE_URL}/admin` : `${API_BASE_URL}/doctor/login`;

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(credentials)
        });
        const data = await response.json().catch(() => ({}));

        if (!response.ok || !data.token) {
            error.textContent = data.message || "Las credenciales no son válidas.";
            return;
        }

        localStorage.setItem("token", data.token);
        localStorage.setItem("userRole", role);
        window.location.assign(`/${role}Dashboard?token=${encodeURIComponent(data.token)}`);
    } catch (requestError) {
        console.error("Error al iniciar sesión:", requestError);
        error.textContent = "No se pudo conectar con el servidor.";
    }
}

function closeLoginForm() {
    const modal = document.getElementById("modal");
    const modalBody = document.getElementById("modal-body");
    if (modal) modal.style.display = "none";
    if (modalBody) modalBody.replaceChildren();
    document.body.classList.remove("modal-open");
}
