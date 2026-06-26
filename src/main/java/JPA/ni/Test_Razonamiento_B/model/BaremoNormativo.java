package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Entity
@Table(name = "baremo_normativo")
@Getter @Setter
@NoArgsConstructor
@View(members = "General[puntuacionMinima; puntuacionMaxima; percentil; nivel; diagnostico; descripcion; activo]")
@Tab(properties = "puntuacionMinima, puntuacionMaxima, percentil, nivel, diagnostico")
public class BaremoNormativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Required
    @Column(nullable = false)
    Integer puntuacionMinima;

    @Required
    @Column(nullable = false)
    Integer puntuacionMaxima;

    @Required
    @Column(nullable = false, length = 20)
    @DisplaySize(20)
    String percentil;

    @Required
    @Column(nullable = false, length = 50)
    @DisplaySize(30)
    String nivel;

    @Required
    @Column(nullable = false, length = 150)
    @DisplaySize(60)
    String diagnostico;

    @Stereotype("MEMO")
    @Column(length = 600)
    @DisplaySize(80)
    String descripcion;

    @Column(nullable = false)
    Boolean activo;

    @PrePersist
    public void inicializar() {
        if (activo == null) {
            activo = true;
        }
        validar();
    }

    @PreUpdate
    public void validar() {
        if (puntuacionMinima != null && puntuacionMaxima != null && puntuacionMinima > puntuacionMaxima) {
            throw new IllegalStateException("La puntuación mínima debe ser menor o igual que la máxima.");
        }
    }

    public boolean pertenece(Integer puntuacion) {
        if (puntuacion == null) return false;
        return puntuacion >= puntuacionMinima && puntuacion <= puntuacionMaxima;
    }

    @Override
    public String toString() {
        return percentil + " - " + diagnostico;
    }
}
