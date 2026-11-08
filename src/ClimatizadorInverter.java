// Es un climatizador, solo cambia el nivel mínimo (10) y el factor de consumo (80 %)
public class ClimatizadorInverter extends Climatizador {
    public ClimatizadorInverter(String codigo, Oficina oficina, double potenciaNominalW) {
        super(codigo, oficina, potenciaNominalW, 10);
    }

    protected double factorConsumo() {
        return 0.80;
    }

    protected String getNombreTipo() {
        return "Climatizador inverter";
    }
}
