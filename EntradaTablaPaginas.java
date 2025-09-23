public class EntradaTablaPaginas {
    private int numMarco = -1; // -1 indica que no está asignado
    private boolean valida = false;
    private long contadorLRU = 0L;

    // --- Getters y Setters para cada atributo ---
    public int getNumMarco() { return numMarco; }
    public void setNumMarco(int numMarco) { this.numMarco = numMarco; }
    public boolean esValida() { return valida; }
    public void setValida(boolean valida) { this.valida = valida; }
    public long getContadorLRU() { return contadorLRU; }
    public void setContadorLRU(long contadorLRU) { this.contadorLRU = contadorLRU; }
}