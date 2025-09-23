import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Esta clase se encarga de generar los archivos de referencias de memoria
 * para múltiples procesos que simulan la suma de matrices.
 * Corresponde a la Opción 1 del Caso 2.
 */
public class GeneradorReferencias {

    private int tamPagina;
    private int numProcesos;
    private String[] tamMatrices;

    /**
     * Método principal para iniciar la generación de referencias.
     * Lee la configuración y luego genera un archivo por cada proceso.
     * @param rutaConfig La ruta al archivo de configuración (ej. "config.txt").
     */
    public void generar(String rutaConfig) {
        try {
            System.out.println("--- Iniciando Generación de Referencias ---");
            leerConfiguracion(rutaConfig);

            for (int i = 0; i < numProcesos; i++) {
                generarArchivoParaProceso(i);
            }

            System.out.println(">>> Archivos de referencias generados exitosamente. <<<");

        } catch (IOException e) {
            System.err.println("Error al procesar el archivo de configuración: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error: Formato numérico incorrecto en el archivo de configuración.");
        }
    }

    /**
     * Lee y parsea el archivo de configuración para inicializar los parámetros
     * del generador.
     * @param rutaConfig La ruta al archivo de configuración.
     * @throws IOException Si ocurre un error de E/S.
     * @throws NumberFormatException Si un valor numérico no es válido.
     */
    private void leerConfiguracion(String rutaConfig) throws IOException, NumberFormatException {
        System.out.println("Leyendo archivo de configuración: " + rutaConfig);
        try (BufferedReader br = new BufferedReader(new FileReader(rutaConfig))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split("=");
                if (partes.length == 2) {
                    String clave = partes[0].trim();
                    String valor = partes[1].trim();

                    switch (clave) {
                        case "TP":
                            this.tamPagina = Integer.parseInt(valor);
                            break;
                        case "NPROC":
                            this.numProcesos = Integer.parseInt(valor);
                            break;
                        case "TAMS":
                            // Separa los tamaños por el espacio, ej. "4x4 8x8"
                            this.tamMatrices = valor.split(" ");
                            break;
                    }
                }
            }
        }
        System.out.println("Configuración leída: TP=" + tamPagina + ", NPROC=" + numProcesos);
    }

    /**
     * Genera el archivo de referencias para un proceso específico.
     * @param idProceso El ID del proceso (0, 1, 2, ...).
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    private void generarArchivoParaProceso(int idProceso) throws IOException {
        String nombreArchivo = "proc" + idProceso + ".txt";
        System.out.println("Generando archivo para proceso " + idProceso + ": " + nombreArchivo);

        // Parsea el tamaño de la matriz (ej. "4x4") para obtener filas y columnas
        String[] dimensiones = tamMatrices[idProceso].split("x");
        int nf = Integer.parseInt(dimensiones[0]);
        int nc = Integer.parseInt(dimensiones[1]);

        final int TAM_ENTERO_BYTES = 4; // Tamaño de un entero en bytes [cite: 44]
        int tamMatrizBytes = nf * nc * TAM_ENTERO_BYTES;
        int memoriaTotalBytes = 3 * tamMatrizBytes; // Tres matrices: matriz1, matriz2, matriz3 [cite: 42]
        int numPaginasVirtuales = (int) Math.ceil((double) memoriaTotalBytes / tamPagina);
        int numReferenciasTotales = nf * nc * 3;

        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            // Escribe la cabecera en el archivo [cite: 45]
            writer.println("TP=" + this.tamPagina);
            writer.println("NF=" + nf);
            writer.println("NC=" + nc);
            writer.println("NR=" + numReferenciasTotales);
            writer.println("NP=" + numPaginasVirtuales);

            // Bucles anidados para generar y escribir cada referencia
            // simulando el almacenamiento en "row-major order" [cite: 42]
            for (int i = 0; i < nf; i++) {
                for (int j = 0; j < nc; j++) {
                    // --- Referencia para Matriz 1 (lectura) ---
                    int dv1 = (i * nc + j) * TAM_ENTERO_BYTES;
                    int pagina1 = dv1 / tamPagina;
                    int offset1 = dv1 % tamPagina;
                    writer.println("M1:[" + i + "-" + j + "]," + pagina1 + "," + offset1 + ",r");

                    // --- Referencia para Matriz 2 (lectura) ---
                    int dv2 = tamMatrizBytes + (i * nc + j) * TAM_ENTERO_BYTES;
                    int pagina2 = dv2 / tamPagina;
                    int offset2 = dv2 % tamPagina;
                    writer.println("M2:[" + i + "-" + j + "]," + pagina2 + "," + offset2 + ",r");

                    // --- Referencia para Matriz 3 (escritura) ---
                    int dv3 = (2 * tamMatrizBytes) + (i * nc + j) * TAM_ENTERO_BYTES;
                    int pagina3 = dv3 / tamPagina;
                    int offset3 = dv3 % tamPagina;
                    writer.println("M3:[" + i + "-" + j + "]," + pagina3 + "," + offset3 + ",w");
                }
            }
        }
    }
}