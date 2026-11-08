
//Se lanza cuando una operación no cumple sus condiciones de validez. Toda validación se hace ANTES de modificar el estado, de modo que una operación rechazada nunca altera consumos, alertas ni créditos

public class OperacionInvalidaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
