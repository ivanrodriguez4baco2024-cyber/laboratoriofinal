
// Capacidad de producir un hallazgo (severidad + mensaje) a partir de un dato y la hora del día
public interface GeneradorDeAlertas {
    
    Hallazgo evaluar(double dato, int horaDelDia);
}
