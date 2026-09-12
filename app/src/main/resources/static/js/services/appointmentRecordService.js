import { API_BASE_URL } from "../config/config.js";

const APPOINTMENT_API = `${API_BASE_URL}/doctor/appointments`;

export async function getAllAppointments(date, patientName, token) {
    try {
        const dateValue = encodeURIComponent(date || new Date().toISOString().split("T")[0]);
        const nameValue = encodeURIComponent(patientName && patientName.trim() !== "" ? patientName.trim() : "null");
        const tokenValue = encodeURIComponent(token || "");
        const response = await fetch(`${APPOINTMENT_API}/${dateValue}/${nameValue}/${tokenValue}`, {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });

        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(data.message || `Error al obtener citas: ${response.status}`);
        }

        if (data.message && !data.appointments) {
            throw new Error(data.message);
        }

        return Array.isArray(data) ? data : (data.appointments || []);
    } catch (error) {
        console.error("Error al obtener las citas del doctor:", error);
        throw error;
    }
}
