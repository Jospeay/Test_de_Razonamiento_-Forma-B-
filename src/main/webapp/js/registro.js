/**
 * registro.js - Lógica del formulario de registro conectada al ExamenServlet
 */
document.addEventListener('DOMContentLoaded', () => {
    const formRegistro = document.getElementById('formRegistro');
    if (!formRegistro) return;

    formRegistro.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Recolectar datos del formulario
        const aspirante = {
            cedula:         document.getElementById('cedula').value.trim(),
            nombreCompleto: document.getElementById('nombreCompleto').value.trim(),
            edad:           document.getElementById('edad').value.trim(),
            sexo:           document.getElementById('sexo').value,           // MASCULINO | FEMENINO
            departamentoId: document.getElementById('departamento').value,    // Enum ID (ej. MANAGUA)
            municipio:      document.getElementById('municipio').value.trim(),
            zona:           document.getElementById('zona').value,           // URBANA | RURAL
            tipoColegio:    document.getElementById('tipoColegio').value,    // PUBLICO | PRIVADO
            correo:         document.getElementById('correo').value.trim(),
            telefono:       document.getElementById('telefono').value.trim()
        };

        // Validaciones básicas del lado del cliente
        if (!aspirante.cedula || !aspirante.nombreCompleto || !aspirante.departamentoId) {
            Swal.fire({ icon: 'warning', title: 'Campos incompletos', text: 'Por favor, complete todos los campos obligatorios.', confirmButtonColor: '#27496D' });
            return;
        }

        if (!Validaciones.esCorreoValido(aspirante.correo)) {
            Swal.fire({ icon: 'error', title: 'Correo inválido', text: 'Ingrese una dirección de correo electrónico válida.', confirmButtonColor: '#27496D' });
            return;
        }

        // Mostrar indicador de carga
        Swal.fire({ title: 'Procesando registro...', allowOutsideClick: false, showConfirmButton: false, didOpen: () => { Swal.showLoading(); } });

        const resultado = await guardarAspirante(aspirante);

        setTimeout(() => {
            Swal.hideLoading();
            if (resultado.exito) {
                Swal.fire({
                    icon: 'success',
                    title: '¡Registro Exitoso!',
                    html: `Bienvenido, <strong>${resultado.data.nombreCompleto || aspirante.nombreCompleto}</strong>. A continuación, lea atentamente las instrucciones del test.`,
                    confirmButtonText: 'Continuar',
                    showConfirmButton: true,
                    confirmButtonColor: '#27496D'
                }).then(() => {
                    window.location.href = 'instrucciones.html';
                });
            } else {
                Swal.fire({
                    icon: 'error',
                    title: 'Error de Conexión',
                    html: `No se pudo conectar con el servidor.<br><small>${resultado.mensaje}</small>`,
                    confirmButtonText: 'Entendido',
                    confirmButtonColor: '#dc3545'
                });
            }
        }, 500);
    });
});
