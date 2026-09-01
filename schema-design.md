## MySQL Database Design

La base de datos MySQL gestiona los datos operativos estructurados y críticos donde la integridad referencial, la consistencia de las transacciones (ACID) y la validación de relaciones son fundamentales.

---

### Table: admin
Almacena el personal administrativo encargado del control de acceso y gestión del sistema.

- `id`: INT, Primary Key, Auto Increment, NOT NULL
- `username`: VARCHAR(50), Unique, NOT NULL
- `email`: VARCHAR(100), Unique, NOT NULL
- `password_hash`: VARCHAR(255), NOT NULL
- `created_at`: DATETIME, Default CURRENT_TIMESTAMP

---

### Table: patients
Almacena la información de perfil de los pacientes registrados.

- `id`: INT, Primary Key, Auto Increment, NOT NULL
- `first_name`: VARCHAR(50), NOT NULL
- `last_name`: VARCHAR(50), NOT NULL
- `email`: VARCHAR(100), Unique, NOT NULL
- `phone`: VARCHAR(20), NOT NULL
- `date_of_birth`: DATE, NOT NULL
- `created_at`: DATETIME, Default CURRENT_TIMESTAMP

---

### Table: doctors
Almacena la información de los profesionales médicos disponibles en la clínica.

- `id`: INT, Primary Key, Auto Increment, NOT NULL
- `first_name`: VARCHAR(50), NOT NULL
- `last_name`: VARCHAR(50), NOT NULL
- `specialty`: VARCHAR(100), NOT NULL
- `email`: VARCHAR(100), Unique, NOT NULL
- `phone`: VARCHAR(20), NOT NULL
- `created_at`: DATETIME, Default CURRENT_TIMESTAMP

---

### Table: doctor_availability
Define las franjas horarias configuradas por cada doctor para la atención de citas.

- `id`: INT, Primary Key, Auto Increment, NOT NULL
- `doctor_id`: INT, Foreign Key → doctors(id) ON DELETE CASCADE, NOT NULL
- `day_of_week`: TINYINT, NOT NULL (1 = Monday, 7 = Sunday)
- `start_time`: TIME, NOT NULL
- `end_time`: TIME, NOT NULL

---

### Table: appointments
Registra las reservas de citas realizadas entre pacientes y doctores.

- `id`: INT, Primary Key, Auto Increment, NOT NULL
- `doctor_id`: INT, Foreign Key → doctors(id) ON DELETE RESTRICT, NOT NULL
- `patient_id`: INT, Foreign Key → patients(id) ON DELETE CASCADE, NOT NULL
- `appointment_time`: DATETIME, NOT NULL
- `duration_minutes`: INT, Default 60, NOT NULL
- `status`: TINYINT, Default 0, NOT NULL (0 = Scheduled, 1 = Completed, 2 = Cancelled)
- `created_at`: DATETIME, Default CURRENT_TIMESTAMP

---

### Table: prescriptions
Registra las recetas generadas vinculadas a una consulta médica específica.

- `id`: INT, Primary Key, Auto Increment, NOT NULL
- `appointment_id`: INT, Foreign Key → appointments(id) ON DELETE CASCADE, NOT NULL
- `issued_date`: DATETIME, Default CURRENT_TIMESTAMP, NOT NULL
- `notes`: TEXT

---

### Justificación de Decisiones de Diseño

1. **Integridad Referencial y Borrados (ON DELETE):**
   - Si un **paciente** se elimina (`patients`), sus citas e historial de recetas se eliminan en cascada (`ON DELETE CASCADE`), ya que carecen de sentido sin el paciente.
   - Si se intenta eliminar un **doctor** (`doctors`) con citas históricas o asociadas, la restricción `ON DELETE RESTRICT` impide la eliminación directa para conservar el historial operativo y financiero de la clínica. En su lugar, el doctor se inactiva desde la lógica de la aplicación.

2. **Prevención de Citas Superpuestas:**
   - La combinación de `doctor_id` y `appointment_time` debe validarse a nivel de consulta/aplicación o mediante un índice compuesto único en caso de bloques de tiempo fijos, evitando que un médico atienda dos citas simultáneas.

3. **Disponibilidad de Doctores:**
   - La tabla `doctor_availability` independiza la agenda semanal del médico de la tabla de citas, permitiendo consultar únicamente bloques de tiempo hábiles antes de realizar una reserva.
  
   - # MongoDB Collection Design

Las bases de datos orientadas a documentos NoSQL como MongoDB ofrecen la flexibilidad necesaria para almacenar datos semiestructurados, de esquema evolutivo o con estructuras anidadas complejas que resultan ineficientes en tablas relacionales rígidas.

---

## Collection 1: medical_consultation_notes
Almacena notas clínicas detalladas de la consulta, diagnósticos estructurados/no estructurados, adjuntos médicos y el detalle extendido de medicamentos prescritos.

```json
{
  "_id": "64f1a2b3c4d5e6f7a8b9c0d1",
  "appointmentId": 1052,
  "patientId": 451,
  "doctorId": 88,
  "consultationDate": "2026-09-01T10:30:00Z",
  "vitalSigns": {
    "bloodPressure": "120/80 mmHg",
    "heartRateBpm": 72,
    "temperatureCelsius": 36.6,
    "weightKg": 70.5
  },
  "clinicalNotes": "El paciente presenta síntomas leves de migraña recurrente. No refiere otros malestares.",
  "diagnosis": {
    "code": "G43.9",
    "description": "Migraña, no especificada",
    "chronic": false
  },
  "prescriptions": [
    {
      "medication": "Ibuprofeno",
      "dosage": "600mg",
      "frequency": "Cada 8 horas",
      "durationDays": 5,
      "instructions": "Tomar junto con alimentos."
    }
  ],
  "attachments": [
    {
      "fileName": "analisis_sangre_sep.pdf",
      "fileType": "application/pdf",
      "fileUrl": "[https://storage.smartclinic.com/docs/analisis_sangre_sep.pdf](https://storage.smartclinic.com/docs/analisis_sangre_sep.pdf)",
      "uploadedAt": "2026-09-01T10:35:00Z"
    }
  ],
  "tags": ["migraña", "ambulatorio", "revisión"],
  "metadata": {
    "createdAt": "2026-09-01T10:40:00Z",
    "updatedAt": "2026-09-01T10:40:00Z",
    "version": 1
  }
}
