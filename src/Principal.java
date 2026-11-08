
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

// Clase con el método main, menú de consola de TorreGT
public class Principal {
    private static final Scanner teclado = new Scanner(System.in).useLocale(Locale.US);
    private static final SistemaTorre sistema = new SistemaTorre();

    public static void main(String[] args) {
        DatosIniciales.cargar(sistema);
        System.out.println("TorreGT: gestión de torres de oficinas inteligentes");
        boolean salir = false;
        try {
            while (!salir) {
                mostrarMenu();
                int opcion = leerEntero("Opción: ");
                try {
                    salir = ejecutar(opcion);
                } catch (OperacionInvalidaException e) {
                    System.out.println("Operación rechazada: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nSe cerró la entrada. Fin del programa.");
        }
        System.out.println("Hasta luego.");
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("Hora de simulación: " + sistema.getHoraSimulacion()+ " (hora del día " + sistema.getHoraDelDia() + ":00) ---");
        System.out.println(" 1. Registrar oficina");
        System.out.println(" 2. Registrar dispositivo");
        System.out.println(" 3. Encender dispositivo");
        System.out.println(" 4. Apagar dispositivo");
        System.out.println(" 5. Ajustar nivel de un dispositivo");
        System.out.println(" 6. Registrar lectura");
        System.out.println(" 7. Registrar movimiento detectado");
        System.out.println(" 8. Avanzar una hora de simulación");
        System.out.println(" 9. Atender la alerta más urgente");
        System.out.println("10. Consultar oficinas");
        System.out.println("11. Consultar dispositivos");
        System.out.println("12. Consultar alertas");
        System.out.println("13. Reportes");
        System.out.println(" 0. Salir");
    }

    // true si el usuario decidió salir
    private static boolean ejecutar(int opcion) {
        switch (opcion) {
            case 1:
                registrarOficina();
                break;
            case 2:
                registrarDispositivo();
                break;
            case 3:
                sistema.encender(leerTexto("Código del dispositivo: "));
                System.out.println("Dispositivo encendido.");
                break;
            case 4:
                sistema.apagar(leerTexto("Código del dispositivo: "));
                System.out.println("Dispositivo apagado.");
                break;
            case 5:
                ajustarNivel();
                break;
            case 6:
                registrarLectura();
                break;
            case 7:
                registrarMovimiento();
                break;
            case 8:
                avanzarHora();
                break;
            case 9:
                System.out.println("Atendida: " + sistema.atenderMasUrgente());
                break;
            case 10:
                System.out.print(sistema.consultarOficinas());
                break;
            case 11:
                System.out.print(sistema.consultarDispositivos());
                break;
            case 12:
                System.out.print(sistema.consultarAlertas());
                break;
            case 13:
                menuReportes();
                break;
            case 0:
                return true;
            default:
                System.out.println("Opción no válida.");
        }
        return false;
    }

    private static void registrarOficina() {
        String codigo = leerTexto("Código de la oficina: ");
        String inquilino = leerTexto("Nombre del inquilino: ");
        sistema.registrarOficina(codigo, inquilino);
        System.out.println("Oficina registrada.");
    }

    private static void registrarDispositivo() {
        System.out.println("Tipos: 1 Luminaria | 2 Climatizador | 3 Climatizador inverter | 4 Sensor de humo | 5 Cámara");
        int tipo = leerEntero("Tipo: ");
        if (tipo < 1 || tipo > 5) {
            throw new OperacionInvalidaException("Tipo de dispositivo no válido.");
        }
        String codigo = leerTexto("Código del dispositivo: ");
        Oficina oficina = sistema.getOficina(leerTexto("Código de la oficina: "));
        double potencia = leerDecimal("Potencia nominal (W): ");

        Dispositivo d;
        switch (tipo) {
            case 1:
                d = new Luminaria(codigo, oficina, potencia);
                break;
            case 2:
                d = new Climatizador(codigo, oficina, potencia);
                break;
            case 3:
                d = new ClimatizadorInverter(codigo, oficina, potencia);
                break;
            case 4:
                d = new SensorHumo(codigo, oficina, potencia);
                break;
            default:
                d = new CamaraSeguridad(codigo, oficina, potencia);
        }
        sistema.registrarDispositivo(d);
        System.out.println("Dispositivo registrado.");
    }

    private static void ajustarNivel() {
        String codigo = leerTexto("Código del dispositivo: ");
        int nivel = leerEntero("Nuevo nivel (%): ");
        sistema.ajustarNivel(codigo, nivel);
        System.out.println("Nivel ajustado.");
    }

    private static void registrarLectura() {
        String codigo = leerTexto("Código del dispositivo: ");
        double valor = leerDecimal("Valor de la lectura: ");
        Alerta alerta = sistema.registrarLectura(codigo, valor);
        mostrarResultadoAlerta(alerta, "Lectura registrada.");
    }

    private static void registrarMovimiento() {
        String codigo = leerTexto("Código de la cámara: ");
        Alerta alerta = sistema.registrarMovimiento(codigo);
        mostrarResultadoAlerta(alerta, "Movimiento registrado dentro del horario laboral: no genera alerta.");
    }

    private static void mostrarResultadoAlerta(Alerta alerta, String sinAlerta) {
        if (alerta != null) {
            System.out.println("Se generó: " + alerta);
        } else {
            System.out.println(sinAlerta + " No alcanzó ningún umbral.");
        }
    }

    private static void avanzarHora() {
        List<Factura> facturas = sistema.avanzarHora();
        System.out.println("Hora avanzada. Ahora es la hora " + sistema.getHoraSimulacion() + ".");
        if (!facturas.isEmpty()) {
            System.out.println("*** Se cerró el día. Facturas generadas: ***");
            for (Factura f : facturas) {
                System.out.println(f);
            }
        }
    }

    private static void menuReportes() {
        System.out.println("Reportes: 1 Dispositivos de una oficina | 2 Bandeja de alertas" + " | 3 Atendidas con retraso | 4 Facturas, totales por franja y créditos");
        int opcion = leerEntero("Reporte: ");
        switch (opcion) {
            case 1:
                System.out.print(sistema.reporteDispositivosDeOficina(leerTexto("Código de la oficina: ")));
                break;
            case 2:
                System.out.print(sistema.reporteBandeja());
                break;
            case 3:
                System.out.print(sistema.reporteAtendidasConRetraso());
                break;
            case 4:
                System.out.print(sistema.reporteFacturas());
                break;
            default:
                System.out.println("Reporte no válido.");
        }
    }

    //Lectura segura: nextInt/nextDouble + nextLine, sin cerrar el programa

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                int valor = teclado.nextInt();
                teclado.nextLine();
                return valor;
            } catch (InputMismatchException e) {
                teclado.nextLine();
                System.out.println("Entrada inválida: escriba un número entero.");
            }
        }
    }

    private static double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                double valor = teclado.nextDouble();
                teclado.nextLine();
                return valor;
            } catch (InputMismatchException e) {
                teclado.nextLine();
                System.out.println("Entrada inválida: escriba un número (use punto decimal).");
            }
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine();
    }
}
