package JPA.ni.Test_Razonamiento_B.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.openxava.jpa.XPersistence;
import JPA.ni.Test_Razonamiento_B.run.DbInitializer;

@WebListener
public class DbInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("DbInitializerListener: Iniciando inicialización de base de datos...");
        try {
            // Iniciar transacción limpia
            if (!XPersistence.getManager().getTransaction().isActive()) {
                XPersistence.getManager().getTransaction().begin();
            }

            DbInitializer.initData();

            if (XPersistence.getManager().getTransaction().isActive()) {
                XPersistence.getManager().getTransaction().commit();
            }
            System.out.println("DbInitializerListener: Inicialización completada.");

        } catch (Exception e) {
            // Rollback seguro — el servidor debe seguir arrancando aunque el init falle
            try {
                if (XPersistence.getManager().getTransaction().isActive()) {
                    XPersistence.getManager().getTransaction().rollback();
                }
            } catch (Exception re) {
                // ignorar error de rollback
            }
            System.err.println("DbInitializerListener: Error durante la inicialización (no crítico para el arranque).");
            e.printStackTrace();
        } finally {
            // Cerrar el EntityManager para liberar la conexión en el thread de inicio
            try {
                XPersistence.getManager().close();
            } catch (Exception ignored) { }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nada que limpiar
    }
}
