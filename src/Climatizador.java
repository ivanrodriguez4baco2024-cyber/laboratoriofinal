
import java.util.Locale;

// Regulable (nivel 20-100, inicia en 50) que mide temperatura y genera alertas
public class Climatizador extends DispositivoRegulable implements Medible, GeneradorDeAlertas {
    private static final double UMBRAL_BAJA = 28;
    private static final double UMBRAL_ALTA = 32;
    private static final double UMBRAL_CRITICA = 40;

    public Climatizador(String codigo, Oficina oficina, double potenciaNominalW) {
        this(codigo, oficina, potenciaNominalW, 20);
    }

    // Permite que una subclase (el inverter) cambie solo el nivel mínimo
    public Climatizador(String codigo, Oficina oficina, double potenciaNominalW, int nivelMinimo) {
        super(codigo, oficina, potenciaNominalW, nivelMinimo, 50);
    }

    public double consumoPorHoraKWh() {
        return getPotenciaNominalW() * (0.30 + 0.70 * factorNivel()) * factorConsumo() / 1000.0;
    }

    // Punto de variación, el inverter lo sobrescribe sin duplicar la fórmula
    protected double factorConsumo() {
        return 1.0;
    }

    protected String getNombreTipo() {
        return "Climatizador";
    }

    public Hallazgo registrarLectura(double valor, int horaDelDia) {
        return evaluar(valor, horaDelDia);
    }

    public Hallazgo evaluar(double dato, int horaDelDia) {
        exigirEncendido();
        validarLectura(dato);
        String texto = String.format(Locale.US, "Temperatura de %.1f °C", dato);
        if (dato >= UMBRAL_CRITICA) {
            return new Hallazgo(new Critica(), texto + " (crítica).");
        }
        if (dato >= UMBRAL_ALTA) {
            return new Hallazgo(new Alta(), texto + " (alta).");
        }
        if (dato >= UMBRAL_BAJA) {
            return new Hallazgo(new Baja(), texto + " (baja).");
        }
        return null;
    }

    public String getUnidad() {
        return "°C";
    }

    public double getLecturaMinima() {
        return -10;
    }

    public double getLecturaMaxima() {
        return 60;
    }

    public String describir() {
        return descripcionBase(getNombreTipo()) + " | nivel " + getNivel() + "% (mín. " + getNivelMinimo() + ")";
    }
}
