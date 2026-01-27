package com.grupo_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) {
        final String HOST = "localhost";
        final int PORT = 3030;

        System.out.println("\n== INICIANDO CLIENTE ==");

        try (Socket socket = new Socket(HOST, PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner sc = new Scanner(System.in)) {

            System.out.println("Conectado al servidor.\n");

            String linea;

            while ((linea = in.readLine()) != null) {

                // El servidor pide entrada
                if (linea.trim().equals(">")) {
                    System.out.print(linea);
                    String respuesta = sc.nextLine();
                    out.println(respuesta);
                }
                // Fin del juego
                else if (linea.equals("TERMINADO")) {
                    System.out.println("\n=== PARTIDA FINALIZADA ===");
                    break;
                }
                // Mensaje normal
                else {
                    System.out.println(linea);
                }
            }

        } catch (IOException e) {
            System.out.println("Conexión cerrada por el servidor");
        }
        System.out.println("\ncerrando sesión...");
    }
}
