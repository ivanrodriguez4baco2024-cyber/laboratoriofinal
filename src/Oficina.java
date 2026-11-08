
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

// Oficina de una torre: acumula consumo por franja y créditos del día, y conserva sus facturas
public class Oficina {
    private final String codigo;
    private final String inquilino;
    private final List<Dispositivo> dispositivos = new ArrayList<>();
    private double[] kwhPorFranja; // indexado con Franja.ordinal()
    private double creditosDia;
    private final List<Factura> facturas = new ArrayList<>();

    public Oficina(String codigo, String inquilino) {
        if (codigo == null || codigo.isBlank()) {
            throw new OperacionInvalidaException("El código de la oficina no puede estar vacío.");
        }
        if (inquilino == null || inquilino.isBlank()) {
            throw new OperacionInvalidaException("El inquilino no puede estar vacío.");
        }
        this.codigo = codigo;
        this.inquilino = inquilino;
        reiniciarAcumulados();
    }

    public void agregarDispositivo(Dispositivo d) {
        if (d.getOficina() != this) {
            throw new OperacionInvalidaException("El dispositivo " + d.getCodigo()+ " pertenece a otra oficina.");
        }
        if (dispositivos.contains(d)) {
            throw new OperacionInvalidaException("El dispositivo " + d.getCodigo() + " ya está en la oficina.");
        }
        dispositivos.add(d);
    }

    public void acumular(Franja franja, double kwh) {
        if (kwh < 0) {
            throw new OperacionInvalidaException("El consumo no puede ser negativo.");
        }
        kwhPorFranja[franja.ordinal()] += kwh;
    }

    public void acreditar(double monto) {
        if (monto < 0) {
            throw new OperacionInvalidaException("El crédito no puede ser negativo.");
        }
        creditosDia += monto;
    }

    // Genera la factura del día, la guarda y reinicia acumulados y créditos
    public Factura cerrarDia(int dia) {
        Factura factura = new Factura(codigo, inquilino, dia, kwhPorFranja, creditosDia);
        facturas.add(factura);
        reiniciarAcumulados();
        return factura;
    }

    private void reiniciarAcumulados() {
        kwhPorFranja = new double[Franja.values().length];
        creditosDia = 0;
    }

    // Copia ordenada por código: la lista interna no se expone
    public List<Dispositivo> getDispositivosOrdenados() {
        List<Dispositivo> copia = new ArrayList<>(dispositivos);
        copia.sort(Comparator.comparing(Dispositivo::getCodigo));
        return copia;
    }

    public List<Factura> getFacturas() {
        return Collections.unmodifiableList(new ArrayList<>(facturas));
    }

    public double getKwhDia() {
        double suma = 0;
        for (double k : kwhPorFranja) {
            suma += k;
        }
        return suma;
    }

    public double getCreditosDia() {
        return creditosDia;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getInquilino() {
        return inquilino;
    }

    public String toString() {
        return "Oficina " + codigo + " (" + inquilino + ") | dispositivos: " + dispositivos.size()  + " | consumo acumulado del día: " + Formato.kwh(getKwhDia()) + " | créditos del día: " + Formato.q(creditosDia);
    }
}
