package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sesion_test")
@Getter @Setter
@NoArgsConstructor
@View(members =
    "General[" +
    "   aspirante;" +
    "   prueba;" +
    "   fechaInicio;" +
    "   fechaFin;" +
    "   estado;" +
    "   duracionTotal;" +
    "   tiempoRestante;" +
    "   puntajeFinal;" +
    "   preguntasRespondidas;" +
    "   preguntasCorrectas;" +
    "   preguntasIncorrectas" +
    "];" +
    "Respuestas{respuestas};" +
    "Resultado{resultado}"
)
@Tab(properties = "aspirante.nombreCompleto, prueba.nombre, estado, puntajeFinal, fechaInicio")
public class SesionTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aspirante_id", nullable = false)
    @DescriptionsList(descriptionProperties = "nombreCompleto")
    @NoFrame
    Aspirante aspirante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prueba_id", nullable = false)
    @DescriptionsList(descriptionProperties = "nombre")
    @NoFrame
    PruebaRazonamiento prueba;

    @Column(nullable = false)
    LocalDateTime fechaInicio;

    @Column(nullable = true)
    LocalDateTime fechaFin;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EstadoSesion estado;

    @Column(nullable = false)
    Integer duracionTotal; // en segundos, por defecto 720 (12 min)

    @Column(nullable = false)
    Integer tiempoRestante; // en segundos

    @Column(nullable = false)
    Integer puntajeFinal;

    @Column(nullable = false)
    Integer preguntasRespondidas;

    @Column(nullable = false)
    Integer preguntasCorrectas;

    @Column(nullable = false)
    Integer preguntasIncorrectas;

    @ElementCollection
    @ListProperties("item.numero, opcionSeleccionada, correcta, tiempoRespuesta")
    List<RespuestaUsuario> respuestas = new ArrayList<>();

    @OneToOne(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    ResultadoEvaluacion resultado;

    @PrePersist
    public void inicializar() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoSesion.EN_PROCESO;
        }
        if (duracionTotal == null) {
            duracionTotal = 720;
        }
        if (tiempoRestante == null) {
            tiempoRestante = 720;
        }
        if (puntajeFinal == null) {
            puntajeFinal = 0;
        }
        if (preguntasRespondidas == null) {
            preguntasRespondidas = 0;
        }
        if (preguntasCorrectas == null) {
            preguntasCorrectas = 0;
        }
        if (preguntasIncorrectas == null) {
            preguntasIncorrectas = 0;
        }
    }

    @Override
    public String toString() {
        return (aspirante != null ? aspirante.getNombreCompleto() : "?") + " - " + (fechaInicio != null ? fechaInicio.toLocalDate() : "?");
    }
}
