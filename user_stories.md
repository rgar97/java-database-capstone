# Historias de Usuario de Administrador

---

### Title:
_Como Administrador, quiero iniciar sesión en el portal con mi nombre de usuario y contraseña, para gestionar la plataforma de manera segura._

**Acceptance Criteria:**
1. El sistema solicita un nombre de usuario y contraseña válidos en la pantalla de acceso.
2. Si las credenciales son correctas, el administrador es redirigido al panel de control principal.
3. Si las credenciales son incorrectas, el sistema muestra un mensaje de error claro y bloquea el acceso.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Las contraseñas deben enviarse de forma cifrada (HTTPS) y no almacenarse en texto plano.

---

### Title:
_Como Administrador, quiero cerrar sesión en el portal, para proteger el acceso no autorizado al sistema._

**Acceptance Criteria:**
1. Existe un botón visible de "Cerrar sesión" en el menú principal del portal.
2. Al hacer clic en el botón, la sesión activa se invalida en el servidor y se redirige a la pantalla de inicio de sesión.
3. Si se presiona el botón de "Atrás" en el navegador tras cerrar sesión, el sistema no permite ver las páginas protegidas.

**Priority:** Medium  
**Story Points:** 2  
**Notes:**
- Cierre automático de sesión por inactividad tras 15 minutos sin interacción del usuario.

---

### Title:
_Como Administrador, quiero agregar doctores al portal, para mantener actualizada la oferta médica disponible para los pacientes._

**Acceptance Criteria:**
1. El administrador dispone de un formulario con los campos: nombre, apellido, especialidad, correo y número de colegiado.
2. El sistema valida que el correo electrónico no esté duplicado en la base de datos.
3. Al completar el registro, se crean las credenciales iniciales del doctor y se le envía una notificación.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- El correo con las credenciales temporales debe caducar tras 24 horas si no es activado.

---

### Title:
_Como Administrador, quiero eliminar el perfil de un doctor del portal, para retirar la oferta de atención médica cuando un profesional ya no esté disponible._

**Acceptance Criteria:**
1. El administrador puede buscar y seleccionar el perfil de un doctor para solicitar su eliminación.
2. El sistema muestra una confirmación previa indicando la acción irreversible antes de proceder.
3. El perfil del doctor cambia a estado inactivado/eliminado y deja de ser visible para los pacientes.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Caso borde: Si el doctor tiene citas pendientes programadas, el sistema debe alertar al administrador para reasignarlas antes de darlo de baja.

---

### Title:
_Como Administrador, quiero ejecutar un procedimiento almacenado en MySQL CLI para obtener el número de citas por mes, para rastrear las estadísticas de uso del sistema._

**Acceptance Criteria:**
1. El procedimiento almacenado está creado y optimizado en la base de datos MySQL.
2. Al ejecutar la sentencia desde MySQL CLI, la consulta devuelve la cantidad total de citas agrupadas por año y mes.
3. La consulta se ejecuta de forma eficiente en entornos con un alto número de registros históricos.

**Priority:** Medium  
**Story Points:** 3  
**Notes:**

# Historias de Usuario de Paciente

---

### Title:
_Como Paciente, quiero ver una lista de doctores sin iniciar sesión, para explorar opciones antes de registrarme._

**Acceptance Criteria:**
1. Un visitante no autenticado puede acceder a la sección pública de doctores.
2. La lista muestra información básica del doctor (nombre, especialidad, foto y descripción general).
3. Los botones o enlaces para reservar cita redirigen a la pantalla de inicio de sesión o registro.

**Priority:** Medium  
**Story Points:** 3  
**Notes:**
- La lista debe incluir paginación o desplazamiento infinito para no ralentizar la carga pública.

---

### Title:
_Como Paciente, quiero registrarme usando mi correo electrónico y contraseña, para poder reservar citas en el portal._

**Acceptance Criteria:**
1. El usuario tiene acceso a un formulario de registro con campos de correo electrónico, contraseña y confirmación de contraseña.
2. El sistema valida que el formato del correo sea correcto y que la contraseña cumpla con los requisitos mínimos de seguridad.
3. Tras un registro exitoso, el paciente recibe un correo electrónico de confirmación o bienvenida.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- El correo electrónico debe ser único en la base de datos para evitar cuentas duplicadas.

---

### Title:
_Como Paciente, quiero iniciar sesión en el portal, para gestionar mis reservas de forma segura._

**Acceptance Criteria:**
1. El paciente puede ingresar con su correo electrónico y contraseña registrados.
2. Si las credenciales son válidas, el paciente accede a su panel de control personal.
3. Si la contraseña o el correo son incorrectos, se muestra un mensaje de error amigable.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Debe existir una opción de "Olvidé mi contraseña" en la pantalla de inicio de sesión.

---

### Title:
_Como Paciente, quiero cerrar sesión en el portal, para asegurar mi cuenta y proteger mis datos personales._

**Acceptance Criteria:**
1. El botón de "Cerrar sesión" es claramente accesible dentro del perfil del paciente.
2. Al pulsar el botón, finaliza la sesión activa del navegador de forma segura.
3. Al navegar hacia atrás en el historial del navegador, no se permite el acceso a pantallas privadas de la cuenta.

**Priority:** Medium  
**Story Points:** 2  
**Notes:**
- Cierre automático de sesión tras un periodo prolongado de inactividad por razones de privacidad médica.

---

### Title:
_Como Paciente, quiero iniciar sesión y reservar una cita de una hora para consultar con un doctor, para programar mi atención médica._

**Acceptance Criteria:**
1. El paciente autenticado puede seleccionar un doctor y ver sus horarios disponibles en bloques de 1 hora.
2. El paciente confirma la selección y el sistema reserva la cita asignándola a su cuenta.
3. Se genera una notificación y confirmación de la cita reservada para el paciente y el doctor.

**Priority:** High  
**Story Points:** 8  
**Notes:**
- Debe bloquearse el horario seleccionado durante el proceso de confirmación para evitar que otro paciente reserve la misma hora.

---

### Title:
_Como Paciente, quiero ver mis próximas citas, para poder prepararme adecuadamente antes de cada consulta._

**Acceptance Criteria:**
1. El panel de usuario muestra una sección destacada con la lista de citas futuras ordenadas por fecha y hora.
2. Cada cita muestra el nombre del doctor, la especialidad, la fecha, la hora y el estado de la reserva.
3. Si el paciente no tiene citas agendadas, se muestra un mensaje indicativo con un acceso directo para buscar doctores.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Debe permitirse descargar o añadir la cita al calendario personal (Google Calendar / iCal).


# Historias de Usuario de Doctor

---

### Title:
_Como Doctor, quiero iniciar sesión en el portal, para gestionar mis citas de manera segura._

**Acceptance Criteria:**
1. El doctor puede ingresar con su correo electrónico institucional y contraseña registrada.
2. Tras la autenticación correcta, el sistema redirige al doctor a su panel de gestión médica.
3. Si la cuenta del doctor está desactivada, el sistema impide el acceso y muestra un mensaje de contacto con administración.

**Priority:** High  
**Story Points:** 3  
**Notes:**
- Requerir autenticación de doble factor (2FA) como medida adicional de seguridad para datos médicos.

---

### Title:
_Como Doctor, quiero cerrar sesión en el portal, para proteger mis datos y la privacidad de mis pacientes._

**Acceptance Criteria:**
1. El botón de "Cerrar sesión" se encuentra visible en la barra de navegación del panel médico.
2. Al pulsar el botón, la sesión actual se destruye completamente en el servidor.
3. El sistema redirige automáticamente a la pantalla de inicio de sesión pública.

**Priority:** Medium  
**Story Points:** 2  
**Notes:**
- Bloqueo de sesión tras 10 minutos de inactividad para cumplir con normativas de protección de datos de salud.

---

### Title:
_Como Doctor, quiero ver mi calendario de citas, para mantenerme organizado durante mi jornada laboral._

**Acceptance Criteria:**
1. El portal ofrece una vista de calendario (diaria, semanal y mensual) con todas las citas programadas.
2. Cada bloque de cita muestra la hora, nombre del paciente y el tipo de consulta.
3. El doctor puede filtrar la vista del calendario por estado de la cita (confirmada, cancelada, completada).

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Permitir la sincronización del calendario del portal con Google Calendar o Outlook.

---

### Title:
_Como Doctor, quiero marcar mi indisponibilidad, para informar a los pacientes solo sobre mis horarios disponibles._

**Acceptance Criteria:**
1. El doctor puede seleccionar días u bloques de horas específicos dentro del calendario y marcarlos como "No disponible".
2. Los horarios marcados como indisponibles quedan automáticamente bloqueados para nuevas reservas de pacientes.
3. El sistema permite agregar una nota interna sobre el motivo del bloqueo (vacaciones, reunión, personal).

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Si se marca indisponibilidad sobre un horario con citas previas, el sistema debe solicitar la reprogramación o cancelación previa de dichas citas.

---

### Title:
_Como Doctor, quiero actualizar mi perfil con mi especialización e información de contacto, para que los pacientes tengan información actualizada._

**Acceptance Criteria:**
1. El doctor tiene un formulario editable con campos de especialidad, teléfono de contacto, biografía y horario de atención general.
2. Los cambios guardados se reflejan inmediatamente en el directorio público de doctores.
3. El sistema valida que los datos de contacto mantengan el formato correcto antes de guardar.

**Priority:** Medium  
**Story Points:** 3  
**Notes:**
- Los cambios en campos sensibles (como la especialidad) pueden requerir una revalidación por parte del administrador.

---

### Title:
_Como Doctor, quiero ver los detalles del paciente para las citas próximas, para poder estar preparado antes de la consulta._

**Acceptance Criteria:**
1. Al seleccionar una cita agendada, el doctor puede visualizar la ficha del paciente con su nombre, edad, teléfono y motivo de la consulta.
2. La información se presenta de forma clara en una ventana emergente o sección dedicada.
3. Solo el doctor asignado a la cita tiene acceso a visualizar los detalles personales y antecedentes ingresados por ese paciente.

**Priority:** High  
**Story Points:** 5  
**Notes:**
- Garantizar que los detalles del paciente solo sean accesibles dentro del marco de tiempo de la cita agendada para resguardar la privacidad.
