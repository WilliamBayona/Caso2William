import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n== SIMULADOR DE MEMORIA VIRTUAL ==");
            System.out.println("1. Generar archivos de referencias (Opción 1)");
            System.out.println("2. Ejecutar simulación de memoria virtual (Opción 2)");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    // --- Lógica para la Opción 1 ---
                    System.out.print("Ingrese el nombre del archivo de configuración (ej: config.txt): ");
                    String archivoConfig = scanner.nextLine();
                    GeneradorReferencias generador = new GeneradorReferencias();
                    generador.generar(archivoConfig);
                    break;

                case "2":
                    // --- Lógica para la Opción 2 ---
                    try {
                        System.out.print("Ingrese el número total de marcos de RAM: ");
                        int marcos = Integer.parseInt(scanner.nextLine());

                        System.out.print("Ingrese el número de procesos a simular: ");
                        int procesos = Integer.parseInt(scanner.nextLine());

                        // Validación para asegurar que los marcos son múltiplos de los procesos
                        if (marcos % procesos != 0) {
                            System.err.println("Error: El número de marcos debe ser múltiplo del número de procesos.");
                            continue; // Vuelve al menú principal
                        }

                        Simulador sim = new Simulador(marcos, procesos);
                        sim.iniciar();
                        sim.simular();
                        sim.imprimirEstadisticas();

                    } catch (NumberFormatException e) {
                        System.err.println("Error: Por favor, ingrese un número válido.");
                    }
                    break;

                case "3":
                    // --- Salir del programa ---
                    System.out.println("Saliendo del simulador...");
                    scanner.close();
                    return; // Termina el programa

                default:
                    System.out.println("Opción no válida. Por favor, intente de nuevo.");
                    break;
            }
        }
    }
}