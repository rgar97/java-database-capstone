
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

export function showBookingOverlay() {
    alert("La reserva de citas todavía no está disponible en el backend.");
}

window.openModal = openModal;
window.closeModal = closeModal;
