package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "item_razonamiento")
@Getter @Setter
@NoArgsConstructor
@View(members = "Información[numero; tipoPregunta; nivel; enunciado; activo]; Opciones{opciones}")
@Tab(properties = "numero, tipoPregunta, nivel, activo")
public class ItemRazonamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Required
    @Column(nullable = false, unique = true)
    Integer numero;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    TipoPregunta tipoPregunta;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    NivelPregunta nivel;

    @Required
    @Stereotype("MEMO")
    @Column(nullable = false, length = 1000)
    @DisplaySize(80)
    String enunciado;

    @Column(nullable = false)
    Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parte_id", nullable = false)
    @DescriptionsList(descriptionProperties = "nombreParte")
    @NoFrame
    ParteRazonamiento parte;

    @ElementCollection
    @ListProperties("letra, texto, orden, esCorrecta")
    @OrderBy("orden ASC")
    List<OpcionRespuesta> opciones = new ArrayList<>();

    @PrePersist
    public void inicializar() {
        if (activo == null) {
            activo = true;
        }
        validarOpciones();
    }

    @PreUpdate
    public void validarOpciones() {
        if (opciones != null && !opciones.isEmpty()) {
            long correctas = opciones.stream()
                .filter(o -> Boolean.TRUE.equals(o.esCorrecta))
                .count();
            if (correctas > 1) {
                throw new IllegalStateException("Cada pregunta debe tener como máximo una respuesta correcta.");
            }
        }
    }

    @Override
    public String toString() {
        return String.valueOf(numero);
    }
}
