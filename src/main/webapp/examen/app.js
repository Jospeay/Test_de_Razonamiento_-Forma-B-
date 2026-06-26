/**
 * app.js — Test de Razonamiento Forma B
 * Universidad Americana (UAM) / Facultad de Ciencias Médicas
 *
 * Toda la data (preguntas, opciones, respuestas correctas) se carga
 * dinámicamente desde el backend OpenXava via API REST.
 * NO hay contenido hardcoded en este archivo.
 */

'use strict';

// ─── Estado global ─────────────────────────────────────────────────
const state = {
    questions:       [],   // Array de preguntas cargadas desde el backend
    answers:         {},   // { numeroPregunta: letraSeleccionada }
    currentIndex:    0,
    sessionId:       null,
    aspiranteId:     null,
    timeRemaining:   720,  // segundos — se sincroniza con el backend
    isSubmitting:    false,
    timerInterval:   null,
    cheatCountdown:  15,
    cheatInterval:   null,
    cheatVisible:    false,
};

// ─── Construcción de la URL base de la API ────────────────────────
// Funciona tanto desde /Test_Razonamiento_B/examen/ (con servidor)
// como desde file:// (modo preview estático).
function getApiBase() {
    const { protocol, host, pathname } = window.location;
    if (protocol === 'file:') {
        // Modo desarrollo local sin servidor — fallback demo
        return null;
    }
    // pathname: /Test_Razonamiento_B/examen/index.html
    // subir dos niveles: /Test_Razonamiento_B/api
    const parts = pathname.split('/').filter(Boolean);
    const ctx = parts[0]; // "Test_Razonamiento_B"
    return `${protocol}//${host}/${ctx}/api`;
}

const API = getApiBase();

// ─── Elementos del DOM ────────────────────────────────────────────
const $ = id => document.getElementById(id);

const screens = {
    instructions: $('screen-instructions'),
    register:     $('screen-register'),
    exam:         $('screen-exam'),
    finished:     $('screen-finished'),
};

// ─── Inicialización ───────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
    loadDepartamentos();
    setupAntiCheat();
    setupListeners();
});

// ─── Event Listeners ──────────────────────────────────────────────
function setupListeners() {
    $('btn-to-register').addEventListener('click', () => showScreen('register'));

    $('form-registration').addEventListener('submit', e => {
        e.preventDefault();
        handleRegistration();
    });

    $('btn-prev-question').addEventListener('click', () => {
        if (state.currentIndex > 0) showQuestion(state.currentIndex - 1);
    });

    $('btn-next-question').addEventListener('click', () => {
        if (state.currentIndex < state.questions.length - 1)
            showQuestion(state.currentIndex + 1);
    });

    $('btn-clear-answer').addEventListener('click', clearCurrentAnswer);

    $('btn-submit-exam').addEventListener('click', () => {
        const unanswered = state.questions.length - Object.keys(state.answers).length;
        let msg = '¿Está seguro de que desea finalizar el examen?';
        if (unanswered > 0) {
            msg = `Tiene ${unanswered} pregunta(s) sin responder. ¿Desea enviar el examen de todos modos?`;
        }
        if (confirm(msg)) submitExam();
    });
}

// ─── Cambio de pantalla ───────────────────────────────────────────
function showScreen(name) {
    Object.values(screens).forEach(s => s.classList.remove('active'));
    screens[name].classList.add('active');
    window.scrollTo(0, 0);
}

// ─── Overlay de carga ─────────────────────────────────────────────
function showLoading(msg = 'Cargando...') {
    $('loading-message').textContent = msg;
    $('loading-overlay').style.display = 'flex';
}
function hideLoading() {
    $('loading-overlay').style.display = 'none';
}

// ─── 1. Cargar Departamentos desde el Backend ─────────────────────
async function loadDepartamentos() {
    const sel = $('reg-depto');
    try {
        if (!API) {
            // Fallback estático si no hay servidor (modo preview)
            const deptos = ['MANAGUA','GRANADA','MASAYA','CARAZO','RIVAS',
                            'LEON','CHINANDEGA','MATAGALPA','JINOTEGA','ESTELÍ',
                            'MADRIZ','NUEVA_SEGOVIA','BOACO','CHONTALES',
                            'ZELAYA_NORTE','ZELAYA_SUR','RIO_SAN_JUAN'];
            sel.innerHTML = '<option value="">Seleccione departamento...</option>';
            deptos.forEach(d => {
                const opt = document.createElement('option');
                opt.value = d;
                opt.textContent = d.replace(/_/g, ' ');
                sel.appendChild(opt);
            });
            return;
        }

        const res = await fetch(`${API}/location/data`);
        if (!res.ok) throw new Error('Error al cargar departamentos');
        const data = await res.json();

        sel.innerHTML = '<option value="">Seleccione departamento...</option>';
        data.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = d.nombre;
            sel.appendChild(opt);
        });
    } catch (err) {
        console.error('[API] Departamentos:', err);
        sel.innerHTML = '<option value="">Error al cargar — refresque la página</option>';
    }
}

// ─── 2. Registro / Login del Aspirante ───────────────────────────
async function handleRegistration() {
    const errorEl = $('register-error');
    const submitBtn = $('btn-start-exam-trigger');
    errorEl.textContent = '';
    submitBtn.disabled = true;
    submitBtn.querySelector('span').textContent = 'Procesando...';

    const cedula       = $('reg-cedula').value.trim();
    const nombre       = $('reg-nombre').value.trim();
    const edad         = $('reg-edad').value.trim();
    const sexo         = $('reg-sexo').value;
    const deptoId      = $('reg-depto').value;
    const municipio    = $('reg-muni').value.trim();
    const zona         = $('reg-zona').value;
    const tipoColegio  = $('reg-colegio').value;
    const correo       = $('reg-correo').value.trim();
    const telefono     = $('reg-telefono').value.trim();

    // Validación básica en cliente
    if (!cedula || !nombre || !edad || !sexo || !deptoId || !municipio || !zona || !tipoColegio) {
        errorEl.textContent = '⚠ Por favor complete todos los campos obligatorios (marcados con *).';
        submitBtn.disabled = false;
        submitBtn.querySelector('span').textContent = 'Comenzar Evaluación';
        return;
    }

    if (!API) {
        // Modo demo sin servidor
        state.aspiranteId = 1;
        await startOrResumeExam();
        return;
    }

    const params = new URLSearchParams({
        cedula, nombreCompleto: nombre, edad, sexo,
        departamentoId: deptoId, municipioId: municipio,
        zona, tipoColegio, correo, telefono,
    });

    try {
        const res = await fetch(`${API}/auth/register-or-login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params,
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Error al procesar el registro.');
        state.aspiranteId = data.id;
        await startOrResumeExam();
    } catch (err) {
        errorEl.textContent = `⚠ ${err.message}`;
        submitBtn.disabled = false;
        submitBtn.querySelector('span').textContent = 'Comenzar Evaluación';
    }
}

// ─── 3. Iniciar o Reanudar Sesión ─────────────────────────────────
async function startOrResumeExam() {
    showLoading('Iniciando sesión de evaluación...');

    if (!API) {
        // Modo demo: cargar preguntas directamente
        hideLoading();
        await loadQuestions();
        return;
    }

    try {
        const res = await fetch(`${API}/exam/start`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ aspiranteId: state.aspiranteId }),
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'No se pudo iniciar el examen.');

        state.sessionId    = data.sessionId;
        state.timeRemaining = data.tiempoRestante ?? 720;

        // Restaurar cheat-countdown si es la misma sesión
        const savedId = localStorage.getItem('examSessionId');
        if (savedId === String(state.sessionId)) {
            const saved = parseInt(localStorage.getItem('cheatCountdown'), 10);
            if (!isNaN(saved) && saved >= 0) state.cheatCountdown = saved;
        } else {
            state.cheatCountdown = 15;
            localStorage.setItem('examSessionId', state.sessionId);
            localStorage.setItem('cheatCountdown', '15');
        }

        // Restaurar respuestas previas si el aspirante reanuda
        if (Array.isArray(data.respuestasGuardadas)) {
            data.respuestasGuardadas.forEach(r => {
                if (r.opcionSeleccionada) {
                    state.answers[r.numeroPregunta] = r.opcionSeleccionada;
                }
            });
        }

        // Si el examen ya finalizó anteriormente, mostrar resultado
        if (data.estado === 'FINALIZADA' || data.estado === 'EXPIRADA') {
            hideLoading();
            showFinishedScreen(data);
            return;
        }

        await loadQuestions();
    } catch (err) {
        hideLoading();
        alert(`Error: ${err.message}\nVerifique que el servidor esté activo.`);
        $('btn-start-exam-trigger').disabled = false;
        $('btn-start-exam-trigger').querySelector('span').textContent = 'Comenzar Evaluación';
    }
}

// ─── 4. Cargar Preguntas desde el Backend ─────────────────────────
async function loadQuestions() {
    showLoading('Cargando banco de preguntas...');
    try {
        if (!API) {
            // Modo demo: mensaje instructivo
            hideLoading();
            alert('Servidor no detectado. Inicie la aplicación desde IntelliJ para cargar las preguntas.');
            return;
        }

        const res = await fetch(`${API}/exam/questions`);
        if (!res.ok) throw new Error('No se pudieron cargar las preguntas del servidor.');
        state.questions = await res.json();

        if (state.questions.length === 0) {
            throw new Error('El banco de preguntas está vacío. Verifique que la base de datos esté inicializada.');
        }

        hideLoading();
        buildNavigatorGrid();
        showQuestion(0);
        startTimer();
        showScreen('exam');
    } catch (err) {
        hideLoading();
        alert(`Error al cargar el examen:\n${err.message}`);
    }
}

// ─── 5. Cuadrícula de navegación rápida ──────────────────────────
function buildNavigatorGrid() {
    const grid = $('questions-grid-container');
    grid.innerHTML = '';
    state.questions.forEach((q, idx) => {
        const btn = document.createElement('button');
        btn.className = 'nav-grid-btn';
        btn.id = `nav-btn-${q.numero}`;
        btn.textContent = idx + 1;
        btn.title = `Pregunta ${idx + 1}`;
        if (state.answers[q.numero]) btn.classList.add('answered');
        btn.addEventListener('click', () => showQuestion(idx));
        grid.appendChild(btn);
    });
    updateAnsweredCount();
}

// ─── 6. Mostrar pregunta ─────────────────────────────────────────
function showQuestion(index) {
    if (index < 0 || index >= state.questions.length) return;
    state.currentIndex = index;
    const q = state.questions[index];

    // Meta badges
    $('question-index-tag').textContent   = `Pregunta #${index + 1}`;
    $('question-category-tag').textContent = formatCategory(q.tipoPregunta);

    const diffBadge = $('question-difficulty-tag');
    diffBadge.textContent = formatDifficulty(q.nivel);
    diffBadge.className   = `q-badge q-difficulty ${q.nivel}`;

    // Enunciado
    $('question-text').textContent = q.enunciado;

    // Opciones — generadas desde el backend (sin hardcoding)
    const optList = $('options-group');
    optList.innerHTML = '';
    q.opciones.forEach(op => {
        const div = document.createElement('div');
        div.className = 'option-btn';
        div.setAttribute('role', 'button');
        div.setAttribute('tabindex', '0');
        if (state.answers[q.numero] === op.letra) div.classList.add('selected');

        div.innerHTML = `
            <span class="option-letter">${op.letra}</span>
            <span class="option-text">${escapeHtml(op.texto)}</span>
        `;
        div.addEventListener('click',   () => selectAnswer(q.numero, op.letra));
        div.addEventListener('keypress', e => { if (e.key === 'Enter') selectAnswer(q.numero, op.letra); });
        optList.appendChild(div);
    });

    // Botones de navegación
    $('btn-prev-question').disabled = (index === 0);
    $('btn-next-question').disabled = (index === state.questions.length - 1);
    $('btn-clear-answer').style.display = state.answers[q.numero] ? 'inline-flex' : 'none';

    // Progreso
    $('progress-indicator').textContent = `${index + 1} / ${state.questions.length}`;
    $('progress-bar-fill-el').style.width = `${((index + 1) / state.questions.length) * 100}%`;

    // Resaltar en la cuadrícula
    document.querySelectorAll('.nav-grid-btn').forEach(b => b.classList.remove('current'));
    const navBtn = $(`nav-btn-${q.numero}`);
    if (navBtn) navBtn.classList.add('current');

    // Animación de entrada de la tarjeta
    const card = $('question-card');
    card.style.animation = 'none';
    void card.offsetWidth; // reflow
    card.style.animation = 'fadeInUp 200ms ease both';
}

// ─── 7. Seleccionar respuesta (autoguardado) ──────────────────────
async function selectAnswer(questionNum, letra) {
    state.answers[questionNum] = letra;
    showQuestion(state.currentIndex); // refresca UI

    const navBtn = $(`nav-btn-${questionNum}`);
    if (navBtn) navBtn.classList.add('answered');
    updateAnsweredCount();

    if (API && state.sessionId) {
        await saveAnswerToServer(questionNum, letra);
    }
}

// ─── 8. Limpiar respuesta actual ─────────────────────────────────
async function clearCurrentAnswer() {
    const q = state.questions[state.currentIndex];
    if (!state.answers[q.numero]) return;

    delete state.answers[q.numero];
    showQuestion(state.currentIndex);

    const navBtn = $(`nav-btn-${q.numero}`);
    if (navBtn) navBtn.classList.remove('answered');
    updateAnsweredCount();

    if (API && state.sessionId) {
        await saveAnswerToServer(q.numero, 'null');
    }
}

// ─── 9. Autoguardado en servidor ─────────────────────────────────
async function saveAnswerToServer(questionNum, selectedOption) {
    try {
        const res = await fetch(`${API}/exam/save-answer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({
                sessionId: state.sessionId,
                questionNum,
                selectedOption,
            }),
        });
        const data = await res.json();
        if (!res.ok) {
            // El servidor puede haber expirado la sesión
            console.warn('[API] save-answer:', data.error);
            if (data.error && data.error.includes('expir')) {
                clearInterval(state.timerInterval);
                alert('Su sesión ha expirado. El examen se enviará automáticamente.');
                submitExam();
            }
        } else if (data.tiempoRestante !== undefined) {
            state.timeRemaining = data.tiempoRestante;
            renderTimer();
        }
    } catch (err) {
        // Toleramos fallos de red — se reintentará en la próxima interacción
        console.warn('[API] Error de red en autoguardado:', err.message);
    }
}

// ─── 10. Temporizador ────────────────────────────────────────────
function startTimer() {
    if (state.timerInterval) clearInterval(state.timerInterval);
    renderTimer();
    state.timerInterval = setInterval(() => {
        if (state.timeRemaining > 0) {
            state.timeRemaining--;
            renderTimer();
            if (state.timeRemaining <= 120) {
                $('timer-box').classList.add('danger');
            }
        } else {
            clearInterval(state.timerInterval);
            alert('⏱ ¡El tiempo de 12 minutos ha expirado! Sus respuestas serán enviadas automáticamente.');
            submitExam();
        }
    }, 1000);
}

function renderTimer() {
    const m = Math.floor(state.timeRemaining / 60);
    const s = state.timeRemaining % 60;
    $('timer-display').textContent =
        `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
}

// ─── 11. Finalizar examen ─────────────────────────────────────────
async function submitExam() {
    if (state.isSubmitting) return;
    state.isSubmitting = true;
    clearInterval(state.timerInterval);
    hideCheatWarning();
    window.onbeforeunload = null;
    localStorage.removeItem('examSessionId');
    localStorage.removeItem('cheatCountdown');

    showLoading('Enviando resultados...');

    if (!API || !state.sessionId) {
        // Modo demo sin servidor
        hideLoading();
        showFinishedScreen({
            puntaje: Object.keys(state.answers).length,
            respuestasCorrectas: 0,
            percentil: '—',
            diagnostico: 'Demo — inicie el servidor para calificar.',
        });
        return;
    }

    try {
        const res = await fetch(`${API}/exam/submit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ sessionId: state.sessionId }),
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Error al enviar el examen.');
        hideLoading();
        showFinishedScreen(data);
    } catch (err) {
        hideLoading();
        state.isSubmitting = false;
        alert(`Error al enviar: ${err.message}`);
        startTimer(); // reanudar si fallo manual
    }
}

// ─── 12. Pantalla de resultado ────────────────────────────────────
function showFinishedScreen(resultado) {
    showScreen('finished');
    const box = $('result-summary-box');

    if (resultado.respuestasCorrectas !== undefined || resultado.puntaje !== undefined) {
        const correctas = resultado.respuestasCorrectas ?? resultado.puntaje ?? 0;
        const total     = state.questions.length || 30;

        $('res-correct').textContent   = correctas;
        $('res-blank').textContent     = total - correctas;
        $('res-percentil').textContent = resultado.percentil ?? '—';
        $('res-diagnostico').textContent = resultado.diagnostico ?? 'Resultado disponible en el panel administrativo.';
        box.style.display = 'block';
    }
}

// ─── 13. Contador de preguntas respondidas ────────────────────────
function updateAnsweredCount() {
    const total     = state.questions.length;
    const answered  = Object.keys(state.answers).length;
    const el = $('answered-count-display');
    if (el) el.textContent = `${answered} de ${total} respondidas`;
}

// ─── 14. Anti-trampa ─────────────────────────────────────────────
function setupAntiCheat() {
    // Deshabilitar clic derecho e inspección durante el examen
    document.addEventListener('contextmenu', e => {
        if (screens.exam.classList.contains('active')) e.preventDefault();
    });
    document.addEventListener('copy', e => {
        if (screens.exam.classList.contains('active')) e.preventDefault();
    });
    document.addEventListener('paste', e => {
        if (screens.exam.classList.contains('active')) e.preventDefault();
    });

    // Avisar si intenta salir de la página
    window.onbeforeunload = e => {
        if (screens.exam.classList.contains('active') && !state.isSubmitting) {
            e.preventDefault();
            return 'Tiene un examen en curso. El temporizador seguirá corriendo.';
        }
    };

    // Detectar cambio de pestaña o pérdida de foco
    document.addEventListener('visibilitychange', () => {
        if (!screens.exam.classList.contains('active') || state.isSubmitting) return;
        if (document.hidden) showCheatWarning();
        else hideCheatWarning();
    });
    window.addEventListener('blur', () => {
        if (!screens.exam.classList.contains('active') || state.isSubmitting) return;
        if (!document.hidden) showCheatWarning();
    });
    window.addEventListener('focus', () => {
        if (!screens.exam.classList.contains('active') || state.isSubmitting) return;
        hideCheatWarning();
    });
}

function showCheatWarning() {
    if (state.cheatVisible) return;
    state.cheatVisible = true;

    const modal = $('cheat-warning-modal');
    const countEl = $('cheat-countdown-val');
    modal.style.display = 'flex';
    countEl.textContent = Math.ceil(state.cheatCountdown);

    if (state.cheatInterval) clearInterval(state.cheatInterval);
    state.cheatInterval = setInterval(() => {
        state.cheatCountdown--;
        localStorage.setItem('cheatCountdown', state.cheatCountdown);
        if (countEl) countEl.textContent = Math.max(0, Math.ceil(state.cheatCountdown));
        if (state.cheatCountdown <= 0) {
            clearInterval(state.cheatInterval);
            submitExam();
        }
    }, 1000);
}

function hideCheatWarning() {
    if (!state.cheatVisible) return;
    state.cheatVisible = false;
    $('cheat-warning-modal').style.display = 'none';
    if (state.cheatInterval) {
        clearInterval(state.cheatInterval);
        state.cheatInterval = null;
    }
    localStorage.setItem('cheatCountdown', state.cheatCountdown);
}

// ─── Utilidades ───────────────────────────────────────────────────
function formatCategory(cat) {
    if (!cat) return '—';
    const map = {
        ANALOGIA:          'Analogía Verbal',
        SERIES_ALFABETICAS:'Series Alfabéticas',
        SERIES_NUMERICAS:  'Series Numéricas',
        CODIFICACION:      'Codificación',
        LOGICA:            'Lógica',
        MATEMATICA:        'Matemática',
    };
    return map[cat] || cat.replace(/_/g, ' ');
}

function formatDifficulty(nivel) {
    const map = { FACIL: 'Fácil', INTERMEDIO: 'Intermedio', AVANZADO: 'Avanzado' };
    return map[nivel] || nivel;
}

function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}
