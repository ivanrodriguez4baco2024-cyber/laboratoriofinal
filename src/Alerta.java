
//Alerta generada por un dispositivo. Implementa Comparable, aqui y solo aquí se define el orden de urgencia (mayor severidad, luego hora de generación menor, luego código menor
public class Alerta implements Comparable<Alerta> {
    private final int codigo;
    private final Dispositivo dispositivo;
    private final int horaGeneracion;
    private final Severidad severidad;
    private final String mensaje;
    private boolean atendida;
    private int horasRetraso;

    public Alerta(int codigo, Dispositivo dispositivo, int horaGeneracion, Hallazgo hallazgo) {
        this.codigo = codigo;
        this.dispositivo = dispositivo;
        this.horaGeneracion = horaGeneracion;
        this.severidad = hallazgo.getSeveridad();
        this.mensaje = hallazgo.getMensaje();
        this.atendida = false;
        this.horasRetraso = 0;
    }

    // Pasa a atendida y calcula las horas completas de retraso (nunca negativas)
    public void atender(int horaActual) {
        if (atendida) {
            throw new OperacionInvalidaException("La alerta " + codigo + " ya fue atendida.");
        }
        horasRetraso = Math.max(0, horaActual - horaGeneracion - severidad.getPlazoHoras());
        atendida = true;
    }

    // Crédito a favor de la oficina: horas de retraso por el crédito de su severidad
    public double getCredito() {
        return horasRetraso * severidad.getCreditoPorHora();
    }

    public int compareTo(Alerta otra) {
  
        if (this.severidad.getUrgencia() > otra.severidad.getUrgencia()) {
            return -1;
        }  
        if (this.severidad.getUrgencia() < otra.severidad.getUrgencia()) {
            return 1;
        }
   
        if (this.horaGeneracion < otra.horaGeneracion) {
            return -1;
        }
        if (this.horaGeneracion > otra.horaGeneracion) {
            return 1;
        }

        if (this.codigo < otra.codigo) {
            return -1;
        }
        if (this.codigo > otra.codigo) {
            return 1;
        }
        return 0;
    }

    public String toString() {
    String estado;

    if (atendida) {
        estado = "ATENDIDA (retraso " + horasRetraso
                + " h, crédito " + Formato.q(getCredito()) + ")";
    } else {
        estado = "PENDIENTE";
    }

    return "Alerta #" + codigo + " [" + severidad.getNombre() + "] hora " + horaGeneracion + " | dispositivo " + dispositivo.getCodigo() + " (oficina "  + dispositivo.getOficina().getCodigo() + ") | " + mensaje + " | " + estado;
}

    public int getCodigo() {
        return codigo;
    }

    public Dispositivo getDispositivo() {
        return dispositivo;
    }

    public int getHoraGeneracion() {
        return horaGeneracion;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public int getHorasRetraso() {
        return horasRetraso;
    }
}
