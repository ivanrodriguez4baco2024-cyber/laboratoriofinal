
// Contrato de una severidad de alerta. Cada severidad decide su plazo, crédito y urgencia
public interface Severidad {
    String getNombre();

    // Horas disponibles para atender la alerta sin generar crédito
    int getPlazoHoras();

    // Crédito en quetzales por cada hora de retraso
    double getCreditoPorHora();

    // Mayor valor = más urgente. Es lo que usa la bandeja para ordenar
    int getUrgencia();
}
