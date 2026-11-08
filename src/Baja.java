
public class Baja implements Severidad {

    public String getNombre() {
        return "Baja";
    }

    public int getPlazoHoras() {
        return 24;
    }

    public double getCreditoPorHora() {
        return 0.50;
    }

    public int getUrgencia() {
        return 1;
    }
}
