package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultado_evaluacion")
@Getter @Setter
@NoArgsConstructor
@View(members =
    "General[" +
    "   sesion;" +
    "   respuestasCorrectas;" +
    "   respuestasIncorrectas;" +
    "   preguntasContestadas;" +
    "   puntaje;" +
    "   porcentajeAcierto;" +
    "   percentil;" +
    "   diagnostico;" +
    "   fechaCalculo" +
    "];" +
    "Baremo[baremo]"
)
@Tab(properties = "sesion.aspirante.nombreCompleto, puntaje, percentil, diagnostico")
public class ResultadoEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sesion_id", nullable = false)
    @NoFrame
    SesionTest sesion;

    @Column(nullable = false)
    Integer respuestasCorrectas;

    @Column(nullable = false)
    Integer respuestasIncorrectas;

    @Column(nullable = false)
    Integer preguntasContestadas;

    @Column(nullable = false)
    Integer puntaje;

    @Column(nullable = false)
    Double porcentajeAcierto;

    @Column(nullable = false, length = 20)
    String percentil;

    @Column(nullable = false, length = 150)
    @DisplaySize(60)
    String diagnostico;

    @Column(nullable = false)
    LocalDateTime fechaCalculo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "baremo_id")
    @DescriptionsList(descriptionProperties = "diagnostico")
    @NoFrame
    BaremoNormativo baremo;

    @PrePersist
    public void inicializar() {
        if (fechaCalculo == null) {
            fechaCalculo = LocalDateTime.now();
        }
        if (respuestasCorrectas == null) respuestasCorrectas = 0;
        if (respuestasIncorrectas == null) respuestasIncorrectas = 0;
        if (preguntasContestadas == null) preguntasContestadas = 0;
        if (puntaje == null) puntaje = 0;
        if (porcentajeAcierto == null) porcentajeAcierto = 0.0;
    }

    @Override
    public String toString() {
        return (sesion != null && sesion.getAspirante() != null ? sesion.getAspirante().getNombreCompleto() : "?") + " - " + puntaje + " pts";
    }
}
