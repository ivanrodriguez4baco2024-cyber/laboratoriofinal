
import java.util.Locale;

//Clase abstracta con los datos comunes de todo dispositivo. Los métodos ajustarNivel, registrarLectura y registrarMovimiento rechazan por defecto ("no es regulable", "no mide", "no es una cámara"), cada subclase que tiene esa capacidad los sobrescribe. Así SistemaTorre nunca verifica el tipo del objeto
public abstract class Dispositivo {
    private final String codigo;
    private final Oficina oficina;
    private final double potenciaNominalW;
    private boolean encendido;
    private double consumoDiaKWh;

    protected Dispositivo(String codigo, Oficina oficina, double potenciaNominalW, boolean encendido) {
        if (codigo == null || codigo.isBlank()) {
            throw new OperacionInvalidaException("El código del dispositivo no puede estar vacío.");
        }
        if (oficina == null) {
            throw new OperacionInvalidaException("El dispositivo debe instalarse en una oficina.");
        }
        if (potenciaNominalW <= 0) {
            throw new OperacionInvalidaException("La potencia nominal debe ser un número mayor que cero.");
        }
        this.codigo = codigo;
        this.oficina = oficina;
        this.potenciaNominalW = potenciaNominalW;
        this.encendido = encendido;
        this.consumoDiaKWh = 0;
    }

    public void encender() {
        if (encendido) {
            throw new OperacionInvalidaException("El dispositivo " + codigo + " ya está encendido.");
        }
        encendido = true;
    }

    public void apagar() {
        if (!encendido) {
            throw new OperacionInvalidaException("El dispositivo " + codigo + " ya está apagado.");
        }
        encendido = false;
    }

    // Consumo de una hora según el estado actual. Es final: la regla de apagado no se puede saltar
    public final double consumirHora() {
        if (!encendido) {
            return 0;
        }
        double kwh = consumoPorHoraKWh();
        consumoDiaKWh += kwh;
        return kwh;
    }

    // Fórmula de consumo propia de cada tipo, en kWh por hora encendido
    public abstract double consumoPorHoraKWh();

    // Texto para consultas y reportes
    public abstract String describir();

    public void ajustarNivel(int nivel) {
        throw new OperacionInvalidaException("El dispositivo " + codigo + " no es regulable");
    }

    public Hallazgo registrarLectura(double valor, int horaDelDia) {
        throw new OperacionInvalidaException("El dispositivo " + codigo + " no mide.");
    }

    public Hallazgo registrarMovimiento(int horaDelDia) {
        throw new OperacionInvalidaException("El dispositivo " + codigo + " no es una cámara.");
    }

    protected void exigirEncendido() {
        if (!encendido) {
            throw new OperacionInvalidaException("El dispositivo " + codigo + " está apagado.");
        }
    }

    public void reiniciarConsumoDia() {
        consumoDiaKWh = 0;
    }

    // Parte común del texto descriptivo, las subclases añaden sus datos propios
    protected String descripcionBase(String tipo) {
    String estado;

    if (encendido) {
        estado = "ENCENDIDO";
    } else {
        estado = "APAGADO";
    }

    return String.format(
        Locale.US,
        "[%s] %s | Oficina %s | %.1f W | %s | consumo del día: %s",
        codigo,
        tipo,
        oficina.getCodigo(),
        potenciaNominalW,
        estado,
        Formato.kwh(consumoDiaKWh)
    );
}

    public String getCodigo() {
        return codigo;
    }

    public Oficina getOficina() {
        return oficina;
    }

    public double getPotenciaNominalW() {
        return potenciaNominalW;
    }

    public boolean isEncendido() {
        return encendido;
    }

    public double getConsumoDiaKWh() {
        return consumoDiaKWh;
    }
}
