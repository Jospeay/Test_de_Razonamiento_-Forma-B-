package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalDateTime;

@Embeddable
@Getter @Setter
@NoArgsConstructor
public class RespuestaUsuario {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @DescriptionsList(descriptionProperties = "numero")
    ItemRazonamiento item;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    LetraRespuesta opcionSeleccionada; // Puede ser null si el aspirante deja en blanco la respuesta

    @Column(nullable = false)
    Boolean correcta;

    @Column(nullable = false)
    Integer tiempoRespuesta; // Tiempo acumulado en segundos al responder

    @Column(nullable = false)
    LocalDateTime fechaRespuesta;

    @Override
    public String toString() {
        return "Pregunta " + (item != null ? item.getNumero() : "?") + " - " + (opcionSeleccionada != null ? opcionSeleccionada : "En blanco");
    }
}
