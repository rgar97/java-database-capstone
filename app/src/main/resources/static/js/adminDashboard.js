import { createDoctorCard } from "./components/doctorCard.js";
import { closeModal, openModal } from "./components/modals.js";
import { filterDoctors, getDoctors, saveDoctor } from "./services/doctorServices.js";

// Esperar a que el DOM esté completamente cargado para inicializar la interfaz
document.addEventListener("DOMContentLoaded", () => {
    // 1. Vincular el botón "Agregar Médico" para abrir el modal
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
        addDocBtn.addEventListener("click", () => {
            openModal("addDoctor");
        });
    }

    // 2. Configurar oyentes de eventos para búsqueda y filtrado en tiempo real
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime") || document.getElementById("sortByTime");
    const filterSpecialty = document.getElementById("filterSpecialty") || document.getElementById("filterBySpecialty");

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
    if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);

    // 3. Vincular el formulario de agregar médico
    const addDoctorForm = document.getElementById("addDoctorForm");
    if (addDoctorForm) {
        addDoctorForm.addEventListener("submit", adminAddDoctor);
    }

    // 4. Cargar la lista inicial de médicos
    loadDoctorCards();
});

/**
 * Obtiene todos los médicos del backend y los renderiza en el tablero.
 */
async function loadDoctorCards() {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    contentDiv.innerHTML = "<p>Cargando médicos...</p>";

    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Error al cargar tarjetas de médicos:", error);
        contentDiv.innerHTML = "<p>Ocurrió un error al cargar la lista de médicos.</p>";
    }
}

/**
 * Renderiza la lista de médicos recibida en el contenedor principal.
 * @param {Array} doctors - Arreglo de objetos doctor.
 */
function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = "<p>No se encontraron médicos</p>";
        return;
    }

    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

/**
 * Maneja el evento de cambio/entrada en los filtros y actualiza la lista de médicos.
 */
async function filterDoctorsOnChange() {
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime") || document.getElementById("sortByTime");
    const filterSpecialty = document.getElementById("filterSpecialty") || document.getElementById("filterBySpecialty");

    const nameVal = searchBar ? searchBar.value : "";
    const timeVal = filterTime ? filterTime.value : "";
    const specialtyVal = filterSpecialty ? filterSpecialty.value : "";

    const contentDiv = document.getElementById("content");
    if (contentDiv) {
        contentDiv.innerHTML = "<p>Buscando...</p>";
    }

    const filtered = await filterDoctors(nameVal, timeVal, specialtyVal);
    renderDoctorCards(filtered);
}

/**
 * Recoge los datos del formulario de registro de médico y procesa la solicitud.
 * @param {Event} e - Evento de envío del formulario.
 */
async function adminAddDoctor(e) {
    if (e) e.preventDefault();

    const token = localStorage.getItem("token");
    if (!token) {
        alert("Sesión no válida o expirada. Por favor, inicie sesión como administrador.");
        return;
    }

    // Recoger valores de los campos de entrada
    const nameInput = document.getElementById("docName") || document.getElementById("doctorName");
    const specialtyInput = document.getElementById("docSpecialty") || document.getElementById("doctorSpecialty");
    const emailInput = document.getElementById("docEmail") || document.getElementById("doctorEmail");
    const passwordInput = document.getElementById("docPassword") || document.getElementById("doctorPassword");
    const mobileInput = document.getElementById("docMobile") || document.getElementById("doctorMobile");

    // Recoger los valores seleccionados de los checkboxes de disponibilidad
    const availabilityCheckboxes = document.querySelectorAll("input[name='availability']:checked");
    const availability = Array.from(availabilityCheckboxes).map(cb => cb.value);

    // Construir el objeto del médico
    const newDoctor = {
        name: nameInput ? nameInput.value.trim() : "",
        specialty: specialtyInput ? specialtyInput.value.trim() : "",
        email: emailInput ? emailInput.value.trim() : "",
        password: passwordInput ? passwordInput.value.trim() : "",
        phone: mobileInput ? mobileInput.value.trim() : "",
        availableTimes: availability
    };

    // Validar campos obligatorios
    if (!newDoctor.name || !newDoctor.email || !newDoctor.password) {
        alert("Por favor, complete todos los campos obligatorios.");
        return;
    }

    // Enviar solicitud al servicio de doctores
    const result = await saveDoctor(newDoctor, token);

    if (result && result.success) {
        alert(result.message || "Médico agregado exitosamente.");
        
        // Cerrar el modal limpiando el contenedor del modal si aplica
        const modalContainer = document.getElementById("modalContainer") || document.getElementById("modal");
        if (modalContainer) {
            closeModal();
        }

        // Limpiar el formulario
        const form = document.getElementById("addDoctorForm");
        if (form) form.reset();

        // Refrescar la lista de médicos
        loadDoctorCards();
    } else {
        alert(result.message || "Error al agregar el médico. Verifique los datos ingresados.");
    }
}

// Hacer las funciones accesibles globalmente para vincular desde modales o HTML si es necesario
window.loadDoctorCards = loadDoctorCards;
window.adminAddDoctor = adminAddDoctor;