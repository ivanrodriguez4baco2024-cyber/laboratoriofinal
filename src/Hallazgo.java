
// Resultado de evaluar un dato, la severidad alcanzada y el mensaje de la alerta
public class Hallazgo {
    private final Severidad severidad;
    private final String mensaje;

    public Hallazgo(Severidad severidad, String mensaje) {
        this.severidad = severidad;
        this.mensaje = mensaje;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public String getMensaje() {
        return mensaje;
    }
}
