/**
 * app.js - Conexión a la API del ExamenServlet en OpenXava
 * Todos los endpoints apuntan a la API propia del backend.
 */

// URL base de la API. Puede ser relativa (si el frontend está en el mismo servidor)
// o absoluta (si el frontend está separado en otro puerto).
const API_BASE = 'http://localhost:8080/Test_Razonamiento_B/api';

// ==========================================
// GET: Obtener preguntas completas con opciones
// Ruta: GET /api/exam/questions
// ==========================================
async function obtenerPreguntas() {
    try {
        const response = await fetch(`${API_BASE}/exam/questions`);
        if (!response.ok) {
            const err = await response.json();
            console.error('Error al obtener preguntas:', err.error);
            return null;
        }
        return await response.json(); // Array de { id, numero, tipoPregunta, nivel, enunciado, opciones: [{letra, texto, orden}] }
    } catch (e) {
        console.error('Error de red al obtener preguntas:', e);
        return null;
    }
}

// ==========================================
// POST: Registrar / Login de aspirante
// Ruta: POST /api/auth/register-or-login
// Formato: form-urlencoded (NO JSON body)
// ==========================================
async function guardarAspirante(datos) {
    const params = new URLSearchParams({
        cedula:          datos.cedula,
        nombreCompleto:  datos.nombreCompleto,
        edad:            datos.edad,
        sexo:            datos.sexo,            // MASCULINO | FEMENINO
        departamentoId:  datos.departamentoId,  // e.g. MANAGUA
        municipioId:     datos.municipio,
        zona:            datos.zona,            // URBANA | RURAL
        tipoColegio:     datos.tipoColegio,     // PUBLICO | PRIVADO
        correo:          datos.correo  || '',
        telefono:        datos.telefono || ''
    });

    try {
        const response = await fetch(`${API_BASE}/auth/register-or-login`, {
            method: 'POST',
            body: params   // Content-Type: application/x-www-form-urlencoded
        });

        if (!response.ok) {
            const err = await response.json();
            return { exito: false, mensaje: err.error || `Error del servidor: ${response.status}` };
        }

        const result = await response.json(); // { id, codigo, nombreCompleto, cedula }
        localStorage.setItem('aspiranteActual', JSON.stringify(result));
        return { exito: true, data: result };

    } catch (e) {
        return { exito: false, mensaje: `Error de red (CORS o servidor inactivo): ${e.message}` };
    }
}

// ==========================================
// POST: Iniciar sesión de examen
// Ruta: POST /api/exam/start
// Devuelve: { sessionId, estado, tiempoRestante, respuestasGuardadas }
// ==========================================
async function iniciarExamen(aspiranteId) {
    const params = new URLSearchParams({ aspiranteId });
    try {
        const response = await fetch(`${API_BASE}/exam/start`, {
            method: 'POST',
            body: params
        });
        if (!response.ok) {
            const err = await response.json();
            console.error('Error al iniciar examen:', err.error);
            return null;
        }
        return await response.json();
    } catch (e) {
        console.error('Error de red al iniciar examen:', e);
        return null;
    }
}

// ==========================================
// POST: Guardar respuesta en tiempo real
// Ruta: POST /api/exam/save-answer
// Params: sessionId, questionNum, selectedOption (letra: A|B|C|D)
// ==========================================
async function guardarRespuestaServidor(sessionId, questionNum, letra) {
    const params = new URLSearchParams({
        sessionId,
        questionNum,
        selectedOption: letra // A, B, C o D
    });
    try {
        const response = await fetch(`${API_BASE}/exam/save-answer`, {
            method: 'POST',
            body: params
        });
        return await response.json();
    } catch (e) {
        console.error('Error guardando respuesta:', e);
        return null;
    }
}

// ==========================================
// POST: Enviar examen completo
// Ruta: POST /api/exam/submit
// ==========================================
async function submitExamen(sessionId) {
    const params = new URLSearchParams({ sessionId });
    try {
        const response = await fetch(`${API_BASE}/exam/submit`, {
            method: 'POST',
            body: params
        });
        return await response.json();
    } catch (e) {
        console.error('Error enviando examen:', e);
        return null;
    }
}

// ==========================================
// Utilidades de LocalStorage
// ==========================================
function obtenerAspiranteActual() {
    const asp = localStorage.getItem('aspiranteActual');
    return asp ? JSON.parse(asp) : null;
}

function obtenerSesionActual() {
    const sesion = localStorage.getItem('sesionActual');
    return sesion ? JSON.parse(sesion) : null;
}

function guardarSesionLocal(data) {
    localStorage.setItem('sesionActual', JSON.stringify(data));
}

function limpiarSesion() {
    localStorage.removeItem('eval_inicio');
    localStorage.removeItem('sesionActual');
}
