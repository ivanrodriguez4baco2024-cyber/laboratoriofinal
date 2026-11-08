
// Regulable, nivel 10-100 (inicia en 100). No mide ni genera alertas
public class Luminaria extends DispositivoRegulable {
    public Luminaria(String codigo, Oficina oficina, double potenciaNominalW) {
        super(codigo, oficina, potenciaNominalW, 10, 100);
    }

    public double consumoPorHoraKWh() {
        return getPotenciaNominalW() * factorNivel() / 1000.0;
    }

    public String describir() {
        return descripcionBase("Luminaria") + " | nivel " + getNivel() + "%";
    }
}
