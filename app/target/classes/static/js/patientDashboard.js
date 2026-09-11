import { createDoctorCard } from "./components/doctorCard.js";
import { openModal } from "./components/modals.js";
import { getDoctors, filterDoctors } from "./services/doctorServices.js";
import { patientLogin, patientSignup } from "./services/patientServices.js";

document.addEventListener("DOMContentLoaded", () => {
    // 1. Cargar tarjetas de médicos al iniciar la página
    loadDoctorCards();

    // 2. Vincular disparadores de modal para Registro e Inicio de Sesión
    const signupBtn = document.getElementById("patientSignup");
    if (signupBtn) {
        signupBtn.addEventListener("click", () => openModal("patientSignup"));
    }

    const loginBtn = document.getElementById("patientLogin");
    if (loginBtn) {
        loginBtn.addEventListener("click", () => openModal("patientLogin"));
    }

    // 3. Vincular oyentes de eventos para Búsqueda y Filtro
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty = document.getElementById("filterSpecialty");

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
    if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
});

/**
 * Obtiene todos los médicos desde el backend y los renderiza en la vista.
 */
async function loadDoctorCards() {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    contentDiv.innerHTML = "<p>Cargando médicos...</p>";

    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Error al obtener la lista de médicos:", error);
        contentDiv.innerHTML = "<p>Ocurrió un error al cargar la información de los médicos.</p>";
    }
}

/**
 * Filtra los médicos en tiempo real en función de los criterios ingresados por el usuario.
 */
async function filterDoctorsOnChange() {
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty = document.getElementById("filterSpecialty");

    const nameVal = searchBar ? searchBar.value : "";
    const timeVal = filterTime ? filterTime.value : "";
    const specialtyVal = filterSpecialty ? filterSpecialty.value : "";

    const contentDiv = document.getElementById("content");
    if (contentDiv) {
        contentDiv.innerHTML = "<p>Buscando...</p>";
    }

    try {
        const doctors = await filterDoctors(nameVal, timeVal, specialtyVal);
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Error durante el filtrado de médicos:", error);
        if (contentDiv) {
            contentDiv.innerHTML = "<p>Error al aplicar los filtros.</p>";
        }
    }
}

/**
 * Utilidad de renderizado reutilizable para mostrar la lista de tarjetas de doctores.
 * @param {Array} doctors - Arreglo con la información de los médicos.
 */
export function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = "<p>No doctors found with the given filters.</p>";
        return;
    }

    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

/**
 * Maneja el registro de nuevos pacientes desde el formulario modal.
 */
window.signupPatient = async function (event) {
    if (event) event.preventDefault();

    const nameInput = document.getElementById("patientName") || document.getElementById("signupName");
    const emailInput = document.getElementById("patientEmail") || document.getElementById("signupEmail");
    const passwordInput = document.getElementById("patientPassword") || document.getElementById("signupPassword");
    const phoneInput = document.getElementById("patientPhone") || document.getElementById("signupPhone");
    const addressInput = document.getElementById("patientAddress") || document.getElementById("signupAddress");

    const patientData = {
        name: nameInput ? nameInput.value.trim() : "",
        email: emailInput ? emailInput.value.trim() : "",
        password: passwordInput ? passwordInput.value.trim() : "",
        phone: phoneInput ? phoneInput.value.trim() : "",
        address: addressInput ? addressInput.value.trim() : ""
    };

    if (!patientData.name || !patientData.email || !patientData.password) {
        alert("Por favor, complete todos los campos requeridos.");
        return;
    }

    try {
        const response = await patientSignup(patientData);

        if (response && response.success) {
            alert(response.message || "Registro exitoso.");
            
            // Cerrar el modal
            const modalContainer = document.getElementById("modalContainer") || document.getElementById("modal");
            if (modalContainer) {
                modalContainer.innerHTML = "";
                modalContainer.classList.add("hidden");
            }

            // Recargar la página para actualizar el estado
            window.location.reload();
        } else {
            alert(response.message || "No se pudo registrar al paciente.");
        }
    } catch (error) {
        console.error("Error en signupPatient:", error);
        alert("Ocurrió un error inesperado al registrar la cuenta.");
    }
};

/**
 * Maneja el inicio de sesión del paciente autenticando credenciales y guardando el token JWT.
 */
window.loginPatient = async function (event) {
    if (event) event.preventDefault();

    const emailInput = document.getElementById("loginEmail") || document.getElementById("patientLoginEmail");
    const passwordInput = document.getElementById("loginPassword") || document.getElementById("patientLoginPassword");

    const credentials = {
        email: emailInput ? emailInput.value.trim() : "",
        password: passwordInput ? passwordInput.value.trim() : ""
    };

    if (!credentials.email || !credentials.password) {
        alert("Por favor, ingrese su correo electrónico y contraseña.");
        return;
    }

    try {
        const response = await patientLogin(credentials);

        if (response.ok) {
            const data = await response.json();
            
            // Guardar el token JWT recuperado
            if (data.token) {
                localStorage.setItem("token", data.token);
            }
            if (data.id) {
                localStorage.setItem("patientId", data.id);
            }

            // Redirigir al panel del paciente con sesión iniciada
            window.location.href = "loggedPatientDashboard.html";
        } else {
            const errorData = await response.json().catch(() => ({}));
            alert(errorData.message || "Credenciales incorrectas. Verifique su correo y contraseña.");
        }
    } catch (error) {
        console.error("Error en loginPatient:", error);
        alert("Ocurrió un error al intentar iniciar sesión. Por favor, reintente más tarde.");
    }
};

// Exponer funciones globales
window.loadDoctorCards = loadDoctorCards;
window.filterDoctorsOnChange = filterDoctorsOnChange;