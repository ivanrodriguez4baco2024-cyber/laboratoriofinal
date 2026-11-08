
// Capacidad de funcionar a un nivel de intensidad (porcentaje)
public interface Regulable {

    // void: es un comando que cambia el estado (el nivel). Si es inválido, lanza OperacionInvalidaException en lugar de devolver un valor, así el estado no cambia
    void ajustarNivel(int nivel);

    int getNivel();

    int getNivelMinimo();

    int getNivelMaximo();
}
