package com.grupo_1;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Servidor {
    public static void main(String[] args) {
        final int PUERTO = 3030;
        ArrayList<Socket> jugadores = new ArrayList<>();
        PrintWriter pw;

        try (ServerSocket svSocket = new ServerSocket(PUERTO); ) {
            System.out.println("== SERVIDOR ESCUCHANDO EN EL PUERTO: " + PUERTO + " ==");
            svSocket.setSoTimeout(2000);

            while (true) {
                try {
                    Socket cliente = svSocket.accept(); // Acepta 'clientes'
                    pw = new PrintWriter(cliente.getOutputStream());
                    jugadores.add(cliente);
                    System.out.println("Nuevo cliente: "+cliente.getInetAddress());

                    pw.println("ESPERANDO A OTRO JUGADOR");

                    if (jugadores.size() == 2) {
                        new Thread(new Juego(jugadores)).start();
                        jugadores.clear();
                    }
                } catch (SocketTimeoutException e) {
                    System.out.print(".");
                }
            }

        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }finally{

        }
    }
}