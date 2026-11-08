
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//Coordina la simulación. Trabaja solo contra las abstracciones (Dispositivo, Severidad, Iterable), no conoce tipos concretos de dispositivo ni de severidad. Toda validación ocurre antes de modificar estado
public class SistemaTorre {
    private int horaSimulacion;
    private final List<Oficina> oficinas = new ArrayList<>();
    private final List<Dispositivo> dispositivos = new ArrayList<>();
    private final BandejaAlertas bandeja = new BandejaAlertas();
    private final List<Alerta> atendidas = new ArrayList<>();
    private int siguienteCodigoAlerta = 1;

    public SistemaTorre() {
        this.horaSimulacion = 0;
    }

    //Registro

    public void registrarOficina(String codigo, String inquilino) {
        Oficina nueva = new Oficina(codigo, inquilino); // valida vacíos
        if (buscarOficina(codigo) != null) {
            throw new OperacionInvalidaException("Ya existe una oficina con el código " + codigo + ".");
        }
        oficinas.add(nueva);
    }

    public void registrarDispositivo(Dispositivo d) {
        if (d == null) {
            throw new OperacionInvalidaException("Dispositivo inválido.");
        }
        if (buscarDispositivoOpcional(d.getCodigo()) != null) {
            throw new OperacionInvalidaException("Ya existe un dispositivo con el código " + d.getCodigo() + ".");
        }
        if (!oficinas.contains(d.getOficina())) {
            throw new OperacionInvalidaException("La oficina del dispositivo no está registrada.");
        }
        d.getOficina().agregarDispositivo(d);
        dispositivos.add(d);
    }

    // Comandos 

    public void encender(String codigo) {
        buscarDispositivo(codigo).encender();
    }

    public void apagar(String codigo) {
        buscarDispositivo(codigo).apagar();
    }

    public void ajustarNivel(String codigo, int nivel) {
        buscarDispositivo(codigo).ajustarNivel(nivel);
    }

    //la alerta generada, o null si la lectura no alcanzó ningún umbral
    public Alerta registrarLectura(String codigo, double valor) {
        Dispositivo d = buscarDispositivo(codigo);
        return crearAlerta(d, d.registrarLectura(valor, getHoraDelDia()));
    }

    public Alerta registrarMovimiento(String codigo) {
        Dispositivo d = buscarDispositivo(codigo);
        return crearAlerta(d, d.registrarMovimiento(getHoraDelDia()));
    }

    private Alerta crearAlerta(Dispositivo d, Hallazgo hallazgo) {
        if (hallazgo == null) {
            return null;
        }
        Alerta alerta = new Alerta(siguienteCodigoAlerta++, d, horaSimulacion, hallazgo);
        bandeja.agregar(alerta);
        return alerta;
    }

    // Atiende la alerta más urgente y acredita el retraso a la oficina del dispositivo
    public Alerta atenderMasUrgente() {
        Alerta alerta = bandeja.retirarMasUrgente(); // rechaza si está vacía
        alerta.atender(horaSimulacion);
        double credito = alerta.getCredito();
        if (credito > 0) {
            alerta.getDispositivo().getOficina().acreditar(credito);
        }
        atendidas.add(alerta);
        return alerta;
    }

    //Avanza una hora, cada dispositivo consume según su estado actual y se acumula a su oficina en la franja de la hora que termina. Si pasa de la hora 23 a la 0, cierra el día. Retorna las facturas generadas (vacío si no se cerró el día)
     
    public List<Factura> avanzarHora() {
        Franja franja = Franja.deHora(getHoraDelDia());
        for (Dispositivo d : dispositivos) {
            double kwh = d.consumirHora();
            if (kwh > 0) {
                d.getOficina().acumular(franja, kwh);
            }
        }
        horaSimulacion++;
        if (getHoraDelDia() == 0) {
            return cerrarDia();
        }
        return Collections.emptyList();
    }

    private List<Factura> cerrarDia() {
        int dia = horaSimulacion / 24;
        List<Factura> generadas = new ArrayList<>();
        for (Oficina o : oficinas) {
            generadas.add(o.cerrarDia(dia));
        }
        for (Dispositivo d : dispositivos) {
            d.reiniciarConsumoDia();
        }
        return generadas;
    }

    // Consultas

   public String consultarOficinas() {
    if (oficinas.isEmpty()) {
        return "No hay oficinas registradas.";
    }

    String resultado = "";
    for (Oficina o : oficinas) {
        resultado += o + "\n";
    }

    return resultado;
}

public String consultarDispositivos() {
    if (dispositivos.isEmpty()) {
        return "No hay dispositivos registrados.";
    }

    String resultado = "";
    for (Dispositivo d : dispositivos) {
        resultado += d.describir() + "\n";
    }

    return resultado;
}

public String consultarAlertas() {
    String resultado = "Pendientes (en orden de urgencia):\n";
    boolean hay = false;

    for (Alerta a : bandeja) {
        resultado += "  " + a + "\n";
        hay = true;
    }

    if (!hay) {
        resultado += "  (ninguna)\n";
    }

    resultado += "Atendidas:\n";

    if (atendidas.isEmpty()) {
        resultado += "  (ninguna)\n";
    }

    for (Alerta a : atendidas) {
        resultado += "  " + a + "\n";
    }

    return resultado;
}

    // ---------- Reportes ----------

public String reporteDispositivosDeOficina(String codigoOficina) {
    Oficina o = getOficina(codigoOficina);
    String resultado = "Dispositivos de la oficina " + o.getCodigo() + " (" + o.getInquilino() + "), ordenados por código:\n";

    List<Dispositivo> lista = o.getDispositivosOrdenados();

    if (lista.isEmpty()) {
        resultado += "  (sin dispositivos)\n";
    }

    for (Dispositivo d : lista) {
        resultado += "  " + d.describir() + "\n";
    }

    return resultado;
}

public String reporteBandeja() {
    String resultado = "Bandeja de alertas en orden de urgencia:\n";
    boolean hay = false;

    for (Alerta a : bandeja) {
        resultado += "  " + a + "\n";
        hay = true;
    }

    if (!hay) {
        resultado += "  (bandeja vacía)\n";
    }

    return resultado;
}

public String reporteAtendidasConRetraso() {
    String resultado = "Alertas atendidas con retraso:\n";
    boolean hay = false;

    for (Alerta a : atendidas) {
        if (a.getHorasRetraso() > 0) {
            resultado += "  " + a + "\n";
            hay = true;
        }
    }

    if (!hay) {
        resultado += "  (ninguna)\n";
    }

    return resultado;
}

public String reporteFacturas() {
    String resultado = "Facturas generadas:\n";
    double[] totalKwh = new double[Franja.values().length];
    double[] totalMonto = new double[Franja.values().length];
    double creditosAplicados = 0;
    boolean hay = false;

    for (Oficina o : oficinas) {
        for (Factura f : o.getFacturas()) {
            resultado += f + "\n";

            for (Franja fr : Franja.values()) {
                totalKwh[fr.ordinal()] += f.getKwh(fr);
                totalMonto[fr.ordinal()] += f.getMonto(fr);
            }

            creditosAplicados += f.getCreditosAplicados();
            hay = true;
        }
    }

    if (!hay) {
        resultado += "  (aún no se ha cerrado ningún día)\n";
        return resultado;
    }

    resultado += "Totales facturados por franja:\n";

    for (Franja fr : Franja.values()) {
        resultado += "  " + fr.getNombre() + ": " + Formato.kwh(totalKwh[fr.ordinal()])  + " -> " + Formato.q(totalMonto[fr.ordinal()]) + "\n";
    }

    resultado += "Créditos aplicados en total: " + Formato.q(creditosAplicados) + "\n";

    return resultado;
}

    //  Acceso 

    public Oficina getOficina(String codigo) {
        Oficina o = buscarOficina(codigo);
        if (o == null) {
            throw new OperacionInvalidaException("No existe la oficina " + codigo + ".");
        }
        return o;
    }

    private Oficina buscarOficina(String codigo) {
        for (Oficina o : oficinas) {
            if (o.getCodigo().equals(codigo)) {
                return o;
            }
        }
        return null;
    }

    private Dispositivo buscarDispositivoOpcional(String codigo) {
        for (Dispositivo d : dispositivos) {
            if (d.getCodigo().equals(codigo)) {
                return d;
            }
        }
        return null;
    }

    private Dispositivo buscarDispositivo(String codigo) {
        Dispositivo d = buscarDispositivoOpcional(codigo);
        if (d == null) {
            throw new OperacionInvalidaException("No existe el dispositivo " + codigo + ".");
        }
        return d;
    }

    public Collection<Oficina> getOficinas() {
        return Collections.unmodifiableList(oficinas);
    }

    public Collection<Dispositivo> getDispositivos() {
        return Collections.unmodifiableList(dispositivos);
    }

    public int getHoraSimulacion() {
        return horaSimulacion;
    }

    public int getHoraDelDia() {
        return horaSimulacion % 24;
    }
}
