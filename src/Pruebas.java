
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

// Pruebas automáticas sin dependencias externas. Cada caso imprime: entrada/descripción, resultado esperado y resultado obtenido. Ejecutar esta clase y copiar la salida como evidencia en el PDF
public class Pruebas {
    private static int total = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {
        seccion("1. FÓRMULAS DE CONSUMO (kWh por hora)");
        pruebasConsumo();
        seccion("2. FRANJAS TARIFARIAS, CIERRE DE DÍA Y FACTURA");
        pruebasFacturacion();
        seccion("3. UMBRALES Y SEVERIDADES DE ALERTAS");
        pruebasUmbrales();
        seccion("4. ORDEN DE LA BANDEJA Y DESEMPATES");
        pruebasBandeja();
        seccion("5. PLAZOS Y CRÉDITOS POR RETRASO");
        pruebasPlazosYCreditos();
        seccion("6. OPERACIONES RECHAZADAS NO MODIFICAN EL ESTADO");
        pruebasRechazos();
        seccion("7. DATOS INICIALES DEL PROGRAMA");
        pruebasDatosIniciales();

        System.out.println();
        System.out.println("RESUMEN: " + (total - fallidas) + " de " + total + " casos correctos.");
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    // consumo

    private static void pruebasConsumo() {
        Oficina o = new Oficina("T", "Prueba");

        Luminaria l = new Luminaria("L", o, 100);
        l.encender();
        verificar("Luminaria 100 W, nivel 100 (inicial)", "0.100", kwh(l.consumoPorHoraKWh()));
        l.ajustarNivel(50);
        verificar("Luminaria 100 W, nivel 50", "0.050", kwh(l.consumoPorHoraKWh()));
        l.apagar();
        verificar("Luminaria conserva su nivel al apagarse (nivel 50)", "50", String.valueOf(l.getNivel()));
        verificar("Luminaria apagada: consumirHora() devuelve 0", "0.000", kwh(l.consumirHora()));

        Climatizador c = new Climatizador("C", o, 1500);
        verificar("Climatizador: nivel inicial", "50", String.valueOf(c.getNivel()));
        c.encender();
        c.ajustarNivel(60);
        verificar("Climatizador 1500 W, nivel 60 (ejemplo del enunciado)", "1.080", kwh(c.consumoPorHoraKWh()));
        c.ajustarNivel(20);
        verificar("Climatizador 1500 W, nivel 20 (mínimo)", "0.660", kwh(c.consumoPorHoraKWh()));

        ClimatizadorInverter ci = new ClimatizadorInverter("CI", o, 1500);
        verificar("Inverter: nivel inicial", "50", String.valueOf(ci.getNivel()));
        ci.encender();
        ci.ajustarNivel(60);
        verificar("Inverter 1500 W, nivel 60 (ejemplo del enunciado)", "0.864", kwh(ci.consumoPorHoraKWh()));
        ci.ajustarNivel(10);
        verificar("Inverter 1500 W, nivel 10 (permitido)", "0.444", kwh(ci.consumoPorHoraKWh()));
        verificar("Climatizador normal rechaza nivel 10", "rechazada", rechaza(() -> c.ajustarNivel(10)));
        verificar("Luminaria rechaza nivel 9", "rechazada", rechaza(() -> {
            Luminaria x = new Luminaria("X", o, 10);
            x.encender();
            x.ajustarNivel(9);
        }));

        SensorHumo s = new SensorHumo("S", o, 10);
        verificar("Sensor de humo 10 W", "0.010", kwh(s.consumoPorHoraKWh()));
        verificar("Sensor de humo inicia encendido", "true", String.valueOf(s.isEncendido()));

        CamaraSeguridad cam = new CamaraSeguridad("CAM", o, 100);
        verificar("Cámara inicia apagada", "false", String.valueOf(cam.isEncendido()));
        cam.consumirHora();
        verificar("Cámara apagada: consumo del día no cambia", "0.000", kwh(cam.getConsumoDiaKWh()));
        cam.encender();
        cam.consumirHora();
        cam.consumirHora();
        verificar("Cámara 100 W encendida 2 h: consumo del día", "0.200", kwh(cam.getConsumoDiaKWh()));
    }

    // facturación

    private static void pruebasFacturacion() {
        // Eclimatizador 1,500 W nivel 60 de 8:00 a 20:00 + alerta Alta generada en la hora 10 y atendida en la 17 (retraso 3 h = Q6).
        SistemaTorre s = new SistemaTorre();
        s.registrarOficina("T1", "Inquilino 1");
        Oficina o = s.getOficina("T1");
        s.registrarDispositivo(new Climatizador("C1", o, 1500));
        avanzar(s, 8);
        s.encender("C1");
        s.ajustarNivel("C1", 60);
        avanzar(s, 2);
        s.registrarLectura("C1", 35);
        avanzar(s, 7);
        s.atenderMasUrgente();
        avanzar(s, 3);
        s.apagar("C1");
        avanzar(s, 4);
        verificar("El día se cierra solo al pasar de la hora 23 a la 0 (facturas de la oficina)", "1", String.valueOf(o.getFacturas().size()));
        Factura f = o.getFacturas().get(0);
        verificar(" kWh franja normal (10 h)", "10.800", kwh(f.getKwh(Franja.NORMAL)));
        verificar(" kWh franja punta (2 h)", "2.160", kwh(f.getKwh(Franja.PUNTA)));
        verificar(" kWh franja valle", "0.000", kwh(f.getKwh(Franja.VALLE)));
        verificar(" monto normal", "Q15.12", Formato.q(f.getMonto(Franja.NORMAL)));
        verificar(" monto punta", "Q4.54", Formato.q(f.getMonto(Franja.PUNTA)));
        verificar(" suma de montos (19.656)", "Q19.66", Formato.q(f.getSubtotal()));
        verificar(" créditos (3 h x Q2)", "Q6.00", Formato.q(f.getCreditos()));
        verificar(" total a pagar (19.656 - 6)", "Q13.66", Formato.q(f.getTotal()));
        verificar("Tras el cierre: consumo del día del dispositivo vuelve a 0", "0.000", kwh(o.getDispositivosOrdenados().get(0).getConsumoDiaKWh()));
        verificar("Tras el cierre: créditos del día vuelven a 0", "Q0.00", Formato.q(o.getCreditosDia()));
        verificar("Tras el cierre: acumulado de la oficina vuelve a 0", "0.000", kwh(o.getKwhDia()));

        //Las tres franjas en un día completo, luminaria de 1,000 W encendida las 24 horas
        SistemaTorre s2 = new SistemaTorre();
        s2.registrarOficina("T2", "Inquilino 2");
        Oficina o2 = s2.getOficina("T2");
        s2.registrarDispositivo(new Luminaria("L1", o2, 1000));
        s2.encender("L1");
        avanzar(s2, 24);
        Factura f2 = o2.getFacturas().get(0);
        verificar("Valle 22:00-5:59 (8 h): kWh", "8.000", kwh(f2.getKwh(Franja.VALLE)));
        verificar("Normal 6:00-17:59 (12 h): kWh", "12.000", kwh(f2.getKwh(Franja.NORMAL)));
        verificar("Punta 18:00-21:59 (4 h): kWh", "4.000", kwh(f2.getKwh(Franja.PUNTA)));
        verificar("Monto valle (8 x 0.85)", "Q6.80", Formato.q(f2.getMonto(Franja.VALLE)));
        verificar("Monto normal (12 x 1.40)", "Q16.80", Formato.q(f2.getMonto(Franja.NORMAL)));
        verificar("Monto punta (4 x 2.10)", "Q8.40", Formato.q(f2.getMonto(Franja.PUNTA)));
        verificar("Total sin créditos", "Q32.00", Formato.q(f2.getTotal()));

        // Créditos que superan el monto el total no baja de cero y el exceso no se traslada.
        SistemaTorre s3 = new SistemaTorre();
        s3.registrarOficina("T3", "Inquilino 3");
        Oficina o3 = s3.getOficina("T3");
        s3.registrarDispositivo(new Climatizador("C3", o3, 1000));
        avanzar(s3, 6);
        s3.encender("C3");
        s3.registrarLectura("C3", 45);
        avanzar(s3, 4);
        s3.atenderMasUrgente();
        s3.apagar("C3");
        avanzar(s3, 14);
        Factura f3 = o3.getFacturas().get(0);
        verificar("Crédito > monto: monto bruto (4 h x 0.65 kWh x Q1.40)", "Q3.64", Formato.q(f3.getSubtotal()));
        verificar("Crédito > monto: crédito acumulado (3 h x Q5)", "Q15.00", Formato.q(f3.getCreditos()));
        verificar("Crédito > monto: créditos aplicados (solo hasta el monto)", "Q3.64", Formato.q(f3.getCreditosAplicados()));
        verificar("Crédito > monto: total a pagar nunca baja de cero", "Q0.00", Formato.q(f3.getTotal()));
        avanzar(s3, 24);
        verificar("El exceso de créditos no se traslada al día siguiente (créditos día 2)", "Q0.00", Formato.q(o3.getCreditosDia()));
    }

    // umbrales

   private static void pruebasUmbrales() {
    Oficina o = new Oficina("T", "Prueba");

    Climatizador c = new Climatizador("C", o, 1000);
    c.encender();
    double[] temps = {-10, 27.9, 28, 31.9, 32, 39.9, 40, 60};
    String[] esperadoC = {"ninguna", "ninguna", "Baja", "Baja", "Alta", "Alta", "Crítica", "Crítica"};

    for (int i = 0; i < temps.length; i++) {
        verificar("Climatizador lectura " + temps[i] + " °C", esperadoC[i], severidad(c.registrarLectura(temps[i], 12)));
    }

    String resultado = "aceptada";
    try {
        c.registrarLectura(60.1, 12);
    } catch (RuntimeException e) {
        resultado = "rechazada";
    }
    verificar("Climatizador lectura 60.1 °C (fuera de rango)", "rechazada", resultado);

    resultado = "aceptada";
    try {
        c.registrarLectura(-10.1, 12);
    } catch (RuntimeException e) {
        resultado = "rechazada";
    }
    verificar("Climatizador lectura -10.1 °C (fuera de rango)", "rechazada", resultado);

    ClimatizadorInverter ci = new ClimatizadorInverter("CI", o, 1000);
    ci.encender();
    verificar("Inverter hereda umbrales: 40 °C", "Crítica", severidad(ci.registrarLectura(40, 12)));

    SensorHumo s = new SensorHumo("S", o, 5);
    double[] ppm = {0, 149.9, 150, 299.9, 300, 1000};
    String[] esperadoS = {"ninguna", "ninguna", "Alta", "Alta", "Crítica", "Crítica"};

    for (int i = 0; i < ppm.length; i++) {
        verificar("Sensor de humo lectura " + ppm[i] + " ppm", esperadoS[i], severidad(s.registrarLectura(ppm[i], 12)));
    }

    resultado = "aceptada";
    try {
        s.registrarLectura(1000.1, 12);
    } catch (RuntimeException e) {
        resultado = "rechazada";
    }
    verificar("Sensor de humo 1000.1 ppm (fuera de rango)", "rechazada", resultado);

    resultado = "aceptada";
    try {
        s.registrarLectura(-1, 12);
    } catch (RuntimeException e) {
        resultado = "rechazada";
    }
    verificar("Sensor de humo -1 ppm (fuera de rango)", "rechazada", resultado);

    CamaraSeguridad cam = new CamaraSeguridad("CAM", o, 10);
    cam.encender();
    int[] horas = {0, 6, 7, 12, 18, 19, 23};
    String[] esperadoCam = {"Alta", "Alta", "ninguna", "ninguna", "ninguna", "Alta", "Alta"};

    for (int i = 0; i < horas.length; i++) {
        verificar("Cámara: movimiento a las " + horas[i] + ":00", esperadoCam[i], severidad(cam.registrarMovimiento(horas[i])));
    }
}

    // bandeja

    private static void pruebasBandeja() {
        Oficina o = new Oficina("T", "Prueba");
        CamaraSeguridad d = new CamaraSeguridad("CAM", o, 10);
        BandejaAlertas b = new BandejaAlertas();
        // código, hora de generación, severidad
        b.agregar(new Alerta(1, d, 1, new Hallazgo(new Alta(), "a")));
        b.agregar(new Alerta(2, d, 1, new Hallazgo(new Alta(), "b")));
        b.agregar(new Alerta(3, d, 5, new Hallazgo(new Critica(), "c")));
        b.agregar(new Alerta(4, d, 0, new Hallazgo(new Baja(), "d")));
        b.agregar(new Alerta(5, d, 0, new Hallazgo(new Alta(), "e")));
        b.agregar(new Alerta(6, d, 5, new Hallazgo(new Critica(), "f")));

        verificar("Orden: crítica(c3,h5), crítica(c6,h5), alta h0, alta h1 c1, alta h1 c2, baja", "3,6,5,1,2,4", codigos(b));
        verificar("Recorrer la bandeja dos veces da el mismo orden (no consume)", "3,6,5,1,2,4", codigos(b));
        verificar("Recorrer no vacía la bandeja", "false", String.valueOf(b.estaVacia()));
        String intentoRemove;
        try {
            Iterator<Alerta> it = b.iterator();
            it.next();
            it.remove();
            intentoRemove = "aceptada";
        } catch (UnsupportedOperationException e) {
            intentoRemove = "rechazada";
        }
        verificar("El iterador es de solo lectura (remove rechazado)", "rechazada", intentoRemove);
        verificar("Tras el intento de remove la bandeja sigue igual", "3,6,5,1,2,4", codigos(b));
        String orden = "";
        while (!b.estaVacia()) {
            orden += b.retirarMasUrgente().getCodigo() + ",";
        }
        verificar("retirarMasUrgente() extrae en el mismo orden", "3,6,5,1,2,4,", orden);
        verificar("Atender con bandeja vacía se rechaza", "rechazada", rechaza(b::retirarMasUrgente));
    }

    // plazos y créditos

    private static void pruebasPlazosYCreditos() {
    Oficina o = new Oficina("T", "Prueba");
    CamaraSeguridad d = new CamaraSeguridad("CAM", o, 10);

    verificar("Plazos: Baja / Alta / Crítica (horas)", "24/4/1", new Baja().getPlazoHoras() + "/" + new Alta().getPlazoHoras()  + "/" + new Critica().getPlazoHoras());

    verificar("Créditos por hora: Baja / Alta / Crítica", "Q0.50/Q2.00/Q5.00",  Formato.q(new Baja().getCreditoPorHora()) + "/" + Formato.q(new Alta().getCreditoPorHora()) + "/" + Formato.q(new Critica().getCreditoPorHora()));

    verificar("Baja generada h0, atendida h24 (dentro del plazo): retraso/crédito", "0 h / Q0.00", atender(new Baja(), d, 0, 24));

    verificar("Baja generada h0, atendida h30: retraso/crédito","6 h / Q3.00", atender(new Baja(), d, 0, 30));

    verificar("Alta generada h10, atendida h14 (justo en el plazo)","0 h / Q0.00", atender(new Alta(), d, 10, 14));

    verificar("Alta generada h10, atendida h17 (ejemplo del enunciado)", "3 h / Q6.00", atender(new Alta(), d, 10, 17));

    verificar("Crítica generada h0, atendida h1 (justo en el plazo)","0 h / Q0.00", atender(new Critica(), d, 0, 1));

    verificar("Crítica generada h0, atendida h4","3 h / Q15.00", atender(new Critica(), d, 0, 4));

    String resultado = "aceptada";
    try {
        Alerta a = new Alerta(1, d, 0, new Hallazgo(new Baja(), "x"));
        a.atender(1);
        a.atender(2);
    } catch (RuntimeException e) {
        resultado = "rechazada";
    }

    verificar("Atender dos veces la misma alerta se rechaza", "rechazada", resultado);
}

    // rechazos sin efectos

    private static void pruebasRechazos() {
    SistemaTorre s = new SistemaTorre();
    DatosIniciales.cargar(s);
    s.encender("L-001");
    s.encender("C-001");
    s.encender("CAM-001");
    avanzar(s, 2);
    s.registrarLectura("C-001", 35); // deja una alerta pendiente para comparar el estado

    String antes = foto(s);

    probarRechazo(s, antes, "Apagar el sensor de humo", 1);
    probarRechazo(s, antes, "Ajustar nivel de una luminaria apagada", 2);
    probarRechazo(s, antes, "Nivel 5 en luminaria (mín. 10)", 3);
    probarRechazo(s, antes, "Nivel 101 en luminaria (máx. 100)", 4);
    probarRechazo(s, antes, "Nivel 19 en climatizador (mín. 20)", 5);
    probarRechazo(s, antes, "Ajustar nivel a un dispositivo no regulable", 6);
    probarRechazo(s, antes, "Ajustar nivel de un inverter apagado", 7);
    probarRechazo(s, antes, "Lectura en dispositivo que no mide (luminaria)", 8);
    probarRechazo(s, antes, "Lectura en cámara (no mide)", 9);
    probarRechazo(s, antes, "Lectura en climatizador apagado", 10);
    probarRechazo(s, antes, "Lectura fuera de rango (70 °C)", 11);
    probarRechazo(s, antes, "Lectura negativa en sensor de humo", 12);
    probarRechazo(s, antes, "Movimiento en dispositivo que no es cámara", 13);
    probarRechazo(s, antes, "Movimiento en cámara apagada", 14);
    probarRechazo(s, antes, "Encender un dispositivo ya encendido", 15);
    probarRechazo(s, antes, "Apagar un dispositivo ya apagado", 16);
    probarRechazo(s, antes, "Dispositivo inexistente", 17);
    probarRechazo(s, antes, "Oficina con código repetido", 18);
    probarRechazo(s, antes, "Oficina con código vacío", 19);
    probarRechazo(s, antes, "Oficina con inquilino vacío", 20);
    probarRechazo(s, antes, "Dispositivo con código repetido", 21);
    probarRechazo(s, antes, "Dispositivo con código vacío", 22);
    probarRechazo(s, antes, "Dispositivo con potencia 0", 23);
    probarRechazo(s, antes, "Dispositivo con potencia negativa", 24);
    probarRechazo(s, antes, "Dispositivo en oficina inexistente", 25);

    SistemaTorre vacio = new SistemaTorre();
    DatosIniciales.cargar(vacio);
    probarRechazo(vacio, foto(vacio),
            "Atender con bandeja vacía: se rechaza y no cambia nada", 26);
}

private static void probarRechazo(
        SistemaTorre s, String antes, String nombre, int operacion) {

    String resultado = "aceptada";

    try {
        switch (operacion) {
            case 1:
                s.apagar("S-001");
                break;
            case 2:
                s.ajustarNivel("L-002", 50);
                break;
            case 3:
                s.ajustarNivel("L-001", 5);
                break;
            case 4:
                s.ajustarNivel("L-001", 101);
                break;
            case 5:
                s.ajustarNivel("C-001", 19);
                break;
            case 6:
                s.ajustarNivel("CAM-001", 50);
                break;
            case 7:
                s.ajustarNivel("CI-002", 30);
                break;
            case 8:
                s.registrarLectura("L-001", 10);
                break;
            case 9:
                s.registrarLectura("CAM-001", 10);
                break;
            case 10:
                s.registrarLectura("C-002", 35);
                break;
            case 11:
                s.registrarLectura("C-001", 70);
                break;
            case 12:
                s.registrarLectura("S-001", -5);
                break;
            case 13:
                s.registrarMovimiento("C-001");
                break;
            case 14:
                s.registrarMovimiento("CAM-002");
                break;
            case 15:
                s.encender("L-001");
                break;
            case 16:
                s.apagar("L-002");
                break;
            case 17:
                s.encender("NO-EXISTE");
                break;
            case 18:
                s.registrarOficina("O-101", "Otro");
                break;
            case 19:
                s.registrarOficina("", "Otro");
                break;
            case 20:
                s.registrarOficina("O-999", "");
                break;
            case 21:
                s.registrarDispositivo(
                        new Luminaria("L-001", s.getOficina("O-101"), 50));
                break;
            case 22:
                s.registrarDispositivo(
                        new Luminaria("", s.getOficina("O-101"), 50));
                break;
            case 23:
                s.registrarDispositivo(
                        new Luminaria("L-900", s.getOficina("O-101"), 0));
                break;
            case 24:
                s.registrarDispositivo(
                        new CamaraSeguridad("CAM-900", s.getOficina("O-101"), -5));
                break;
            case 25:
                s.registrarDispositivo(
                        new Luminaria("L-901", s.getOficina("NO-EXISTE"), 50));
                break;
            case 26:
                s.atenderMasUrgente();
                break;
            default:
                throw new IllegalArgumentException("Operación desconocida: " + operacion);
        }
    } catch (RuntimeException e) {
        resultado = "rechazada";
    }

    String despues = foto(s);
    String resultadoFinal;

    if (resultado.equals("rechazada") && antes.equals(despues)) {
        resultadoFinal = "rechazada y sin cambios";
    } else {
        resultadoFinal = resultado
                + (antes.equals(despues) ? "" : " pero el estado CAMBIÓ");
    }

    verificar("Rechazo + estado intacto: " + nombre,
            "rechazada y sin cambios", resultadoFinal);
}

    //  datos iniciales

    private static void pruebasDatosIniciales() {
        SistemaTorre s = new SistemaTorre();
        DatosIniciales.cargar(s);
        verificar("Hora de simulación inicial", "0", String.valueOf(s.getHoraSimulacion()));
        verificar("Oficinas cargadas (mínimo 3)", "true", String.valueOf(s.getOficinas().size() >= 3));
        int lum = 0, cli = 0, inv = 0, hum = 0, cam = 0;
        for (Dispositivo d : s.getDispositivos()) {
            if (d.getClass() == Luminaria.class) lum++;
            if (d.getClass() == Climatizador.class) cli++;
            if (d.getClass() == ClimatizadorInverter.class) inv++;
            if (d.getClass() == SensorHumo.class) hum++;
            if (d.getClass() == CamaraSeguridad.class) cam++;
        }
        verificar("Al menos 2 dispositivos de cada tipo (lum/cli/inv/hum/cam)", "true",String.valueOf(lum >= 2 && cli >= 2 && inv >= 2 && hum >= 2 && cam >= 2));
        verificar("Hay un inverter con nivel menor que 20", "true", String.valueOf(s.consultarDispositivos().contains("nivel 15%")));
        double total = 0;
        for (Dispositivo d : s.getDispositivos()) {
            total += d.getConsumoDiaKWh();
        }
        verificar("Sin consumos iniciales", "0.000", kwh(total));
        verificar("Sin alertas iniciales", "true", String.valueOf(s.reporteBandeja().contains("bandeja vacía")));
        verificar("Sin facturas iniciales", "true", String.valueOf(s.reporteFacturas().contains("aún no")));
    }

    // utilidades

    private static void seccion(String titulo) {
        System.out.println();
        System.out.println("=== " + titulo + " ===");
    }

    private static void verificar(String caso, String esperado, String obtenido) {
    total++;
    boolean ok = esperado.equals(obtenido);

    if (!ok) {
        fallidas++;
    }

    String resultado;
    if (ok) {
        resultado = "[OK]    ";
    } else {
        resultado = "[FALLA] ";
    }

    System.out.println(resultado + caso + "\n          esperado: " + esperado
            + " | obtenido: " + obtenido);
}

    // Ejecuta la operación y dice si fue rechazada con OperacionInvalidaException
    private static String rechaza(Runnable operacion) {
        try {
            operacion.run();
            return "aceptada";
        } catch (OperacionInvalidaException e) {
            return "rechazada";
        }
    }

    private static void agregar(List<String> nombres, List<Runnable> ops, String nombre, Runnable op) {
        nombres.add(nombre);
        ops.add(op);
    }

    private static String kwh(double valor) {
        return String.format(Locale.US, "%.3f", valor);
    }

    private static String severidad(Hallazgo h) {
    if (h != null) {
        return h.getSeveridad().getNombre();
    } else {
        return "ninguna";
    }
}

    private static void avanzar(SistemaTorre s, int horas) {
        for (int i = 0; i < horas; i++) {
            s.avanzarHora();
        }
    }

    private static String codigos(BandejaAlertas b) {
        StringBuilder sb = new StringBuilder();
        for (Alerta a : b) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(a.getCodigo());
        }
        return sb.toString();
    }

    private static String atender(Severidad sev, Dispositivo d, int generada, int atendida) {
        Alerta a = new Alerta(1, d, generada, new Hallazgo(sev, "prueba"));
        a.atender(atendida);
        return a.getHorasRetraso() + " h / " + Formato.q(a.getCredito());
    }

    // Fotografía del estado observable: consumos, alertas, créditos, facturas y hora
    private static String foto(SistemaTorre s) {
        return s.getHoraSimulacion() + "\n" + s.consultarOficinas() + s.consultarDispositivos() + s.consultarAlertas() + s.reporteFacturas();
    }
}
