
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

//Alertas pendientes. Implementa Iterable, quien la recorre recibe las alertas en orden de urgencia sin conocer la estructura interna ni repetir el criterio (lo define Alerta.compareTo)
public class BandejaAlertas implements Iterable<Alerta> {
    private final PriorityQueue<Alerta> pendientes;

    public BandejaAlertas() {
        this.pendientes = new PriorityQueue<>();
    }

    public void agregar(Alerta alerta) {
        pendientes.add(alerta);
    }

    public boolean estaVacia() {
        return pendientes.isEmpty();
    }

    public Alerta retirarMasUrgente() {
        if (pendientes.isEmpty()) {
            throw new OperacionInvalidaException("La bandeja de alertas está vacía.");
        }
        return pendientes.poll();
    }

    // Recorre una copia ordenada, de solo lectura, no expone ni altera la cola interna
    public Iterator<Alerta> iterator() {
        List<Alerta> copia = new ArrayList<>(pendientes);
        Collections.sort(copia);
        return Collections.unmodifiableList(copia).iterator();
    }
}
