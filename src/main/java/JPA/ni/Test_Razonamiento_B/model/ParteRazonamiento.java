package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parte_razonamiento")
@Getter @Setter
@NoArgsConstructor
@View(members = "General[codigoParte; nombreParte; descripcion; numeroOrden; activa]; Items{items}")
@Tab(properties = "codigoParte, nombreParte, numeroOrden, activa")
public class ParteRazonamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Required
    @Column(nullable = false, unique = true, length = 15)
    @DisplaySize(15)
    String codigoParte;

    @Required
    @Column(nullable = false, length = 100)
    @DisplaySize(40)
    String nombreParte;

    @Stereotype("MEMO")
    @Column(length = 500)
    @DisplaySize(70)
    String descripcion;

    @Required
    @Column(nullable = false)
    Integer numeroOrden;

    @Column(nullable = false)
    Boolean activa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prueba_id", nullable = false)
    @DescriptionsList(descriptionProperties = "nombre")
    @NoFrame
    PruebaRazonamiento prueba;

    @OneToMany(mappedBy = "parte", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    @ListProperties("numero, tipoPregunta, nivel")
    List<ItemRazonamiento> items = new ArrayList<>();

    @PrePersist
    public void inicializar() {
        if (activa == null) {
            activa = true;
        }
    }

    @Override
    public String toString() {
        return codigoParte + " - " + nombreParte;
    }
}
