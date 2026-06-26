package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prueba_razonamiento")
@Getter @Setter
@NoArgsConstructor
@View(members = "Información[codigo; nombre; descripcion; duracionSegundos; numeroPreguntas; fechaCreacion; activa]; Partes{partes}")
@Tab(properties = "codigo, nombre, duracionSegundos, numeroPreguntas, activa")
public class PruebaRazonamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Required
    @Column(nullable = false, unique = true, length = 20)
    @DisplaySize(20)
    String codigo;

    @Required
    @Column(nullable = false, length = 150)
    @DisplaySize(60)
    String nombre;

    @Stereotype("MEMO")
    @Column(length = 1000)
    @DisplaySize(80)
    String descripcion;

    @Required
    @Column(nullable = false)
    Integer duracionSegundos;

    @Required
    @Column(nullable = false)
    Integer numeroPreguntas;

    @Column(nullable = false)
    LocalDate fechaCreacion;

    @Column(nullable = false)
    Boolean activa;

    @OneToMany(mappedBy = "prueba", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroOrden ASC")
    @ListProperties("codigoParte, nombreParte, numeroOrden")
    List<ParteRazonamiento> partes = new ArrayList<>();

    @PrePersist
    public void inicializar() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDate.now();
        }
        if (duracionSegundos == null) {
            duracionSegundos = 720; // 12 minutos
        }
        if (numeroPreguntas == null) {
            numeroPreguntas = 30;
        }
        if (activa == null) {
            activa = true;
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
