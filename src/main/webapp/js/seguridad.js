/**
 * seguridad.js - Reglas Anti-trampas
 */

document.addEventListener('DOMContentLoaded', () => {
    
    const mostrarAlertaSeguridad = (mensaje) => {
        Swal.fire({
            icon: 'warning',
            title: 'Acción No Permitida',
            text: mensaje,
            confirmButtonColor: '#27496D',
            confirmButtonText: 'Entendido',
            backdrop: `rgba(0,0,0,0.8)`
        });
    };

    // Bloquear click derecho
    document.addEventListener('contextmenu', (e) => {
        e.preventDefault();
        mostrarAlertaSeguridad('El menú contextual ha sido deshabilitado por razones de seguridad.');
    });

    // Bloquear combinaciones de teclas
    document.addEventListener('keydown', (e) => {
        // F12
        if (e.key === 'F12') {
            e.preventDefault();
            mostrarAlertaSeguridad('Las herramientas de desarrollo están deshabilitadas.');
        }
        
        // Ctrl+C, Ctrl+V, Ctrl+U, Ctrl+P, Ctrl+S
        if (e.ctrlKey) {
            const forbiddenKeys = ['c', 'v', 'u', 'p', 's', 'C', 'V', 'U', 'P', 'S'];
            if (forbiddenKeys.includes(e.key)) {
                e.preventDefault();
                mostrarAlertaSeguridad(`La combinación Ctrl+${e.key.toUpperCase()} no está permitida durante la evaluación.`);
            }
        }
        
        // Ctrl+Shift+I, Ctrl+Shift+J, Ctrl+Shift+C
        if (e.ctrlKey && e.shiftKey) {
            const forbiddenKeys = ['i', 'j', 'c', 'I', 'J', 'C'];
            if (forbiddenKeys.includes(e.key)) {
                e.preventDefault();
                mostrarAlertaSeguridad('Las herramientas de inspección están deshabilitadas.');
            }
        }
    });

    // Deshabilitar selección de texto (también se puede hacer por CSS con user-select: none)
    document.addEventListener('selectstart', (e) => {
        e.preventDefault();
    });
});
