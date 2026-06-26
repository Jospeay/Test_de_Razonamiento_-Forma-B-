package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor
public class OpcionRespuesta {

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    LetraRespuesta letra;

    @Required
    @Column(nullable = false, length = 500)
    @DisplaySize(70)
    String texto;

    @Required
    @Column(nullable = false)
    Integer orden;

    @Column(nullable = false)
    Boolean esCorrecta;

    @Override
    public String toString() {
        return letra + " - " + texto;
    }
}
