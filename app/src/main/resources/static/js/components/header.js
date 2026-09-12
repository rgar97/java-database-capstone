/**
 * Componente de encabezado dinámico reutilizable
 */

// Función global para cerrar sesión de admin/doctor/usuarios generales
window.logout = function () {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
};

// Función global para cerrar sesión de paciente autenticado
window.logoutPatient = function () {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient");
    window.location.href = "/pages/patientDashboard.html";
};

function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return;

    // Si estamos en la página de inicio, limpiamos credenciales
    if (window.location.pathname.endsWith("/") || window.location.pathname.endsWith("/index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
    }

    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // Validación de sesión/token para roles protegidos
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        alert("Sesión expirada o inicio de sesión inválido. Por favor, inicie sesión nuevamente.");
        window.location.href = "/";
        return;
    }

    let headerContent = `
        <header class="header">
            <div class="logo-container">
                <a href="/">
                    <img src="../assets/images/logo/logo.png" alt="CMS Logo" class="logo" onerror="this.src='/assets/images/logo/logo.png'" />
                </a>
            </div>
            <nav class="nav-links">
    `;

    // Inyección condicional según el rol de usuario
    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="adminBtn">Agregar Doctor</button>
            <a href="#" onclick="logout()">Cerrar sesión</a>
        `;
    } else if (role === "doctor") {
        headerContent += `
            <a href="/doctorDashboard">Inicio</a>
            <a href="#" onclick="logout()">Cerrar sesión</a>
        `;
    } else if (role === "patient") {
        headerContent += `
            <button id="loginBtn" class="btn-nav">Iniciar sesión</button>
            <button id="registerBtn" class="btn-nav">Registrarse</button>
        `;
    } else if (role === "loggedPatient") {
        headerContent += `
            <a href="/pages/patientDashboard.html">Inicio</a>
            <a href="/pages/patientAppointments.html">Citas</a>
            <a href="#" onclick="logoutPatient()">Cerrar sesión</a>
        `;
    }

    headerContent += `
            </nav>
        </header>
    `;

    // Insertar HTML en el DOM
    headerDiv.innerHTML = headerContent;

    // Adjuntar listeners después de la inyección en el DOM
    attachHeaderButtonListeners();
}

function attachHeaderButtonListeners() {
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn && typeof window.openModal === "function") {
        addDocBtn.addEventListener("click", () => window.openModal("addDoctor"));
    }

    const loginBtn = document.getElementById("loginBtn");
    if (loginBtn && typeof window.openModal === "function") {
        loginBtn.addEventListener("click", () => window.openModal("patientLogin"));
    }

    const registerBtn = document.getElementById("registerBtn");
    if (registerBtn && typeof window.openModal === "function") {
        registerBtn.addEventListener("click", () => window.openModal("patientSignup"));
    }
}

// Ejecutar el renderizado cuando el DOM esté completamente cargado
document.addEventListener("DOMContentLoaded", renderHeader);