
// Franjas tarifarias. La tarifa vive aquí para no repetirla en la facturación
public enum Franja {
    VALLE("Valle", 0.85),
    NORMAL("Normal", 1.40),
    PUNTA("Punta", 2.10);

    private final String nombre;
    private final double tarifaPorKWh;

    Franja(String nombre, double tarifaPorKWh) {
        this.nombre = nombre;
        this.tarifaPorKWh = tarifaPorKWh;
    }

    public String getNombre() {
        return nombre;
    }

    public double getTarifaPorKWh() {
        return tarifaPorKWh;
    }

    // Valle 22:00-5:59, Normal 6:00-17:59, Punta 18:00-21:59
    public static Franja deHora(int horaDelDia) {
        if (horaDelDia >= 22 || horaDelDia < 6) {
            return VALLE;
        }
        if (horaDelDia >= 18) {
            return PUNTA;
        }
        return NORMAL;
    }
}
