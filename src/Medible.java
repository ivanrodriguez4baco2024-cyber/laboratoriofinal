
import java.util.Locale;

// Capacidad de medir una magnitud dentro de un rango válido
public interface Medible {
    String getUnidad();

    double getLecturaMinima();

    double getLecturaMaxima();

    // Validación común del rango, evita repetirla en cada dispositivo que mide
   default void validarLectura(double valor) {
        if (Double.isNaN(valor) || valor < getLecturaMinima() || valor > getLecturaMaxima()) {
            throw new OperacionInvalidaException(String.format(Locale.US, "Lectura fuera de rango: debe estar entre %.0f y %.0f %s.", getLecturaMinima(), getLecturaMaxima(), getUnidad()));
        }
    }
}
