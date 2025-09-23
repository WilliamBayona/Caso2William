import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Proceso {
    private int id;
    private ArrayList<String> referencias;
    private int ptrInstruccion = 0;
    private Map<Integer, EntradaTablaPaginas> tablaPaginas;
    private List<Integer> marcosAsignados;
    private int fallosDePagina = 0;
    private int hitsDePagina = 0; // Reintroducido para el logging detallado
    private int accesosASwap = 0;

    public Proceso(int id) {
        this.id = id;
        this.referencias = new ArrayList<>();
        this.tablaPaginas = new HashMap<>();
        this.marcosAsignados = new ArrayList<>();
    }

    public void cargarDesdeArchivo(String nombreArchivo) throws IOException {
        System.out.println("PROC " + id + " == Leyendo archivo de configuración ==");
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            int contadorCabecera = 0;
            while ((linea = br.readLine()) != null) {
                if (contadorCabecera < 5) {
                    String[] partes = linea.split("=");
                    String clave = partes[0];
                    String valor = partes[1];
                    switch (clave) {
                        case "TP":
                            System.out.println("PROC " + id + " leyendo TP. Tam Páginas: " + valor);
                            break;
                        case "NF":
                            System.out.println("PROC " + id + " leyendo NF. Num Filas: " + valor);
                            break;
                        case "NC":
                            System.out.println("PROC " + id + " leyendo NC. Num Cols: " + valor);
                            break;
                        case "NR":
                            System.out.println("PROC " + id + " leyendo NR. Num Referencias: " + valor);
                            break;
                        case "NP":
                            System.out.println("PROC " + id + " leyendo NP. Num Paginas: " + valor);
                            int numPaginasVirtuales = Integer.parseInt(valor);
                            for (int i = 0; i < numPaginasVirtuales; i++) {
                                tablaPaginas.put(i, new EntradaTablaPaginas());
                            }
                            break;
                    }
                    contadorCabecera++;
                } else {
                    this.referencias.add(linea);
                }
            }
        }
        System.out.println("PROC " + id + " == Terminó de leer archivo de configuración ==");
    }

    public void actualizarLRU(int paginaAccedida) {
        tablaPaginas.values().stream()
                .filter(EntradaTablaPaginas::esValida)
                .forEach(e -> e.setContadorLRU(e.getContadorLRU() >> 1));
        EntradaTablaPaginas entradaAccedida = tablaPaginas.get(paginaAccedida);
        long mascara = 1L << 63;
        entradaAccedida.setContadorLRU(entradaAccedida.getContadorLRU() | mascara);
    }

    public int encontrarVictimaLRU() {
        long minContador = Long.MAX_VALUE;
        int paginaVictima = -1;
        for (Map.Entry<Integer, EntradaTablaPaginas> entry : tablaPaginas.entrySet()) {
            EntradaTablaPaginas pte = entry.getValue();
            if (pte.esValida() && pte.getContadorLRU() < minContador) {
                minContador = pte.getContadorLRU();
                paginaVictima = entry.getKey();
            }
        }
        return paginaVictima;
    }

    public boolean haTerminado() { return ptrInstruccion >= referencias.size(); }
    public String getSiguienteReferencia() { return referencias.get(ptrInstruccion); }
    public void avanzarInstruccion() { this.ptrInstruccion++; }
    public void registrarHit() { this.hitsDePagina++; }
    public void registrarFallo() { this.fallosDePagina++; }
    public void registrarAccesoSwap(int cantidad) { this.accesosASwap += cantidad; }
    public void agregarMarcoAsignado(int numMarco) { this.marcosAsignados.add(numMarco); }
    public void agregarMarcosAsignados(List<Integer> marcos) { this.marcosAsignados.addAll(marcos); }
    public List<Integer> liberarMarcos() {
        List<Integer> marcosLiberados = new ArrayList<>(this.marcosAsignados);
        this.marcosAsignados.clear();
        return marcosLiberados;
    }

    public int getId() { return id; }
    public int getPtrInstruccion() { return ptrInstruccion; }
    public int getFallosDePagina() { return fallosDePagina; }
    public int getHitsDePagina() { return hitsDePagina; }
    public int getAccesosASwap() { return accesosASwap; }
    public int getTotalReferencias() { return referencias.size(); }
    public Map<Integer, EntradaTablaPaginas> getTablaPaginas() { return tablaPaginas; }
    public List<Integer> getMarcosAsignados() { return marcosAsignados; }
}