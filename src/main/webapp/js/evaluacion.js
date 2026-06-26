/**
 * evaluacion.js - Lógica central de la pantalla de evaluación
 * Conectada al ExamenServlet: inicia sesión, carga preguntas, guarda respuestas.
 */

let timer;
let controlPestana;
let pm;
let indicePreguntaActual = 0;
let totalPreguntas = 0;
let sesionId = null;

// Respuestas locales: { numero: 'A' | 'B' | 'C' | 'D' }
let respuestasLocales = {};

document.addEventListener('DOMContentLoaded', async () => {
    if (!document.getElementById('contenedorEvaluacion')) return;

    // 1. Verificar que hay un aspirante registrado
    const aspirante = obtenerAspiranteActual();
    if (!aspirante || !aspirante.id) {
        Swal.fire({
            icon: 'warning', title: 'Acceso incorrecto',
            text: 'Debe completar el registro antes de iniciar la evaluación.',
            confirmButtonColor: '#27496D'
        }).then(() => { window.location.href = '../index.html'; });
        return;
    }

    // 2. Iniciar (o recuperar) sesión de examen en el servidor
    const sesionData = await iniciarExamen(aspirante.id);
    if (!sesionData) {
        Swal.fire({ icon: 'error', title: 'Error', text: 'No se pudo iniciar la sesión de examen. Verifique que el servidor esté activo.', confirmButtonColor: '#dc3545' });
        return;
    }

    sesionId = sesionData.sessionId;
    guardarSesionLocal(sesionData);

    // Restaurar respuestas guardadas del servidor
    if (sesionData.respuestasGuardadas && Array.isArray(sesionData.respuestasGuardadas)) {
        sesionData.respuestasGuardadas.forEach(r => {
            if (r.opcionSeleccionada) {
                respuestasLocales[r.numeroPregunta] = r.opcionSeleccionada;
            }
        });
    }

    // 3. Cargar preguntas desde el servidor
    pm = new PreguntasManager();
    const cargado = await pm.cargarEstructura();
    if (!cargado || pm.obtenerTotalItems() === 0) {
        Swal.fire({ icon: 'error', title: 'Sin preguntas', text: 'No hay preguntas activas para esta evaluación. Contacte al administrador.', confirmButtonColor: '#dc3545' });
        return;
    }

    totalPreguntas = pm.obtenerTotalItems();

    renderizarMapaPreguntas();
    mostrarPregunta(0);

    // 4. Iniciar Temporizador (usa tiempo restante del servidor para precisión)
    const tiempoInicial = sesionData.tiempoRestante; // segundos
    const timerDisplay = document.getElementById('timerDisplay');
    const timerContainer = document.getElementById('timerContainer');

    // Guardamos el "inicio real" calculado hacia atrás desde tiempoRestante
    const duracionTotal = tiempoInicial * 1000;
    const tiempoInicioCalculado = Date.now() - (sesionData.tiempoRestante === sesionData.duracionTotal ? 0 : (sesionData.duracionTotal - tiempoInicial) * 1000);

    // Mostrar tiempo inicial
    timerDisplay.textContent = TemporizadorExacto.formato(duracionTotal);

    timer = new TemporizadorExacto(tiempoInicial / 60, (restanteMs) => {
        timerDisplay.textContent = TemporizadorExacto.formato(restanteMs);
        if (restanteMs <= 60000 && restanteMs > 0) {
            timerContainer.classList.add('timer-danger', 'pulse-danger');
            timerContainer.classList.remove('timer-warning');
        } else if (restanteMs <= 300000) {
            timerContainer.classList.add('timer-warning');
        }
    }, finalizarEvaluacion);

    timer.iniciar();

    // 5. Control de abandono de pestaña
    controlPestana = new ControlPestana(
        () => {
            document.getElementById('advertenciaAbandonoModal').classList.remove('d-none');
        },
        () => {
            document.getElementById('advertenciaAbandonoModal').classList.add('d-none');
        },
        () => { finalizarEvaluacionPorInfraccion(); }
    );

    // 6. Eventos de botones de navegación
    document.getElementById('btnAnterior').addEventListener('click', () => {
        if (indicePreguntaActual > 0) mostrarPregunta(indicePreguntaActual - 1);
    });
    document.getElementById('btnSiguiente').addEventListener('click', () => {
        if (indicePreguntaActual < totalPreguntas - 1) mostrarPregunta(indicePreguntaActual + 1);
    });
    document.getElementById('btnFinalizar').addEventListener('click', () => {
        Swal.fire({
            title: '¿Seguro que desea finalizar?',
            text: 'Esta acción no se puede revertir.',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#27496D',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Sí, finalizar',
            cancelButtonText: 'Seguir respondiendo'
        }).then((result) => {
            if (result.isConfirmed) finalizarEvaluacion();
        });
    });
});

// ==========================================
// Renderizar mapa lateral de preguntas
// ==========================================
function renderizarMapaPreguntas() {
    const grid = document.getElementById('navGrid');
    grid.innerHTML = '';
    for (let i = 0; i < totalPreguntas; i++) {
        const item = pm.obtenerItemEnIndiceGlobal(i);
        const btn = document.createElement('div');
        btn.className = 'q-nav-btn unanswered';
        btn.textContent = item.numero;
        btn.id = `navBtn-${i}`;
        if (respuestasLocales[item.numero]) btn.classList.replace('unanswered', 'answered');
        btn.addEventListener('click', () => mostrarPregunta(i));
        grid.appendChild(btn);
    }
}

function actualizarMapa(indiceActual) {
    for (let i = 0; i < totalPreguntas; i++) {
        const item = pm.obtenerItemEnIndiceGlobal(i);
        const btn = document.getElementById(`navBtn-${i}`);
        btn.className = 'q-nav-btn';
        if (i === indiceActual) {
            btn.classList.add('current');
        } else if (respuestasLocales[item.numero]) {
            btn.classList.add('answered');
        } else {
            btn.classList.add('unanswered');
        }
    }
}

// ==========================================
// Mostrar una pregunta por índice
// ==========================================
function mostrarPregunta(indice) {
    indicePreguntaActual = indice;
    const item = pm.obtenerItemEnIndiceGlobal(indice);
    const opciones = pm.obtenerOpcionesDeItem(item.id);
    const respuestaPreviaLetra = respuestasLocales[item.numero] || null;

    document.getElementById('preguntaActualText').textContent = `Pregunta ${indice + 1} de ${totalPreguntas}`;
    document.getElementById('enunciadoText').textContent = item.enunciado;
    document.getElementById('btnAnterior').disabled = (indice === 0);
    document.getElementById('btnSiguiente').disabled = (indice === totalPreguntas - 1);

    const contenedorOpciones = document.getElementById('opcionesContainer');
    contenedorOpciones.innerHTML = '';

    opciones.forEach(op => {
        const div = document.createElement('div');
        div.className = 'option-wrapper fade-in';

        const card = document.createElement('div');
        card.className = `option-card ${respuestaPreviaLetra === op.letra ? 'selected' : ''}`;

        const radio = document.createElement('input');
        radio.type = 'radio';
        radio.name = `pregunta_${item.id}`;
        radio.className = 'option-radio';
        radio.value = op.letra;
        if (respuestaPreviaLetra === op.letra) radio.checked = true;

        // Etiqueta de la letra
        const badge = document.createElement('span');
        badge.className = 'badge me-2';
        badge.style.cssText = 'background-color: var(--primary-color); color: white; border-radius: 4px; padding: 2px 8px;';
        badge.textContent = op.letra;

        const label = document.createElement('span');
        label.textContent = op.texto;

        card.appendChild(radio);
        card.appendChild(badge);
        card.appendChild(label);

        card.addEventListener('click', () => {
            document.querySelectorAll('.option-card').forEach(c => c.classList.remove('selected'));
            card.classList.add('selected');
            radio.checked = true;

            // Guardar localmente
            respuestasLocales[item.numero] = op.letra;

            // Guardar en el servidor (fire and forget)
            guardarRespuestaServidor(sesionId, item.numero, op.letra);
            actualizarMapa(indicePreguntaActual);
        });

        div.appendChild(card);
        contenedorOpciones.appendChild(div);
    });

    actualizarMapa(indiceActual);
}

// ==========================================
// Finalizar evaluación normalmente
// ==========================================
async function finalizarEvaluacion() {
    if (timer) timer.detener();

    Swal.fire({ title: 'Enviando respuestas...', allowOutsideClick: false, showConfirmButton: false, didOpen: () => { Swal.showLoading(); } });

    const resultado = await submitExamen(sesionId);
    limpiarSesion();

    Swal.close();
    window.location.href = 'finalizacion.html';
}

// Finalizar por abandono de pestaña
async function finalizarEvaluacionPorInfraccion() {
    if (timer) timer.detener();
    await submitExamen(sesionId);
    limpiarSesion();

    Swal.fire({
        title: 'Evaluación Anulada', text: 'Ha superado el límite de tiempo fuera de la plataforma.',
        icon: 'error', allowOutsideClick: false, confirmButtonColor: '#27496D'
    }).then(() => { window.location.href = 'finalizacion.html'; });
}
