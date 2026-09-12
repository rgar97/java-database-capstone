import { API_BASE_URL } from "../config/config.js";

// Endpoint base para las operaciones de pacientes
const PATIENT_API = API_BASE_URL + '/patient';
const APPOINTMENT_API = API_BASE_URL + '/appointment';

/**
 * Registra un nuevo paciente en la plataforma.
 * @param {Object} data - Objeto con datos del paciente (nombre, correo, contraseña, etc.).
 * @returns {Promise<{success: boolean, message: string}>} Resultado de la operación.
 */
export async function patientSignup(data) {
    try {
        // Paso 1: Enviar solicitud POST al endpoint de registro
        const response = await fetch(PATIENT_API, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const resData = await response.json().catch(() => ({}));

        // Paso 2: Evaluar la respuesta del servidor
        if (response.ok) {
            return {
                success: true,
                message: resData.message || "Registro completado con éxito."
            };
        } else {
            return {
                success: false,
                message: resData.message || "No se pudo completar el registro del paciente."
            };
        }
    } catch (error) {
        // Paso 3: Capturar fallos de red o errores inesperados
        console.error("Error en patientSignup:", error);
        return {
            success: false,
            message: "Error de red o falta de conexión con el servidor."
        };
    }
}

/**
 * Inicia sesión para un paciente existente.
 * @param {Object} data - Credenciales del paciente (email, password).
 * @returns {Promise<Response>} Respuesta HTTP completa para ser procesada en el flujo de autenticación.
 */
export async function patientLogin(data) {
    try {
        // Paso 1: Enviar credenciales en una solicitud POST
        const response = await fetch(`${PATIENT_API}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        // Retorna la respuesta HTTP nativa para acceso directo a estados, headers o token
        return response;
    } catch (error) {
        console.error("Error en patientLogin:", error);
        throw error;
    }
}

/**
 * Obtiene el perfil/datos del paciente actualmente autenticado.
 * @param {string} token - Token JWT/Sesión del usuario.
 * @returns {Promise<Object|null>} Objeto de datos del paciente o null si falla.
 */
export async function getPatientData(token) {
    try {
        // Paso 1: Realizar petición GET enviando el token en el encabezado
        const response = await fetch(`${PATIENT_API}/${encodeURIComponent(token)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        // Paso 2: Si la solicitud es exitosa, retornar los datos del paciente
        if (response.ok) {
            const patient = await response.json();
            return patient;
        } else {
            console.warn("No se pudieron obtener los datos del paciente. Estado:", response.status);
            return null;
        }
    } catch (error) {
        console.error("Error en getPatientData:", error);
        return null;
    }
}

/**
 * Obtiene las citas médicas del paciente de forma dinámica según el rol solicitante.
 * @param {string|number} id - Identificador único.
 * @param {string} token - Token de autenticación.
 * @param {string} user - Tipo de usuario que realiza la petición ("patient" o "doctor").
 * @returns {Promise<Array|null>} Lista de citas o null en caso de fallo.
 */
export async function getPatientAppointments(id, token, user) {
    try {
        // Paso 1: Construir URL de la API dinámicamente según el rol
        if (user !== "patient") {
            console.warn(`El backend no expone citas para el rol ${user}.`);
            return null;
        }

        const endpointUrl = `${PATIENT_API}/${encodeURIComponent(id)}/${encodeURIComponent(token)}`;

        // Paso 2: Consultar las citas asociadas
        const response = await fetch(endpointUrl, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const appointments = await response.json();
            return appointments;
        } else {
            console.error(`Error al recuperar citas para ${user}. Estado:`, response.status);
            return null;
        }
    } catch (error) {
        console.error("Error en getPatientAppointments:", error);
        return null;
    }
}

/**
 * Filtra las citas en tiempo real por condición (ej. "pending", "consulted") y nombre.
 * @param {string} condition - Estado o filtro de la cita.
 * @param {string} name - Filtro por nombre.
 * @param {string} token - Token de autenticación.
 * @returns {Promise<Array>} Lista de citas filtradas o lista vacía.
 */
export async function filterAppointments(condition, name, token) {
    try {
        // Paso 1: Construir parámetros de búsqueda
        const pathValue = (value) => encodeURIComponent(value && value.trim() !== '' ? value.trim() : 'null');
        const url = `${PATIENT_API}/filter/${pathValue(condition)}/${pathValue(name)}/${encodeURIComponent(token)}`;

        // Paso 2: Realizar la petición filtrada
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const filteredAppointments = await response.json();
            return Array.isArray(filteredAppointments) ? filteredAppointments : [];
        } else {
            console.warn("Respuesta no exitosa al filtrar citas:", response.status);
            return [];
        }
    } catch (error) {
        console.error("Error inesperado en filterAppointments:", error);
        alert("Ocurrió un error de red al intentar filtrar las citas.");
        return [];
    }
}

export async function bookAppointment(doctorId, patientId, appointmentTime, token) {
    try {
        const response = await fetch(`${APPOINTMENT_API}/${encodeURIComponent(token)}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                doctor: { id: doctorId },
                patient: { id: patientId },
                appointmentTime,
                status: 0
            })
        });
        const data = await response.json().catch(() => ({}));
        return {
            success: response.ok,
            message: data.message || (response.ok
                ? "Cita reservada correctamente."
                : "No se pudo reservar la cita.")
        };
    } catch (error) {
        console.error("Error al reservar la cita:", error);
        return { success: false, message: "No se pudo conectar con el servidor." };
    }
}