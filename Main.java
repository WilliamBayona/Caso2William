public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Por favor, especifique una opción.");
            System.out.println("Uso: java Main <opcion> [argumentos]");
            System.out.println("Opciones:");
            System.out.println("  1 <archivo_config>       : Generar archivos de referencias.");
            System.out.println("  2 <num_marcos> <num_proc>: Ejecutar simulación.");
            return;
        }

        String opcion = args[0];

        if (opcion.equals("1")) {
            if (args.length < 2) {
                System.out.println("Uso para Opción 1: java Main 1 <archivo_config>");
                return;
            }
            String archivoConfig = args[1];
            GeneradorReferencias generador = new GeneradorReferencias();
            generador.generar(archivoConfig);

        } else if (opcion.equals("2")) {
            if (args.length < 3) {
                System.out.println("Uso para Opción 2: java Main 2 <num_marcos> <num_proc>");
                return;
            }
            int marcos = Integer.parseInt(args[1]);
            int procesos = Integer.parseInt(args[2]);

            if (marcos % procesos != 0) {
                 System.out.println("Error: El número de marcos debe ser múltiplo del número de procesos.");
                 return;
            }

            Simulador sim = new Simulador(marcos, procesos);
            sim.iniciar();
            sim.simular();
            sim.imprimirEstadisticas();
            
        } else {
            System.out.println("Opción no válida. Use '1' o '2'.");
        }
    }
}