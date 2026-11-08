
import java.util.Locale;

// Siempre encendido; mide humo en ppm y genera alertas
public class SensorHumo extends Dispositivo implements Medible, GeneradorDeAlertas {
    private static final double UMBRAL_ALTA = 150;
    private static final double UMBRAL_CRITICA = 300;

    public SensorHumo(String codigo, Oficina oficina, double potenciaNominalW) {
        super(codigo, oficina, potenciaNominalW, true);
    }

    public void apagar() {
        throw new OperacionInvalidaException("El sensor de humo " + getCodigo() + " no se puede apagar.");
    }

    public double consumoPorHoraKWh() {
        return getPotenciaNominalW() / 1000.0;
    }

    public Hallazgo registrarLectura(double valor, int horaDelDia) {
        return evaluar(valor, horaDelDia);
    }

    public Hallazgo evaluar(double dato, int horaDelDia) {
        exigirEncendido();
        validarLectura(dato);
        String texto = String.format(Locale.US, "Concentración de humo de %.1f ppm", dato);
        if (dato >= UMBRAL_CRITICA) {
            return new Hallazgo(new Critica(), texto + " (crítica).");
        }
        if (dato >= UMBRAL_ALTA) {
            return new Hallazgo(new Alta(), texto + " (alta).");
        }
        return null;
    }


    public String getUnidad() {
        return "ppm";
    }

    public double getLecturaMinima() {
        return 0;
    }

    public double getLecturaMaxima() {
        return 1000;
    }

    public String describir() {
        return descripcionBase("Sensor de humo");
    }
}
