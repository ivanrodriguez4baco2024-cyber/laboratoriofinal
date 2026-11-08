
import java.util.Locale;

// Utilidades de presentación, montos y kWh siempre con punto decimal
public final class Formato {
    private Formato() {
    }

    public static String q(double monto) {
        return String.format(Locale.US, "Q%.2f", monto);
    }

    public static String kwh(double valor) {
        return String.format(Locale.US, "%.3f kWh", valor);
    }
}
