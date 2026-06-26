package JPA.ni.Test_Razonamiento_B.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.persistence.EntityManager;
import org.openxava.jpa.XPersistence;
import JPA.ni.Test_Razonamiento_B.model.*;
import JPA.ni.Test_Razonamiento_B.run.SchemaMigration;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@WebServlet("/api/*")
public class ExamenServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addCorsHeaders(response); // Garantiza CORS en CUALQUIER respuesta GET
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Endpoint no especificado");
            return;
        }

        try {
            if (pathInfo.equals("/location/data")) {
                handleGetLocationData(response);
            } else if (pathInfo.equals("/exam/questions")) {
                handleGetQuestions(response);
            } else if (pathInfo.equals("/debug/db")) {
                handleDebugDb(response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Ruta no encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno: " + e.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addCorsHeaders(response); // Garantiza CORS en CUALQUIER respuesta POST
        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Endpoint no especificado");
            return;
        }

        try {
            if (pathInfo.equals("/auth/register-or-login")) {
                handleRegisterOrLogin(request, response);
            } else if (pathInfo.equals("/exam/start")) {
                handleStartExam(request, response);
            } else if (pathInfo.equals("/exam/save-answer")) {
                handleSaveAnswer(request, response);
            } else if (pathInfo.equals("/exam/submit")) {
                handleSubmitExam(request, response);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Ruta no encontrada");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno: " + e.getMessage());
        }
    }

    // 1. Obtener Departamentos y Municipios para el registro
    private void handleGetLocationData(HttpServletResponse response) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Departamento[] departamentos = Departamento.values();
        for (int i = 0; i < departamentos.length; i++) {
            Departamento d = departamentos[i];
            sb.append("{");
            sb.append("\"id\":\"").append(d.name()).append("\",");
            sb.append("\"nombre\":\"").append(escapeJson(d.getNombre())).append("\",");
            sb.append("\"municipios\":[]");
            sb.append("}");
            if (i < departamentos.length - 1) sb.append(",");
        }
        sb.append("]");

        sendJson(response, sb.toString());
    }

    // 2. Obtener preguntas de la prueba (OCULTANDO si la opción es correcta para evitar fraudes en inspección HTML)
    private void handleGetQuestions(HttpServletResponse response) throws Exception {
        EntityManager em = XPersistence.getManager();
        List<ItemRazonamiento> items = em.createQuery("select i from ItemRazonamiento i where i.activo = true order by i.numero", ItemRazonamiento.class).getResultList();

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < items.size(); i++) {
            ItemRazonamiento item = items.get(i);
            sb.append("{");
            sb.append("\"id\":").append(item.getId()).append(",");
            sb.append("\"numero\":").append(item.getNumero()).append(",");
            sb.append("\"tipoPregunta\":\"").append(item.getTipoPregunta().name()).append("\",");
            sb.append("\"nivel\":\"").append(item.getNivel().name()).append("\",");
            sb.append("\"enunciado\":\"").append(escapeJson(item.getEnunciado())).append("\",");
            sb.append("\"opciones\":[");

            List<OpcionRespuesta> opciones = item.getOpciones();
            for (int j = 0; j < opciones.size(); j++) {
                OpcionRespuesta op = opciones.get(j);
                sb.append("{");
                sb.append("\"letra\":\"").append(op.getLetra().name()).append("\",");
                sb.append("\"texto\":\"").append(escapeJson(op.getTexto())).append("\",");
                sb.append("\"orden\":").append(op.getOrden());
                sb.append("}");
                if (j < opciones.size() - 1) sb.append(",");
            }
            sb.append("]");
            sb.append("}");
            if (i < items.size() - 1) sb.append(",");
        }
        sb.append("]");

        sendJson(response, sb.toString());
    }

    // 3. Registro / Login de Aspirantes
    private void handleRegisterOrLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String cedula = request.getParameter("cedula");
        String nombreCompleto = request.getParameter("nombreCompleto");
        String edadStr = request.getParameter("edad");
        String sexoStr = request.getParameter("sexo");
        String deptoIdStr = request.getParameter("departamentoId");
        String muniIdStr = request.getParameter("municipioId");
        String zonaStr = request.getParameter("zona");
        String tipoColegioStr = request.getParameter("tipoColegio");
        String correo = request.getParameter("correo");
        String telefono = request.getParameter("telefono");

        if (cedula == null || cedula.trim().isEmpty()) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Cédula requerida.");
            return;
        }

        EntityManager em = XPersistence.getManager();

        SchemaMigration.ensureAspiranteSchema(em);

        Aspirante aspirante = null;

        // Buscar si ya existe
        List<Aspirante> list = em.createQuery("select a from Aspirante a where a.cedula = :cedula", Aspirante.class)
                .setParameter("cedula", cedula)
                .getResultList();

        boolean transactionStartedHere = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            transactionStartedHere = true;
        }
        try {
            if (!list.isEmpty()) {
                aspirante = list.get(0);
            } else {
                // Registrar nuevo
                if (nombreCompleto == null || edadStr == null || sexoStr == null || deptoIdStr == null || muniIdStr == null || zonaStr == null || tipoColegioStr == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Datos de registro incompletos.");
                    if (transactionStartedHere && em.getTransaction().isActive()) em.getTransaction().rollback();
                    return;
                }

                aspirante = new Aspirante();
                aspirante.setCedula(cedula);
                aspirante.setNombreCompleto(nombreCompleto);
                aspirante.setEdad(Integer.parseInt(edadStr));
                
                // Mapear sexo (puede venir en minúsculas, mayúsculas o CamelCase de la web)
                aspirante.setSexo(Sexo.valueOf(sexoStr.toUpperCase()));
                
                // Mapear zona
                aspirante.setZona(Zona.valueOf(zonaStr.toUpperCase()));
                
                // Mapear tipoColegio
                aspirante.setTipoColegio(TipoColegio.valueOf(tipoColegioStr.toUpperCase()));

                aspirante.setCorreo(correo);
                aspirante.setTelefono(telefono);
                aspirante.setFechaRegistro(java.time.LocalDate.now());
                aspirante.setActivo(true);

                Departamento depto = Departamento.valueOf(deptoIdStr.toUpperCase());
                aspirante.setDepartamento(depto);
                aspirante.setMunicipio(muniIdStr);

                em.persist(aspirante);
            }
            if (transactionStartedHere) {
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            if (transactionStartedHere && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }

        String json = "{" +
                "\"id\":" + aspirante.getId() + "," +
                "\"codigo\":\"" + aspirante.getCodigo() + "\"," +
                "\"nombreCompleto\":\"" + escapeJson(aspirante.getNombreCompleto()) + "\"," +
                "\"cedula\":\"" + escapeJson(aspirante.getCedula()) + "\"" +
                "}";

        sendJson(response, json);
    }

    // 4. Iniciar o recuperar sesión de examen
    private void handleStartExam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String aspiranteIdStr = request.getParameter("aspiranteId");
        if (aspiranteIdStr == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Aspirante ID requerido.");
            return;
        }

        EntityManager em = XPersistence.getManager();
        Integer aspiranteId = Integer.parseInt(aspiranteIdStr);

        Aspirante aspirante = em.find(Aspirante.class, aspiranteId);
        if (aspirante == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Aspirante no encontrado.");
            return;
        }

        // Buscar prueba activa
        List<PruebaRazonamiento> pruebas = em.createQuery("select p from PruebaRazonamiento p where p.codigo = 'RAZONAMIENTO_B' and p.activa = true", PruebaRazonamiento.class).getResultList();
        if (pruebas.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Prueba de razonamiento no configurada.");
            return;
        }
        PruebaRazonamiento prueba = pruebas.get(0);

        // Buscar si ya tiene una sesión
        List<SesionTest> sesiones = em.createQuery("select s from SesionTest s where s.aspirante.id = :aspId and s.prueba.id = :pruebaId", SesionTest.class)
                .setParameter("aspId", aspiranteId)
                .setParameter("pruebaId", prueba.getId())
                .getResultList();

        SesionTest sesion = null;
        boolean transactionStartedHere = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            transactionStartedHere = true;
        }
        try {
            if (!sesiones.isEmpty()) {
                sesion = sesiones.get(0);
                if (sesion.getEstado() == EstadoSesion.EN_PROCESO) {
                    // Calcular tiempo restante real basado en servidor
                    long segundosTranscurridos = Duration.between(sesion.getFechaInicio(), LocalDateTime.now()).getSeconds();
                    int restante = sesion.getDuracionTotal() - (int) segundosTranscurridos;

                    if (restante <= 0) {
                        // El tiempo se terminó, cerrar sesión automáticamente
                        sesion.setEstado(EstadoSesion.EXPIRADA);
                        sesion.setTiempoRestante(0);
                        sesion.setFechaFin(LocalDateTime.now());
                        calcularYPersistirResultado(em, sesion);
                    } else {
                        sesion.setTiempoRestante(restante);
                    }
                }
            } else {
                // Crear nueva sesión
                sesion = new SesionTest();
                sesion.setAspirante(aspirante);
                sesion.setPrueba(prueba);
                sesion.setFechaInicio(LocalDateTime.now());
                sesion.setEstado(EstadoSesion.EN_PROCESO);
                sesion.setDuracionTotal(prueba.getDuracionSegundos());
                sesion.setTiempoRestante(prueba.getDuracionSegundos());
                sesion.setPreguntasRespondidas(0);
                sesion.setPreguntasCorrectas(0);
                sesion.setPreguntasIncorrectas(0);
                sesion.setPuntajeFinal(0);

                em.persist(sesion);
            }
            if (transactionStartedHere) {
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            if (transactionStartedHere && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }

        // Obtener respuestas ya guardadas para que el frontend las pinte
        StringBuilder respuestasJson = new StringBuilder();
        respuestasJson.append("[");
        List<RespuestaUsuario> respuestas = sesion.getRespuestas();
        for (int i = 0; i < respuestas.size(); i++) {
            RespuestaUsuario ru = respuestas.get(i);
            respuestasJson.append("{");
            respuestasJson.append("\"numeroPregunta\":").append(ru.getItem().getNumero()).append(",");
            respuestasJson.append("\"opcionSeleccionada\":").append(ru.getOpcionSeleccionada() == null ? "null" : "\"" + ru.getOpcionSeleccionada().name() + "\"");
            respuestasJson.append("}");
            if (i < respuestas.size() - 1) respuestasJson.append(",");
        }
        respuestasJson.append("]");

        String json = "{" +
                "\"sessionId\":" + sesion.getId() + "," +
                "\"estado\":\"" + sesion.getEstado().name() + "\"," +
                "\"tiempoRestante\":" + sesion.getTiempoRestante() + "," +
                "\"respuestasGuardadas\":" + respuestasJson.toString() +
                "}";

        sendJson(response, json);
    }

    // 5. Guardar respuesta en tiempo real (Autoguardado / AJAX)
    private void handleSaveAnswer(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sessionIdStr = request.getParameter("sessionId");
        String questionNumStr = request.getParameter("questionNum");
        String selectedOptionStr = request.getParameter("selectedOption"); // puede venir vacío o null si es para limpiar/dejar en blanco

        if (sessionIdStr == null || questionNumStr == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Faltan parámetros.");
            return;
        }

        EntityManager em = XPersistence.getManager();
        Integer sessionId = Integer.parseInt(sessionIdStr);
        Integer questionNum = Integer.parseInt(questionNumStr);

        SesionTest sesion = em.find(SesionTest.class, sessionId);
        if (sesion == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Sesión no encontrada.");
            return;
        }

        if (sesion.getEstado() != EstadoSesion.EN_PROCESO) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Examen no activo.");
            return;
        }

        // Verificar cronómetro server-side
        long segundosTranscurridos = Duration.between(sesion.getFechaInicio(), LocalDateTime.now()).getSeconds();
        int restante = sesion.getDuracionTotal() - (int) segundosTranscurridos;
        if (restante <= 0) {
            em.getTransaction().begin();
            try {
                sesion.setEstado(EstadoSesion.EXPIRADA);
                sesion.setTiempoRestante(0);
                sesion.setFechaFin(LocalDateTime.now());
                calcularYPersistirResultado(em, sesion);
                em.getTransaction().commit();
            } catch (Exception e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "El tiempo del examen ha finalizado.");
            return;
        }

        // Buscar pregunta
        List<ItemRazonamiento> items = em.createQuery("select i from ItemRazonamiento i where i.numero = :num", ItemRazonamiento.class)
                .setParameter("num", questionNum)
                .getResultList();
        if (items.isEmpty()) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Pregunta no encontrada.");
            return;
        }
        ItemRazonamiento item = items.get(0);

        boolean transactionStartedHere = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            transactionStartedHere = true;
        }
        try {
            // Actualizar tiempo restante
            sesion.setTiempoRestante(restante);

            // Buscar si ya existía respuesta para este ítem
            RespuestaUsuario respuestaExistente = null;
            for (RespuestaUsuario ru : sesion.getRespuestas()) {
                if (ru.getItem().getId().equals(item.getId())) {
                    respuestaExistente = ru;
                    break;
                }
            }

            LetraRespuesta letraElegida = (selectedOptionStr == null || selectedOptionStr.trim().isEmpty() || selectedOptionStr.equals("null"))
                    ? null : LetraRespuesta.valueOf(selectedOptionStr.toUpperCase());

            boolean esCorrecta = false;
            if (letraElegida != null) {
                // Verificar si es correcta comparando con la opción marcada con esCorrecta = true
                for (OpcionRespuesta op : item.getOpciones()) {
                    if (op.getLetra() == letraElegida && Boolean.TRUE.equals(op.getEsCorrecta())) {
                        esCorrecta = true;
                        break;
                    }
                }
            }

            if (respuestaExistente != null) {
                respuestaExistente.setOpcionSeleccionada(letraElegida);
                respuestaExistente.setCorrecta(esCorrecta);
                respuestaExistente.setFechaRespuesta(LocalDateTime.now());
                respuestaExistente.setTiempoRespuesta((int) segundosTranscurridos);
            } else {
                RespuestaUsuario nueva = new RespuestaUsuario();
                nueva.setItem(item);
                nueva.setOpcionSeleccionada(letraElegida);
                nueva.setCorrecta(esCorrecta);
                nueva.setFechaRespuesta(LocalDateTime.now());
                nueva.setTiempoRespuesta((int) segundosTranscurridos);
                sesion.getRespuestas().add(nueva);
            }

            // Recalcular contadores parciales en la sesión
            int respondidas = 0;
            int correctas = 0;
            int incorrectas = 0;
            for (RespuestaUsuario ru : sesion.getRespuestas()) {
                if (ru.getOpcionSeleccionada() != null) {
                    respondidas++;
                    if (ru.getCorrecta()) {
                        correctas++;
                    } else {
                        incorrectas++;
                    }
                }
            }
            sesion.setPreguntasRespondidas(respondidas);
            sesion.setPreguntasCorrectas(correctas);
            sesion.setPreguntasIncorrectas(incorrectas);
            sesion.setPuntajeFinal(correctas);

            if (transactionStartedHere) {
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            if (transactionStartedHere && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }

        sendJson(response, "{\"status\":\"success\", \"tiempoRestante\":" + restante + "}");
    }

    // 6. Finalizar examen manualmente y calificar
    private void handleSubmitExam(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sessionIdStr = request.getParameter("sessionId");
        if (sessionIdStr == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Sesión ID requerido.");
            return;
        }

        EntityManager em = XPersistence.getManager();
        Integer sessionId = Integer.parseInt(sessionIdStr);

        SesionTest sesion = em.find(SesionTest.class, sessionId);
        if (sesion == null) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Sesión no encontrada.");
            return;
        }

        if (sesion.getEstado() != EstadoSesion.EN_PROCESO) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "La sesión ya ha sido finalizada previamente.");
            return;
        }

        boolean transactionStartedHere = false;
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            transactionStartedHere = true;
        }
        try {
            sesion.setEstado(EstadoSesion.FINALIZADA);
            sesion.setFechaFin(LocalDateTime.now());
            sesion.setTiempoRestante(0);

            ResultadoEvaluacion resultado = calcularYPersistirResultado(em, sesion);
            if (transactionStartedHere) {
                em.getTransaction().commit();
            }

            String jsonResult = "{" +
                    "\"status\":\"success\"," +
                    "\"respuestasCorrectas\":" + resultado.getRespuestasCorrectas() + "," +
                    "\"respuestasIncorrectas\":" + resultado.getRespuestasIncorrectas() + "," +
                    "\"preguntasContestadas\":" + resultado.getPreguntasContestadas() + "," +
                    "\"puntaje\":" + resultado.getPuntaje() + "," +
                    "\"porcentajeAcierto\":" + resultado.getPorcentajeAcierto() + "," +
                    "\"percentil\":\"" + resultado.getPercentil() + "\"," +
                    "\"diagnostico\":\"" + escapeJson(resultado.getDiagnostico()) + "\"" +
                    "}";

            sendJson(response, jsonResult);

        } catch (Exception e) {
            if (transactionStartedHere && em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    // Método utilitario para calificar la sesión contra el BaremoNormativo y guardar el ResultadoEvaluacion
    private ResultadoEvaluacion calcularYPersistirResultado(EntityManager em, SesionTest sesion) {
        int correctas = 0;
        int contestadas = 0;

        for (RespuestaUsuario ru : sesion.getRespuestas()) {
            if (ru.getOpcionSeleccionada() != null) {
                contestadas++;
                if (ru.getCorrecta()) {
                    correctas++;
                }
            }
        }

        int incorrectas = contestadas - correctas;
        List<ItemRazonamiento> activeItems = em.createQuery("select i from ItemRazonamiento i where i.activo = true", ItemRazonamiento.class).getResultList();
        int totalQuestions = activeItems.isEmpty() ? 1 : activeItems.size();
        double porcentaje = ((double) correctas / totalQuestions) * 100.0;

        // Actualizar sesión
        sesion.setPreguntasRespondidas(contestadas);
        sesion.setPreguntasCorrectas(correctas);
        sesion.setPreguntasIncorrectas(incorrectas);
        sesion.setPuntajeFinal(correctas);

        // Buscar Baremo aplicable
        List<BaremoNormativo> baremos = em.createQuery(
                "select b from BaremoNormativo b where b.activo = true and :score >= b.puntuacionMinima and :score <= b.puntuacionMaxima",
                BaremoNormativo.class)
                .setParameter("score", correctas)
                .getResultList();

        BaremoNormativo baremo = null;
        String percentil = "N/A";
        String diagnostico = "Sin diagnóstico";

        if (!baremos.isEmpty()) {
            baremo = baremos.get(0);
            percentil = baremo.getPercentil();
            diagnostico = baremo.getDiagnostico();
        }

        // Buscar si ya existe resultado para esta sesión
        List<ResultadoEvaluacion> resExistentes = em.createQuery(
                "select r from ResultadoEvaluacion r where r.sesion.id = :sesId", ResultadoEvaluacion.class)
                .setParameter("sesId", sesion.getId())
                .getResultList();

        ResultadoEvaluacion re;
        if (!resExistentes.isEmpty()) {
            re = resExistentes.get(0);
        } else {
            re = new ResultadoEvaluacion();
            re.setSesion(sesion);
        }

        re.setRespuestasCorrectas(correctas);
        re.setRespuestasIncorrectas(incorrectas);
        re.setPreguntasContestadas(contestadas);
        re.setPercentil(percentil);
        re.setDiagnostico(diagnostico);
        re.setPuntaje(correctas);
        re.setPorcentajeAcierto(porcentaje);
        re.setFechaCalculo(LocalDateTime.now());
        re.setBaremo(baremo);

        if (re.getId() == null) {
            em.persist(re);
        }

        sesion.setResultado(re);
        return re;
    }

    // ====== CORS: Permite acceso desde el frontend separado ======
    private void addCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        addCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Funciones utilitarias para Servlet
    private void sendJson(HttpServletResponse response, String json) throws IOException {
        addCorsHeaders(response);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        addCorsHeaders(response);
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"" + escapeJson(message) + "\"}");
        out.flush();
    }

    private void handleDebugDb(HttpServletResponse response) throws Exception {
        EntityManager em = org.openxava.jpa.XPersistence.getManager();
        java.sql.Connection conn = em.unwrap(java.sql.Connection.class);
        java.sql.DatabaseMetaData meta = conn.getMetaData();
        java.sql.ResultSet rs = meta.getColumns(null, null, "aspirante", null);
        StringBuilder sb = new StringBuilder();
        sb.append("Columns in 'aspirante' table:\n");
        while (rs.next()) {
            sb.append("  ").append(rs.getString("COLUMN_NAME")).append(" (").append(rs.getString("TYPE_NAME")).append(")\n");
        }
        
        sb.append("\nActive Columns from query 'SELECT * FROM aspirante LIMIT 1':\n");
        java.sql.Statement stmt = conn.createStatement();
        try {
            java.sql.ResultSet rows = stmt.executeQuery("SELECT * FROM aspirante LIMIT 1");
            java.sql.ResultSetMetaData rowMeta = rows.getMetaData();
            int cols = rowMeta.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                sb.append("  ").append(rowMeta.getColumnName(i)).append(" (").append(rowMeta.getColumnTypeName(i)).append(")\n");
            }
        } catch (Exception e) {
            sb.append("  Error querying columns: ").append(e.toString()).append("\n");
        }
        
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(sb.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
