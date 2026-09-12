import { deleteDoctor, getPatientData } from "../services/patientServices.js";
import { showBookingOverlay } from "./modals.js";

/**
 * Crea una tarjeta dinámica reutilizable para mostrar información del doctor.
 * @param {Object} doctor - Objeto con los datos del doctor.
 * @returns {HTMLElement} Elemento DOM de la tarjeta del doctor.
 */
export function createDoctorCard(doctor) {
    // Contenedor principal de la tarjeta
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    // Obtener el rol del usuario desde localStorage
    const role = localStorage.getItem("userRole");

    // Sección de información del doctor
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name || "Dr. Desconocido";

    const specialization = document.createElement("p");
    specialization.textContent = `Especialidad: ${doctor.specialty || doctor.specialization || "General"}`;

    const email = document.createElement("p");
    email.textContent = `Correo: ${doctor.email || "No disponible"}`;

    const availability = document.createElement("p");
    const availabilityText = Array.isArray(doctor.availability)
        ? doctor.availability.join(", ")
        : doctor.availability || "Consultar disponibilidad";
    availability.textContent = `Horario: ${availabilityText}`;

    // Añadir elementos de información al contenedor
    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    // Contenedor para acciones/botones
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    // Botones condicionales según el rol
    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.classList.add("btn-delete");

        removeBtn.addEventListener("click", async () => {
            const confirmed = confirm(`¿Estás seguro de que deseas eliminar al Dr. ${doctor.name}?`);
            if (!confirmed) return;

            const token = localStorage.getItem("token");
            try {
                const result = await deleteDoctor(doctor.id, token);
                if (result.success) {
                    card.remove();
                } else {
                    alert(result.message || "Error al eliminar el doctor. Inténtalo de nuevo.");
                }
            } catch (error) {
                console.error("Error al eliminar el doctor:", error);
                alert("Ocurrió un error en el servidor al intentar eliminar.");
            }
        });

        actionsDiv.appendChild(removeBtn);

    } else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("btn-book");

        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });

        actionsDiv.appendChild(bookNow);

    } else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("btn-book");

        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");
            try {
                const patientData = await getPatientData(token);
                showBookingOverlay(e, doctor, patientData);
            } catch (error) {
                console.error("Error al obtener datos del paciente:", error);
                alert("No se pudo iniciar el proceso de reserva.");
            }
        });

        actionsDiv.appendChild(bookNow);
    }

    // Ensamblaje final
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    return card;
}