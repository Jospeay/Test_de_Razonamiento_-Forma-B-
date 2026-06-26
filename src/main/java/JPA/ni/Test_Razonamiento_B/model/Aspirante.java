package JPA.ni.Test_Razonamiento_B.model;

import javax.persistence.*;
import org.openxava.annotations.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "aspirante")
@Getter @Setter
@NoArgsConstructor
@View(members =
    "Datos Generales[" +
    "   codigo;" +
    "   nombreCompleto;" +
    "   cedula;" +
    "   edad;" +
    "   sexo;" +
    "   departamento;" +
    "   municipio;" +
    "   zona;" +
    "   tipoColegio;" +
    "   correo;" +
    "   telefono;" +
    "   fechaRegistro;" +
    "   activo" +
    "];" +
    "Sesiones{sesiones}"
)
@Tab(properties = "codigo, nombreCompleto, cedula, edad, sexo, departamento, municipio, zona, tipoColegio, activo")
public class Aspirante {

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
    String nombreCompleto;

    @Required
    @Column(nullable = false, unique = true, length = 25)
    @DisplaySize(25)
    String cedula;

    @Required
    @Column(nullable = false)
    Integer edad;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Sexo sexo;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    Departamento departamento;

    @Required
    @Column(nullable = false, length = 100)
    @DisplaySize(40)
    String municipio;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Zona zona;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(name = "tipoColegio", nullable = false, length = 20)
    TipoColegio tipoColegio;

    @Column(length = 100)
    @DisplaySize(40)
    String correo;

    @Column(length = 30)
    @DisplaySize(20)
    String telefono;

    @Column(nullable = false)
    LocalDate fechaRegistro;

    @Column(nullable = false)
    Boolean activo;

    @OneToMany(mappedBy = "aspirante", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio DESC")
    @ListProperties("fechaInicio, estado, puntajeFinal")
    List<SesionTest> sesiones = new ArrayList<>();

    @PrePersist
    public void inicializar() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (tipoColegio == null) {
            tipoColegio = TipoColegio.PUBLICO;
        }
        // Generar un código secuencial simple si no existe
        if (codigo == null || codigo.trim().isEmpty()) {
            codigo = "ASP-" + (System.currentTimeMillis() % 1000000);
        }
    }

    @Override
    public String toString() {
        return codigo + " - " + nombreCompleto;
    }
}
