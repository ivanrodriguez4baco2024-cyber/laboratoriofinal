
// No mide, el personal registra movimientos. Alerta Alta si es antes de las 7:00 o desde las 19:00
public class CamaraSeguridad extends Dispositivo implements GeneradorDeAlertas {
    private static final int HORA_INICIO_LABORAL = 7;
    private static final int HORA_FIN_LABORAL = 19;

    public CamaraSeguridad(String codigo, Oficina oficina, double potenciaNominalW) {
        super(codigo, oficina, potenciaNominalW, false);
    }

    public double consumoPorHoraKWh() {
        return getPotenciaNominalW() / 1000.0;
    }

    public Hallazgo registrarMovimiento(int horaDelDia) {
        return evaluar(0, horaDelDia);
    }

    public Hallazgo evaluar(double dato, int horaDelDia) {
        exigirEncendido();
        if (horaDelDia < HORA_INICIO_LABORAL || horaDelDia >= HORA_FIN_LABORAL) {
            return new Hallazgo(new Alta(), "Movimiento detectado fuera del horario laboral (" + horaDelDia + ":00).");
        }
        return null;
    }
    public String describir() {
        return descripcionBase("Cámara de seguridad");
    }
}
