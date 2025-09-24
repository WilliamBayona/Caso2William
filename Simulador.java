import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;
import java.util.Set;
import java.util.Comparator;

public class Simulador {
    private int numMarcosTotales;
    private int numProcesos;
    private Queue<Proceso> colaProcesos;
    private List<Proceso> listaProcesos;

    public Simulador(int numMarcos, int numProcesos) {
        this.numMarcosTotales = numMarcos;
        this.numProcesos = numProcesos;
        this.colaProcesos = new LinkedList<>();
        this.listaProcesos = new ArrayList<>();
    }

    public void iniciar() {
        System.out.println("Inicio:");
        for (int i = 0; i < numProcesos; i++) {
            Proceso p = new Proceso(i);
            try {
                p.cargarDesdeArchivo("proc" + i + ".txt");
                listaProcesos.add(p);
                colaProcesos.add(p);
            } catch (IOException e) {
                System.err.println(" No se pudo leer el archivo proc" + i + ".txt");
                return;
            }
        }

        int marcosPorProceso = numMarcosTotales / numProcesos;
        int marcoActual = 0;
        for (Proceso p : listaProcesos) {
            for (int i = 0; i < marcosPorProceso; i++) {
                System.out.println("Proceso " + p.getId() + ": recibe marco " + marcoActual);
                p.agregarMarcoAsignado(marcoActual++);
            }
        }
    }

    public void simular() {
        System.out.println("Simulación:");
        while (!colaProcesos.isEmpty()) {
            Proceso actual = colaProcesos.poll();

            System.out.println("Turno proc: " + actual.getId());
            System.out.println("PROC " + actual.getId() + " analizando linea_: " + actual.getPtrInstruccion());

            int paginaVirtual = Integer.parseInt(actual.getSiguienteReferencia().split(",")[1]);
            EntradaTablaPaginas pte = actual.getTablaPaginas().get(paginaVirtual);

            if (pte.esValida()) {
                actual.registrarHit();
                System.out.println("PROC " + actual.getId() + " hits: " + actual.getHitsDePagina());
                actual.avanzarInstruccion();
            } else {
                actual.registrarFallo();
                System.out.println("PROC " + actual.getId() + " falla de pag: " + actual.getFallosDePagina());
                manejarFalloDePagina(actual, paginaVirtual);
            }

            actual.actualizarLRU(paginaVirtual);
            System.out.println("PROC " + actual.getId() + " envejecimiento");

            if (!actual.haTerminado()) {
                colaProcesos.add(actual);
            } else {
                System.out.println("========================");
                System.out.println("Termino proc: " + actual.getId());
                System.out.println("========================");
                reasignarMarcos(actual);
            }
        }
        System.out.println("\n>>> Simulación Terminada <<<");
    }

    private void manejarFalloDePagina(Proceso proceso, int paginaVirtual) {
        Set<Integer> marcosOcupados = new HashSet<>();
        proceso.getTablaPaginas().values().stream()
            .filter(EntradaTablaPaginas::esValida)
            .forEach(e -> marcosOcupados.add(e.getNumMarco()));

        int marcoLibre = -1;
        for (int marcoAsignado : proceso.getMarcosAsignados()) {
            if (!marcosOcupados.contains(marcoAsignado)) {
                marcoLibre = marcoAsignado;
                break;
            }
        }

        if (marcoLibre != -1) {
            EntradaTablaPaginas nuevaEntrada = proceso.getTablaPaginas().get(paginaVirtual);
            nuevaEntrada.setNumMarco(marcoLibre);
            nuevaEntrada.setValida(true);
            proceso.registrarAccesoSwap(1);
        } else {
            int paginaVictima = proceso.encontrarVictimaLRU();
            EntradaTablaPaginas pteVictima = proceso.getTablaPaginas().get(paginaVictima);
            int marcoReemplazo = pteVictima.getNumMarco();

            pteVictima.setValida(false);
            pteVictima.setNumMarco(-1);

            EntradaTablaPaginas pteNueva = proceso.getTablaPaginas().get(paginaVirtual);
            pteNueva.setNumMarco(marcoReemplazo);
            pteNueva.setValida(true);
            proceso.registrarAccesoSwap(2);
        }
    }

    private void reasignarMarcos(Proceso procesoTerminado) {
        if (colaProcesos.isEmpty()) { return; }

        List<Integer> marcosLiberados = procesoTerminado.liberarMarcos();
        for(int marco : marcosLiberados){
             System.out.println("PROC " + procesoTerminado.getId() + " removiendo marco: " + marco);
        }

        Proceso procesoReceptor = colaProcesos.stream()
            .max(Comparator.comparingInt(Proceso::getFallosDePagina))
            .orElse(null);

        if (procesoReceptor != null) {
            procesoReceptor.agregarMarcosAsignados(marcosLiberados);
            for(int marco : marcosLiberados){
                System.out.println("PROC " + procesoReceptor.getId() + " asignando marco nuevo " + marco);
            }
        }
    }

    public void imprimirEstadisticas() {
        System.out.println("\n--- Estadísticas Finales ---");
        for (Proceso p : listaProcesos) {
            int totalReferencias = p.getTotalReferencias();
            int fallos = p.getFallosDePagina();
            int hits = totalReferencias - fallos;
            int swap = p.getAccesosASwap();
            double tasaFallos = (totalReferencias > 0) ? (double) fallos / totalReferencias : 0;
            double tasaExito = (totalReferencias > 0) ? (double) hits / totalReferencias : 0;
            
            System.out.println("---------------------------------");
            System.out.println("Proceso: " + p.getId());
            System.out.println("- Num referencias: " + totalReferencias);
            System.out.println("- Fallas: " + fallos);
            System.out.println("- Hits: " + hits);
            System.out.println("- SWAP: " + swap);
            System.out.printf("- Tasa fallas: %.4f\n", tasaFallos);
            System.out.printf("- Tasa éxito: %.4f\n", tasaExito);
        }
        System.out.println("---------------------------------");
    }
}