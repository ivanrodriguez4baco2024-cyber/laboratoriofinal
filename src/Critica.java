
public class Critica implements Severidad {
    
    public String getNombre() {
        return "Crítica";
    }

  
    public int getPlazoHoras() {
        return 1;
    }

    
    public double getCreditoPorHora() {
        return 5.00;
    }


    public int getUrgencia() {
        return 3;
    }
}
