
import { bookAppointment } from "../services/patientServices.js";

export function openModal(type) {
    const modal = document.getElementById("modal");
    const body = document.getElementById("modal-body");
    if (!modal || !body) return;

    const forms = {
        addDoctor: `
            <h2>Agregar médico</h2>
            <form id="addDoctorForm">
                <label>Nombre <input id="docName" required></label>
                <label>Especialidad <input id="docSpecialty" required></label>
                <label>Correo <input id="docEmail" type="email" required></label>
                <label>Contraseña <input id="docPassword" type="password" required></label>
                <label>Teléfono <input id="docMobile" required></label>
                <fieldset>
                    <legend>Disponibilidad</legend>
                    ${["09:00", "10:00", "11:00", "14:00", "15:00"].map(time => `<label><input type="checkbox" name="availability" value="${time}">${time}</label>`).join("")}
                </fieldset>
                <button type="submit">Guardar médico</button>
            </form>`,
        patientSignup: `
            <h2>Registrarse</h2>
            <form onsubmit="signupPatient(event)">
                <input id="patientName" placeholder="Nombre" required>
                <input id="patientEmail" type="email" placeholder="Correo" required>
                <input id="patientPassword" type="password" placeholder="Contraseña" required>
                <input id="patientPhone" placeholder="Teléfono" required>
                <input id="patientAddress" placeholder="Dirección" required>
                <button type="submit">Registrarse</button>
            </form>`,
        patientLogin: `
            <h2>Iniciar sesión</h2>
            <form onsubmit="loginPatient(event)">
                <input id="loginEmail" type="email" placeholder="Correo" required>
                <input id="loginPassword" type="password" placeholder="Contraseña" required>
                <button type="submit">Iniciar sesión</button>
            </form>`
    };

    body.innerHTML = forms[type] || "";
    modal.style.display = "flex";

    const closeButton = document.getElementById("closeModal");
    if (closeButton) closeButton.onclick = closeModal;

    const form = document.getElementById("addDoctorForm");
    if (form) form.addEventListener("submit", window.adminAddDoctor);
}

export function closeModal() {
    const modal = document.getElementById("modal");
    const body = document.getElementById("modal-body");
    if (modal) modal.style.display = "none";
    if (body) body.replaceChildren();
}

export function showBookingOverlay(event, doctor, patientData) {
    const patient = patientData?.patient || patientData;
    const availableTimes = Array.isArray(doctor.availableTimes)
        ? doctor.availableTimes
        : (Array.isArray(doctor.availability) ? doctor.availability : []);
    const timeSlots = availableTimes.map((slot) => {
        const label = String(slot).trim();
        const startTime = label.split("-")[0].trim().slice(0, 5);
        return { label, startTime };
    }).filter(slot => /^\d{2}:\d{2}$/.test(slot.startTime));
    const overlay = document.createElement("div");
    overlay.className = "modalApp active";
    overlay.innerHTML = `
        <button type="button" class="close-booking" aria-label="Cerrar">&times;</button>
        <h2>Reservar cita con ${doctor.name || "el doctor"}</h2>
        <form id="bookingForm">
            <label>Fecha <input id="bookingDate" type="date" required></label>
            <label>Hora
                <select id="bookingTime" required>
                    <option value="">Selecciona una hora</option>
                    ${timeSlots.map(slot => `<option value="${slot.startTime}">${slot.label}</option>`).join("")}
                </select>
            </label>
            <button type="submit" class="btn-confirm-booking">Confirmar reserva</button>
            <p id="bookingMessage" role="alert"></p>
        </form>
    `;
    document.body.appendChild(overlay);

    const dateInput = overlay.querySelector("#bookingDate");
    const today = new Date();
    const localDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
    dateInput.min = localDate;

    const timeSelect = overlay.querySelector("#bookingTime");
    const refreshAvailableTimes = () => {
        const selectedDate = dateInput.value;
        const now = new Date();
        const isToday = selectedDate === localDate;
        const validTimes = timeSlots.filter((slot) => {
            if (!isToday) return true;
            const [hours, minutes] = slot.startTime.split(":").map(Number);
            return hours > now.getHours()
                || (hours === now.getHours() && minutes > now.getMinutes());
        });

        timeSelect.replaceChildren(new Option("Selecciona una hora", ""));
        validTimes.forEach(slot => timeSelect.add(new Option(slot.label, slot.startTime)));
        if (validTimes.length === 0 && isToday) {
            timeSelect.add(new Option("No quedan horarios hoy", ""));
        }
    };
    dateInput.addEventListener("change", refreshAvailableTimes);
    refreshAvailableTimes();

    const close = () => overlay.remove();
    overlay.querySelector(".close-booking").addEventListener("click", close);
    overlay.querySelector("#bookingForm").addEventListener("submit", async (submitEvent) => {
        submitEvent.preventDefault();
        const date = dateInput.value;
        const time = timeSelect.value;
        const message = overlay.querySelector("#bookingMessage");
        if (!doctor?.id || !patient?.id || !time || !localStorage.getItem("token")) {
            message.textContent = "Faltan datos de sesión, doctor u horario.";
            message.className = "booking-error";
            return;
        }

        const result = await bookAppointment(doctor.id, patient.id, `${date}T${time}:00`, localStorage.getItem("token"));
        message.textContent = result.message;
        if (result.success) {
            message.className = "booking-success";
            setTimeout(close, 900);
        } else {
            message.className = "booking-error";
        }
    });
}

window.openModal = openModal;
window.closeModal = closeModal;
