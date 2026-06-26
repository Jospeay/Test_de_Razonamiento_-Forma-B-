/**
 * preguntas.js - Gestor de preguntas adaptado al formato del ExamenServlet
 * El API devuelve: [{ id, numero, tipoPregunta, nivel, enunciado, opciones:[{letra,texto,orden}] }]
 */
class PreguntasManager {
    constructor() {
        this.preguntas = []; // Array plano ordenado por numero
    }

    async cargarEstructura() {
        const data = await obtenerPreguntas();
        if (!data) {
            console.error('No se pudieron cargar las preguntas del servidor.');
            return false;
        }
        // Ordenar por número
        this.preguntas = data.sort((a, b) => a.numero - b.numero);
        return true;
    }

    obtenerItemEnIndiceGlobal(indice) {
        return this.preguntas[indice] || null;
    }

    obtenerTotalItems() {
        return this.preguntas.length;
    }

    obtenerOpcionesDeItem(itemId) {
        const item = this.preguntas.find(p => p.id === itemId);
        if (!item) return [];
        // Ordenar opciones por su campo 'orden'
        return [...item.opciones].sort((a, b) => a.orden - b.orden);
    }
}
