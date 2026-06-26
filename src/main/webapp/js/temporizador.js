/**
 * temporizador.js - Control preciso del tiempo de evaluación
 */

class TemporizadorExacto {
    constructor(duracionMinutos, onTick, onFinish) {
        this.duracionMs = duracionMinutos * 60 * 1000;
        this.onTick = onTick;
        this.onFinish = onFinish;
        this.intervalId = null;
        this.tiempoInicio = null;
    }

    iniciar() {
        // Guardar el tiempo de inicio real en localStorage para persistir ante recargas
        const inicioGuardado = localStorage.getItem('eval_inicio');
        if (inicioGuardado) {
            this.tiempoInicio = parseInt(inicioGuardado, 10);
        } else {
            this.tiempoInicio = Date.now();
            localStorage.setItem('eval_inicio', this.tiempoInicio);
        }

        this.tick();
        this.intervalId = setInterval(() => this.tick(), 1000);
    }

    tick() {
        const ahora = Date.now();
        const tiempoTranscurrido = ahora - this.tiempoInicio;
        const tiempoRestante = this.duracionMs - tiempoTranscurrido;

        if (tiempoRestante <= 0) {
            this.detener();
            this.onTick(0);
            if (this.onFinish) this.onFinish();
        } else {
            this.onTick(tiempoRestante);
        }
    }

    detener() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
    }

    limpiar() {
        this.detener();
        localStorage.removeItem('eval_inicio');
    }

    static formato(ms) {
        if (ms < 0) ms = 0;
        const totalSegundos = Math.floor(ms / 1000);
        const minutos = Math.floor(totalSegundos / 60);
        const segundos = totalSegundos % 60;
        return `${minutos.toString().padStart(2, '0')}:${segundos.toString().padStart(2, '0')}`;
    }
}
