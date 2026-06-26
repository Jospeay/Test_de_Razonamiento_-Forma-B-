/**
 * modalAdvertencia.js - Detección de abandono de pestaña
 */

class ControlPestana {
    constructor(onAbandon, onReturn, onTimeout) {
        this.onAbandon = onAbandon;
        this.onReturn = onReturn;
        this.onTimeout = onTimeout;
        this.tiempoRestante = 10;
        this.interval = null;
        this.modalActivo = false;

        document.addEventListener('visibilitychange', () => this.handleVisibilityChange());
    }

    handleVisibilityChange() {
        // No hacer nada si la prueba no ha iniciado (depende de lógica externa)
        if (!localStorage.getItem('eval_inicio')) return;

        if (document.hidden) {
            this.iniciarPenalizacion();
        } else {
            this.pausarPenalizacion();
        }
    }

    iniciarPenalizacion() {
        if (this.modalActivo) return; // Ya estaba fuera
        this.modalActivo = true;
        if (this.onAbandon) this.onAbandon();

        // Pausar cualquier intervalo anterior
        if (this.interval) clearInterval(this.interval);

        this.interval = setInterval(() => {
            this.tiempoRestante--;
            
            // Actualizar UI del modal (asumiendo que existe un modal visible)
            const contadorEl = document.getElementById('contadorAbandono');
            if (contadorEl) {
                contadorEl.textContent = this.tiempoRestante;
            }

            if (this.tiempoRestante <= 0) {
                clearInterval(this.interval);
                if (this.onTimeout) this.onTimeout();
            }
        }, 1000);
    }

    pausarPenalizacion() {
        if (this.interval) {
            clearInterval(this.interval);
            this.interval = null;
        }
        this.modalActivo = false;
        if (this.onReturn) this.onReturn(this.tiempoRestante);
    }
}
