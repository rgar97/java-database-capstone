import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

// Inicializar Variables Globales
let patientTableBody = null;
let selectedDate = new Date().toISOString().split("T")[0]; // Fecha actual en formato YYYY-MM-DD
let token = localStorage.getItem("token") || "";
let patientName = null;

document.addEventListener("DOMContentLoaded", () => {
    // Referencia al cuerpo de la tabla de citas
    patientTableBody = document.getElementById("patientTableBody");

    // Configurar el selector de fecha con la fecha de hoy por defecto
    const datePicker = document.getElementById("datePicker");
    if (datePicker) {
        datePicker.value = selectedDate;
        datePicker.addEventListener("change", (e) => {
            selectedDate = e.target.value;
            loadAppointments();
        });
    }

    // Configurar la funcionalidad de la barra de búsqueda
    const searchBar = document.getElementById("searchBar");
    if (searchBar) {
        searchBar.addEventListener("input", (e) => {
            const value = e.target.value.trim();
            patientName = value === "" ? null : value;
            loadAppointments();
        });
    }

    // Botón de "Citas de Hoy"
    const todayButton = document.getElementById("todayButton");
    if (todayButton) {
        todayButton.addEventListener("click", () => {
            selectedDate = new Date().toISOString().split("T")[0];
            if (datePicker) {
                datePicker.value = selectedDate;
            }
            loadAppointments();
        });
    }

    // Renderizado e inicio de carga de citas
    loadAppointments();
});

/**
 * Carga y renderiza las citas según la fecha seleccionada y el término de búsqueda.
 */
async function loadAppointments() {
    if (!patientTableBody) {
        patientTableBody = document.getElementById("patientTableBody");
        if (!patientTableBody) return;
    }

    // Limpiar contenido existente de la tabla
    patientTableBody.innerHTML = "";

    try {
        // Recuperar token actualizado en caso de cambio de sesión
        token = localStorage.getItem("token") || "";

        // Obtener citas desde el servicio
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        // Si no se encuentran citas o la lista está vacía
        if (!appointments || appointments.length === 0) {
            const emptyRow = document.createElement("tr");
            emptyRow.innerHTML = `
                <td colspan="6" style="text-align: center; padding: 20px;">
                    No se encontraron citas para la fecha seleccionada.
                </td>
            `;
            patientTableBody.appendChild(emptyRow);
            return;
        }

        // Si existen citas, iterar y agregar cada fila usando createPatientRow
        appointments.forEach(appointment => {
            const row = createPatientRow(appointment);
            if (row) {
                patientTableBody.appendChild(row);
            }
        });

    } catch (error) {
        console.error("Error al cargar las citas del médico:", error);
        
        // Fila alternativa con mensaje de error
        patientTableBody.innerHTML = "";
        const errorRow = document.createElement("tr");
        errorRow.innerHTML = `
            <td colspan="6" style="text-align: center; color: red; padding: 20px;">
                Ocurrió un error al cargar las citas. Por favor, inténtelo de nuevo más tarde.
            </td>
        `;
        patientTableBody.appendChild(errorRow);
    }
}

// Hacer loadAppointments accesible globalmente por si se requiere refrescar desde un modal o evento externo
window.loadAppointments = loadAppointments;