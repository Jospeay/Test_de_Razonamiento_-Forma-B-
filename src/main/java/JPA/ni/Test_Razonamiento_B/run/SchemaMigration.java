package JPA.ni.Test_Razonamiento_B.run;

import javax.persistence.EntityManager;

/**
 * Aplica migraciones de esquema de forma resiliente.
 *
 * IMPORTANTE: Hibernate sin naming strategy configura las columnas en
 * PostgreSQL como minúsculas sin comillas.
 * Por eso "tipoColegio" (@Column name) se almacena como "tipocolegio" en la BD.
 * Este script usa los nombres reales que genera Hibernate (lowercase, sin comillas).
 *
 * Cada sentencia SQL usa su propio SAVEPOINT para que, si falla, sólo
 * se deshace esa sentencia y NO se aborta la transacción principal.
 */
public class SchemaMigration {

    public static void ensureAspiranteSchema(EntityManager em) {
        // Columna tipocolegio (lowercase — así la genera Hibernate sin naming strategy)
        executeSafe(em,
            "ALTER TABLE aspirante ADD COLUMN IF NOT EXISTS tipocolegio VARCHAR(20) DEFAULT 'PUBLICO'");

        // Garantizar que filas existentes tengan valor válido (no NULL)
        executeSafe(em,
            "UPDATE aspirante SET tipocolegio = 'PUBLICO' WHERE tipocolegio IS NULL OR tipocolegio = ''");

        // Normalizar a mayúsculas por si algún valor quedó en otro formato
        executeSafe(em,
            "UPDATE aspirante SET tipocolegio = UPPER(tipocolegio) " +
            "WHERE tipocolegio IS NOT NULL AND tipocolegio != UPPER(tipocolegio)");

        System.out.println("SchemaMigration: esquema de aspirante verificado.");
    }

    /**
     * Ejecuta una sentencia SQL dentro de un SAVEPOINT propio.
     * Si falla, hace ROLLBACK sólo al savepoint — la transacción externa sigue válida.
     */
    private static void executeSafe(EntityManager em, String sql) {
        try {
            em.createNativeQuery("SAVEPOINT sp_schema").executeUpdate();
            em.createNativeQuery(sql).executeUpdate();
            em.createNativeQuery("RELEASE SAVEPOINT sp_schema").executeUpdate();
        } catch (Exception e) {
            try {
                em.createNativeQuery("ROLLBACK TO SAVEPOINT sp_schema").executeUpdate();
            } catch (Exception ignored) { }
            System.out.println("SchemaMigration (ignorado): " + e.getMessage());
        }
    }
}
