

//Datos de arranque (hora 0, sin consumos, alertas ni facturas). Se cargan con la misma API pública del sistema
public final class DatosIniciales {
    private DatosIniciales() {
    }

    public static void cargar(SistemaTorre s) {
        s.registrarOficina("O-101", "Bufete Alfa");
        s.registrarOficina("O-102", "Café Central");
        s.registrarOficina("O-201", "Estudio Beta");

        Oficina o101 = s.getOficina("O-101");
        Oficina o102 = s.getOficina("O-102");
        Oficina o201 = s.getOficina("O-201");

        s.registrarDispositivo(new Luminaria("L-001", o101, 1000));
        s.registrarDispositivo(new Luminaria("L-002", o102, 120));

        s.registrarDispositivo(new Climatizador("C-001", o101, 1500));
        s.registrarDispositivo(new Climatizador("C-002", o201, 2000));

        s.registrarDispositivo(new ClimatizadorInverter("CI-001", o102, 1800));
        s.registrarDispositivo(new ClimatizadorInverter("CI-002", o201, 1200));

        s.registrarDispositivo(new SensorHumo("S-001", o101, 5));
        s.registrarDispositivo(new SensorHumo("S-002", o201, 8));

        s.registrarDispositivo(new CamaraSeguridad("CAM-001", o102, 15));
        s.registrarDispositivo(new CamaraSeguridad("CAM-002", o201, 20));

        // Un inverter con nivel menor que 20 (el nivel se conserva mientras está apagado)
        s.encender("CI-001");
        s.ajustarNivel("CI-001", 15);
        s.apagar("CI-001");
    }
}
