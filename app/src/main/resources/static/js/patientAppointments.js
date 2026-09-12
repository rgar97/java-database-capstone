import { getPatientAppointments, getPatientData } from "./services/patientServices.js";

const state = document.getElementById("appointmentsState");
const tableContainer = document.getElementById("appointmentsTableContainer");
const tableBody = document.getElementById("appointmentsTableBody");

function formatDate(value) {
    if (!value) return "-";
    return new Date(value).toLocaleString("es-ES", {
        dateStyle: "medium",
        timeStyle: "short"
    });
}

function renderAppointments(appointments) {
    if (!appointments.length) {
        state.textContent = "No tienes citas reservadas todavía.";
        tableContainer.hidden = true;
        return;
    }

    tableBody.replaceChildren();
    appointments.forEach(appointment => {
        const doctor = appointment.doctor || {};
        const row = document.createElement("tr");
        [
            doctor.name || appointment.doctorName || "-",
            doctor.specialty || "-",
            formatDate(appointment.appointmentTime),
            Number(appointment.status) === 1 ? "Completada" : "Programada"
        ].forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });
        tableBody.appendChild(row);
    });

    state.textContent = "";
    tableContainer.hidden = false;
}

async function loadAppointments() {
    const token = localStorage.getItem("token");
    if (!token || localStorage.getItem("userRole") !== "loggedPatient") {
        state.textContent = "Inicia sesión como paciente para consultar tus citas.";
        return;
    }

    try {
        const patientData = await getPatientData(token);
        const patient = patientData?.patient || patientData;
        if (!patient?.id) {
            throw new Error("No se pudo identificar al paciente.");
        }

        const response = await getPatientAppointments(patient.id, token, "patient");
        const appointments = Array.isArray(response) ? response : (response?.appointments || []);
        renderAppointments(appointments);
    } catch (error) {
        console.error("Error al cargar las citas:", error);
        state.textContent = "No se pudieron cargar las citas. Inténtalo de nuevo más tarde.";
        tableContainer.hidden = true;
    }
}

document.addEventListener("DOMContentLoaded", loadAppointments);
