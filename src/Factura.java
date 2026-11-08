
/** Factura inmutable de una oficina para un día cerrado. Los arreglos se indexan con Franja.ordinal(). */
public class Factura {
    private final String codigoOficina;
    private final String inquilino;
    private final int dia;
    private final double[] kwh;
    private final double[] monto;
    private final double subtotal;
    private final double creditos;
    private final double creditosAplicados;
    private final double total;

    public Factura(String codigoOficina, String inquilino, int dia, double[] kwhPorFranja, double creditos) {
        this.codigoOficina = codigoOficina;
        this.inquilino = inquilino;
        this.dia = dia;
        Franja[] franjas = Franja.values();
        this.kwh = new double[franjas.length];
        this.monto = new double[franjas.length];
        double suma = 0;

        for (int i = 0; i < franjas.length; i++) {
            kwh[i] = kwhPorFranja[i];
            monto[i] = kwh[i] * franjas[i].getTarifaPorKWh();
            suma += monto[i];
        }

        this.subtotal = suma;
        this.creditos = creditos;
        this.creditosAplicados = Math.min(creditos, suma);
        // El total nunca baja de cero, el exceso de créditos no se traslada.
        this.total = redondear(Math.max(0, suma - creditos));
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public String getCodigoOficina() {
        return codigoOficina;
    }

    public int getDia() {
        return dia;
    }

    public double getKwh(Franja f) {
        return kwh[f.ordinal()];
    }

    public double getMonto(Franja f) {
        return monto[f.ordinal()];
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getCreditos() {
        return creditos;
    }

    public double getCreditosAplicados() {
        return creditosAplicados;
    }

    public double getTotal() {
        return total;
    }

    public String toString() {
    String resultado = "Factura día " + dia + " | Oficina " + codigoOficina + " (" + inquilino + ")\n";

    for (Franja f : Franja.values()) {
        resultado += "   " + f.getNombre() + ": " + Formato.kwh(getKwh(f)) + " -> " + Formato.q(getMonto(f)) + "\n";
    }

    resultado += "   Créditos aplicados: " + Formato.q(creditosAplicados)+ " (acumulados: " + Formato.q(creditos) + ")\n";
    resultado += "   TOTAL A PAGAR: " + Formato.q(total);

    return resultado;
}
}
