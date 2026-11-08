
// Concentra el nivel (y su validación) de todos los dispositivos regulables
public abstract class DispositivoRegulable extends Dispositivo implements Regulable {
    private int nivel;
    private final int nivelMinimo;
    private final int nivelMaximo;

    protected DispositivoRegulable(String codigo, Oficina oficina, double potenciaNominalW, int nivelMinimo, int nivelInicial) {
        super(codigo, oficina, potenciaNominalW, false);
        if (nivelMinimo < 1 || nivelMinimo > 100) {
            throw new OperacionInvalidaException("El nivel mínimo debe estar entre 1 y 100.");
        }
        this.nivelMinimo = nivelMinimo;
        this.nivelMaximo = 100;
        validarNivel(nivelInicial);
        this.nivel = nivelInicial;
    }

    public void ajustarNivel(int nuevoNivel) {
        exigirEncendido();
        validarNivel(nuevoNivel);
        this.nivel = nuevoNivel;
    }

    private void validarNivel(int valor) {
        if (valor < nivelMinimo || valor > nivelMaximo) {
            throw new OperacionInvalidaException("Nivel fuera de rango: debe estar entre " + nivelMinimo + " y " + nivelMaximo + ".");
        }
    }

    // nivel / 100
    protected double factorNivel() {
        return nivel / 100.0;
    }

    public int getNivel() {
        return nivel;
    }


    public int getNivelMinimo() {
        return nivelMinimo;
    }

    public int getNivelMaximo() {
        return nivelMaximo;
    }
}
