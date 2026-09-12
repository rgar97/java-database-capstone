export function createPatientRow(appointment) {
    const patient = appointment?.patient || {};
    const row = document.createElement("tr");
    const prescription = appointment?.prescription || "Pendiente";

    [
        patient.id ?? appointment?.patientId ?? "-",
        patient.name ?? appointment?.patientName ?? "-",
        patient.phone ?? appointment?.patientPhone ?? "-",
        patient.email ?? appointment?.patientEmail ?? "-",
        prescription
    ].forEach((value) => {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.appendChild(cell);
    });

    return row;
}
