package JPA.ni.Test_Razonamiento_B.run;

import javax.persistence.EntityManager;
import org.openxava.jpa.XPersistence;
import JPA.ni.Test_Razonamiento_B.model.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DbInitializer {

    public static void initData() {
        EntityManager em = XPersistence.getManager();

        // --- Paso 1: Migración de esquema (aislada con savepoints) ---
        SchemaMigration.ensureAspiranteSchema(em);

        // --- Paso 2: Verificar si ya existen datos ---
        long count = (long) em.createQuery("select count(p) from PruebaRazonamiento p").getSingleResult();
        if (count > 0) {
            System.out.println("Base de datos ya inicializada.");
            return;
        }

        System.out.println("Inicializando base de datos para Test de Razonamiento B...");

        // --- Paso 3: Crear datos iniciales ---
        // (la transacción ya está activa — el Listener la gestiona)

        // 2. Crear Prueba de Razonamiento
        PruebaRazonamiento prueba = new PruebaRazonamiento();
        prueba.setCodigo("RAZONAMIENTO_B");
        prueba.setNombre("Test de Razonamiento - Forma B");
        prueba.setDescripcion("Test estandarizado de razonamiento para la evaluación psicométrica de capacidades lógicas, numéricas, verbales y abstractas.");
        prueba.setDuracionSegundos(720); // 12 minutos
        prueba.setNumeroPreguntas(30);
        prueba.setFechaCreacion(LocalDate.now());
        prueba.setActiva(true);
        em.persist(prueba);

        // 3. Crear Partes de la Prueba
        ParteRazonamiento parte1 = createParte(em, "PARTE1", "Razonamiento Verbal", "Ejercicios de analogías y relaciones conceptuales.", 1, prueba);
        ParteRazonamiento parte2 = createParte(em, "PARTE2", "Series Alfanuméricas", "Progresiones lógicas de números y letras.", 2, prueba);
        ParteRazonamiento parte3 = createParte(em, "PARTE3", "Codificación Cognitiva", "Ejercicios de descifrado de códigos y patrones de letras y símbolos.", 3, prueba);
        ParteRazonamiento parte4 = createParte(em, "PARTE4", "Lógica Matemática", "Problemas razonados cuantitativos.", 4, prueba);

        // 4. Crear Preguntas (Batería 26 a 55)
        // Pregunta 26
        createPregunta(em, 26, TipoPregunta.CODIFICACION, NivelPregunta.FACIL,
            "duro — rudo; rabos — [ ]", parte3,
            "colar", "parar", "sobar", "durar", LetraRespuesta.C);

        // Pregunta 27
        createPregunta(em, 27, TipoPregunta.SERIES_ALFABETICAS, NivelPregunta.FACIL,
            "A — B — D — G — K — [ ] — [ ]", parte2,
            "O — U", "P — V", "P — U", "R — W", LetraRespuesta.B);

        // Pregunta 28
        createPregunta(em, 28, TipoPregunta.ANALOGIA, NivelPregunta.FACIL,
            "VINO es a VIÑA con VIRUTA es a [ ]", parte1,
            "SIERRA", "GARLOPA", "ARPINTERO", "MADERA", LetraRespuesta.D);

        // Pregunta 29
        createPregunta(em, 29, TipoPregunta.CODIFICACION, NivelPregunta.FACIL,
            "lenguado — 1235678; 752163 — [ ]", parte3,
            "duelo", "legado", "dogal", "duelan", LetraRespuesta.D);

        // Pregunta 30
        createPregunta(em, 30, TipoPregunta.SERIES_NUMERICAS, NivelPregunta.FACIL,
            "13542 — 24653 — 35764 — [ ]", parte2,
            "48576", "46875", "57864", "47588", LetraRespuesta.B);

        // Pregunta 31
        createPregunta(em, 31, TipoPregunta.SERIES_ALFABETICAS, NivelPregunta.FACIL,
            "a — m — c — o — r — q — g — [ ]", parte2,
            "s — e", "i — l", "s — l", "h — u", LetraRespuesta.C);

        // Pregunta 32
        createPregunta(em, 32, TipoPregunta.ANALOGIA, NivelPregunta.FACIL,
            "POCO es a MUCHO como BREVE es a [ ]", parte1,
            "ALTO", "CORTO", "EXTENSO", "GRANDE", LetraRespuesta.C);

        // Pregunta 33
        createPregunta(em, 33, TipoPregunta.CODIFICACION, NivelPregunta.INTERMEDIO,
            "cicuta — cita; radioyente — trae; ascensor — [ ]", parte3,
            "cosa", "seno", "casa", "raso", LetraRespuesta.B);

        // Pregunta 34
        createPregunta(em, 34, TipoPregunta.SERIES_ALFABETICAS, NivelPregunta.INTERMEDIO,
            "BACAB — FEGEF — JIKIJ — [ ]", parte2,
            "NMOMM", "MONOM", "ONPNO", "NOMON", LetraRespuesta.D);

        // Pregunta 35
        createPregunta(em, 35, TipoPregunta.LOGICA, NivelPregunta.INTERMEDIO,
            "Una ilusionista saca de una caja cuatro cajitas: Una blanca, una azul, una verde, una anaranjada; " +
            "de la blanca, saca tres nuevas cajas; de la azul, cuatro cajas; de la verde, una paloma; " +
            "de la anaranjada, un pañuelo rojo. ¿Cuántas cajas usa en total?", parte4,
            "11", "12", "14", "9", LetraRespuesta.B);

        // Pregunta 36
        createPregunta(em, 36, TipoPregunta.CODIFICACION, NivelPregunta.INTERMEDIO,
            "8 — negligir; 7 — notario; 4 — azul; [ ] — negligente", parte3,
            "3", "11", "19", "10", LetraRespuesta.D);

        // Pregunta 37
        createPregunta(em, 37, TipoPregunta.SERIES_ALFABETICAS, NivelPregunta.INTERMEDIO,
            "d — c — b — e — d — c — f — [ ] — [ ]", parte2,
            "f — h", "g — k", "d — h", "d — g", LetraRespuesta.C);

        // Pregunta 38
        createPregunta(em, 38, TipoPregunta.ANALOGIA, NivelPregunta.INTERMEDIO,
            "OJO es a VISTA como OREJA es a [ ]", parte1,
            "SONIDO", "OIDO", "AUDICION", "SERENATA", LetraRespuesta.B);

        // Pregunta 39
        createPregunta(em, 39, TipoPregunta.ANALOGIA, NivelPregunta.INTERMEDIO,
            "limitado — reducido — lacónico — resumido — [ ]", parte1,
            "acortado", "disminuido", "restringido", "aminorado", LetraRespuesta.C);

        // Pregunta 40
        createPregunta(em, 40, TipoPregunta.SERIES_NUMERICAS, NivelPregunta.INTERMEDIO,
            "103, 102, 109, 104, 101, 106, 105, 108, 100, [ ]", parte2,
            "106", "110", "107", "111", LetraRespuesta.A);

        // Pregunta 41
        createPregunta(em, 41, TipoPregunta.SERIES_ALFABETICAS, NivelPregunta.INTERMEDIO,
            "JLK — NPO — RTS — [ ]", parte2,
            "VXW", "UWV", "VWX", "XVW", LetraRespuesta.A);

        // Pregunta 42
        createPregunta(em, 42, TipoPregunta.ANALOGIA, NivelPregunta.INTERMEDIO,
            "PEZ — es a MAR como TOPO es a [ ]", parte1,
            "PRADO", "AIRE", "HIERRA", "TIERRA", LetraRespuesta.D);

        // Pregunta 43
        createPregunta(em, 43, TipoPregunta.CODIFICACION, NivelPregunta.AVANZADO,
            "química — máquina; nimedgo — [ ]", parte3,
            "ODIEN", "MENDIGO", "DIMERA", "GOMIFERO", LetraRespuesta.B);

        // Pregunta 44
        createPregunta(em, 44, TipoPregunta.CODIFICACION, NivelPregunta.AVANZADO,
            "312 — BAC; 571 — [ ]", parte3,
            "ERE", "AGE", "LAC", "AIR", LetraRespuesta.B);

        // Pregunta 45
        createPregunta(em, 45, TipoPregunta.MATEMATICA, NivelPregunta.AVANZADO,
            "Si un paracaídas de 1 metro de diámetro, puede sostener una carga de 5 kilogramos, " +
            "un paracaídas de 3 metros de diámetro, puede sostener [ ]", parte4,
            "15 kilogramos", "30 kilogramos", "45 kilogramos", "60 kilogramos", LetraRespuesta.C);

        // Pregunta 46
        createPregunta(em, 46, TipoPregunta.SERIES_NUMERICAS, NivelPregunta.AVANZADO,
            "91 — 73 — 55 — [ ]", parte2,
            "28", "64", "37", "46", LetraRespuesta.C);

        // Pregunta 47
        createPregunta(em, 47, TipoPregunta.CODIFICACION, NivelPregunta.AVANZADO,
            "P2S1 — SP; S31E5N2E4 — [ ]", parte3,
            "INEES", "INSEE", "ENSIE", "USINE", LetraRespuesta.B);

        // Pregunta 48
        createPregunta(em, 48, TipoPregunta.ANALOGIA, NivelPregunta.AVANZADO,
            "HOY es a AYER como CRUCERO es a [ ]", parte1,
            "BALSA", "ACORAZADO", "VELERO", "BUQUE DE CARGA", LetraRespuesta.C);

        // Pregunta 49
        createPregunta(em, 49, TipoPregunta.CODIFICACION, NivelPregunta.AVANZADO,
            "bol — óbolo; ion — ilusionista; su — [ ]", parte3,
            "presumir", "usurpar", "sumar", "usurero", LetraRespuesta.D);

        // Pregunta 50
        createPregunta(em, 50, TipoPregunta.CODIFICACION, NivelPregunta.AVANZADO,
            "I — 1; XIV — 5; XII — 4; VI — [ ]", parte3,
            "6", "8", "3", "2", LetraRespuesta.C);

        // Pregunta 51
        createPregunta(em, 51, TipoPregunta.SERIES_ALFABETICAS, NivelPregunta.AVANZADO,
            "ABC, KOT, AJT, [ ]", parte2,
            "POK", "ABT", "CQJ", "OQP", LetraRespuesta.C);

        // Pregunta 52
        createPregunta(em, 52, TipoPregunta.ANALOGIA, NivelPregunta.AVANZADO,
            "MINISTRO es a GOBIERNO como LADO es a [ ]", parte1,
            "RECTA", "HEXAGONO", "CIRCULO", "PARABOLA", LetraRespuesta.B);

        // Pregunta 53
        createPregunta(em, 53, TipoPregunta.CODIFICACION, NivelPregunta.AVANZADO,
            "valla — 122; serenas — 22111; codiciada — [ ]", parte3,
            "11222", "22211", "21222", "22122", LetraRespuesta.D);

        // Pregunta 54
        createPregunta(em, 54, TipoPregunta.CODIFICACION, NivelPregunta.AVANZADO,
            "10 — diezmar; 100 — ciencia; 1000 — [ ]", parte3,
            "mímica", "milagro", "migaja", "migración", LetraRespuesta.B);

        // Pregunta 55
        createPregunta(em, 55, TipoPregunta.MATEMATICA, NivelPregunta.AVANZADO,
            "Pedro posee ciento noventa y dos bolas; da la mitad a Santiago; de las que quedan da la tercera " +
            "parte a Francisco, después de la cuarta parte de las que le quedan a Juan y finalmente de las que " +
            "le quedan da las tres cuartas partes a Marcos. ¿Cuántas bolas le quedan?", parte4,
            "12", "2", "36", "6", LetraRespuesta.A);

        // 5. Tabla de Baremos Normativos
        createBaremo(em, 0,  0,  "1",  "Muy Bajo",     "Deficiente",            "Capacidad de razonamiento lógico deficiente o no desarrollada.");
        createBaremo(em, 1,  5,  "10", "Bajo",          "Inferior al promedio",  "Habilidad de razonamiento significativamente por debajo de la norma.");
        createBaremo(em, 6,  10, "30", "Promedio Bajo", "Término medio inferior","Habilidad básica de razonamiento con dificultades en problemas complejos.");
        createBaremo(em, 11, 17, "50", "Promedio",      "Término medio",         "Habilidad de razonamiento adecuada y correspondiente a la media de la población.");
        createBaremo(em, 18, 22, "75", "Promedio Alto", "Término medio superior","Buena capacidad de análisis, deducción y agilidad mental.");
        createBaremo(em, 23, 27, "90", "Alto",          "Superior al promedio",  "Excelente capacidad de razonamiento lógico, rapidez y resolución de problemas.");
        createBaremo(em, 28, 30, "99", "Muy Alto",      "Sobresaliente",         "Capacidades intelectuales excepcionales en análisis lógico y deductivo.");

        System.out.println("Base de datos inicializada exitosamente con 30 preguntas y 7 baremos.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static ParteRazonamiento createParte(EntityManager em, String codigo, String nombre, String desc, int orden, PruebaRazonamiento prueba) {
        ParteRazonamiento p = new ParteRazonamiento();
        p.setCodigoParte(codigo);
        p.setNombreParte(nombre);
        p.setDescripcion(desc);
        p.setNumeroOrden(orden);
        p.setPrueba(prueba);
        p.setActiva(true);
        em.persist(p);
        return p;
    }

    private static void createPregunta(EntityManager em, int numero, TipoPregunta tipo, NivelPregunta nivel, String enunciado,
                                       ParteRazonamiento parte,
                                       String opA, String opB, String opC, String opD,
                                       LetraRespuesta correcta) {
        ItemRazonamiento item = new ItemRazonamiento();
        item.setNumero(numero);
        item.setTipoPregunta(tipo);
        item.setNivel(nivel);
        item.setEnunciado(enunciado);
        item.setParte(parte);
        item.setActivo(true);

        item.getOpciones().add(createOpcion(LetraRespuesta.A, opA, 1, correcta == LetraRespuesta.A));
        item.getOpciones().add(createOpcion(LetraRespuesta.B, opB, 2, correcta == LetraRespuesta.B));
        item.getOpciones().add(createOpcion(LetraRespuesta.C, opC, 3, correcta == LetraRespuesta.C));
        item.getOpciones().add(createOpcion(LetraRespuesta.D, opD, 4, correcta == LetraRespuesta.D));

        em.persist(item);
    }

    private static OpcionRespuesta createOpcion(LetraRespuesta letra, String texto, int orden, boolean esCorrecta) {
        OpcionRespuesta o = new OpcionRespuesta();
        o.setLetra(letra);
        o.setTexto(texto);
        o.setOrden(orden);
        o.setEsCorrecta(esCorrecta);
        return o;
    }

    private static void createBaremo(EntityManager em, int min, int max, String percentil, String nivel, String diagnostico, String desc) {
        BaremoNormativo b = new BaremoNormativo();
        b.setPuntuacionMinima(min);
        b.setPuntuacionMaxima(max);
        b.setPercentil(percentil);
        b.setNivel(nivel);
        b.setDiagnostico(diagnostico);
        b.setDescripcion(desc);
        b.setActivo(true);
        em.persist(b);
    }
}
