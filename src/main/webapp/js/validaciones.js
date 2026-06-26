/**
 * validaciones.js - Validaciones para formularios
 */

const Validaciones = {
    esRequerido: (valor) => {
        return valor && valor.trim() !== '';
    },
    
    esCorreoValido: (correo) => {
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return regex.test(correo);
    },

    esCedulaValida: (cedula) => {
        // Validación básica de cédula nicaragüense (ej. 001-010100-0000A)
        // Adaptar según sea necesario
        const regex = /^\d{3}-?\d{6}-?\d{4}[A-Za-z]$/;
        return regex.test(cedula.replace(/\s/g, ''));
    },
    
    esSoloTexto: (texto) => {
        const regex = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;
        return regex.test(texto);
    },

    esSoloNumeros: (numero) => {
        const regex = /^\d+$/;
        return regex.test(numero);
    }
};
