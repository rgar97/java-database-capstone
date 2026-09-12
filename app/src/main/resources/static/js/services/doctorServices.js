import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = `${API_BASE_URL}/doctor`;

/**
 * Obtiene la lista completa de doctores registrados.
 * @returns {Promise<Array>} Lista de doctores o arreglo vacío en caso de error.
 */
export async function getDoctors() {
    try {
        const response = await fetch(DOCTOR_API, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            throw new Error(`Error en la petición: ${response.status} ${response.statusText}`);
        }

        const data = await response.json();
        return Array.isArray(data) ? data : (data.doctors || []);
    } catch (error) {
        console.error("Error al obtener los médicos:", error);
        return [];
    }
}

/**
 * Elimina un médico por su ID mediante autenticación.
 * @param {string|number} id - ID único del doctor.
 * @param {string} token - Token JWT/Sesión del Administrador.
 * @returns {Promise<{success: boolean, message: string}>}
 */
export async function deleteDoctor(id, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const data = await response.json().catch(() => ({}));
            return {
                success: true,
                message: data.message || "Médico eliminado exitosamente."
            };
        } else {
            const errorData = await response.json().catch(() => ({}));
            return {
                success: false,
                message: errorData.message || "No se pudo eliminar al médico."
            };
        }
    } catch (error) {
        console.error(`Error al eliminar el médico con ID ${id}:`, error);
        return {
            success: false,
            message: "Error de red o conexión al servidor."
        };
    }
}

/**
 * Guarda (agrega) un nuevo médico en la base de datos.
 * @param {Object} doctor - Datos del médico a registrar.
 * @param {string} token - Token de autenticación del Admin.
 * @returns {Promise<{success: boolean, message: string, data?: Object}>}
 */
export async function saveDoctor(doctor, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${encodeURIComponent(token)}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor)
        });

        if (response.ok) {
            const data = await response.json();
            return {
                success: true,
                message: "Médico registrado exitosamente.",
                data
            };
        } else {
            const errorData = await response.json().catch(() => ({}));
            return {
                success: false,
                message: errorData.message || "Ocurrió un error al intentar guardar el médico."
            };
        }
    } catch (error) {
        console.error("Error al guardar el nuevo médico:", error);
        return {
            success: false,
            message: "Error de conexión con el servidor al registrar el médico."
        };
    }
}

/**
 * Filtra los médicos por nombre, tiempo/horario y especialidad.
 * @param {string|null} name - Nombre o término de búsqueda.
 * @param {string|null} time - Rango o filtro de horario (ej. AM, PM, asc, desc).
 * @param {string|null} specialty - Especialidad del médico.
 * @returns {Promise<Array>} Lista de médicos que coinciden con el filtro.
 */
export async function filterDoctors(name, time, specialty) {
    try {
        const pathValue = (value) => encodeURIComponent(value && value.trim() !== '' ? value.trim() : 'null');
        const url = `${DOCTOR_API}/filter/${pathValue(name)}/${pathValue(time)}/${pathValue(specialty)}`;

        const response = await fetch(url, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            throw new Error(`Error en el filtro: ${response.status}`);
        }

        const data = await response.json();
        return Array.isArray(data) ? data : (data.doctors || []);
    } catch (error) {
        console.error("Error al filtrar la lista de médicos:", error);
        return [];
    }
}