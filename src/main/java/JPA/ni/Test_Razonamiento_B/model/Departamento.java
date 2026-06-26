package JPA.ni.Test_Razonamiento_B.model;

import lombok.Getter;

@Getter
public enum Departamento {
    BOACO("Boaco"),
    CARAZO("Carazo"),
    CHINANDEGA("Chinandega"),
    CHONTALES("Chontales"),
    ESTELI("Estelí"),
    GRANADA("Granada"),
    JINOTEGA("Jinotega"),
    LEON("León"),
    MADRIZ("Madriz"),
    MANAGUA("Managua"),
    MASAYA("Masaya"),
    MATAGALPA("Matagalpa"),
    NUEVA_SEGOVIA("Nueva Segovia"),
    RIVAS("Rivas"),
    RIO_SAN_JUAN("Río San Juan"),
    RACCN("Región Autónoma Costa Caribe Norte"),
    RACCS("Región Autónoma Costa Caribe Sur");

    private final String nombre;

    Departamento(String nombre) {
        this.nombre = nombre;
    }
}
