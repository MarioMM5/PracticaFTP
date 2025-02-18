package org.educa;

import org.apache.commons.net.ftp.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class FTPClientApp {
    private static final String SERVER = "127.0.0.1";
    private static final int PORT = 21;

    public static void main(String[] args) {
        try(Scanner scanner = new Scanner(System.in)) {
            FTPClient ftpClient = new FTPClient();
            boolean loginSuccess=false;
            try {
                while(!loginSuccess) {
                    System.out.print("Ingrese nombre de usuario: ");
                    String username = scanner.nextLine();
                    System.out.print("Ingrese la password (No inserte nada si es anónimo): ");
                    String password = scanner.nextLine();

                    ftpClient.connect(SERVER, PORT);
                    loginSuccess = ftpClient.login(username, password);

                    if (loginSuccess) {
                        System.out.println("Conexión exitosa como: " + username);
                        menuOperaciones(ftpClient, scanner, username);
                        ftpClient.logout();
                    } else {
                        System.out.println("Error al conectar. Verifica las credenciales.");
                    }
                }

                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void menuOperaciones(FTPClient ftpClient, Scanner scanner, String username) throws IOException {
        while (true) {
            System.out.println("\n Menú de operaciones:");
            System.out.println("1. Listar archivos en el servidor");
            System.out.println("2. Descargar archivo");
            System.out.println("3. Subir archivo");
            System.out.println("4. Salir");
            System.out.print("Opción: ");
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    listarArchivos(ftpClient);
                    break;
                case 2:
                    System.out.print("Nombre del archivo a descargar: ");
                    String archivoDescargar = scanner.nextLine();
                    descargarArchivo(ftpClient, archivoDescargar);
                    break;
                case 3:
                    if (!username.equalsIgnoreCase("anonimo")) { // Solo autenticados pueden subir
                        System.out.print("Nombre del archivo a subir: ");
                        String archivoSubir = scanner.nextLine();
                        subirArchivo(ftpClient, archivoSubir);
                    } else {
                        System.err.println("Los usuarios anónimos no pueden subir archivos.");
                    }
                    break;
                case 4:
                    return;
                default:
                    System.err.println("Opción no válida.");
            }
        }
    }

    private static void listarArchivos(FTPClient ftpClient) throws IOException {
        FTPFile[] archivos = ftpClient.listFiles();
        System.out.println("Archivos en el servidor:");
        for (FTPFile archivo : archivos) {
            System.out.println("- " + archivo.getName());
        }
    }

    private static void subirArchivo(FTPClient ftpClient, String localFilePath) {
        try {
            File archivo = new File(localFilePath);
            if (!archivo.exists()) {
                System.out.println("El archivo a subir no existe.");
                return;
            }
            FileInputStream fis = new FileInputStream(archivo);
            boolean subido = ftpClient.storeFile(archivo.getName(), fis);
            fis.close();
            if (subido) {
                System.out.println("Archivo subido con éxito.");
            } else {
                System.out.println("Error al subir el archivo.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void descargarArchivo(FTPClient ftpClient, String remoteFilePath) throws IOException {
        File archivoLocal = new File(remoteFilePath);
        FileOutputStream fos = new FileOutputStream(archivoLocal);
        boolean descargado = ftpClient.retrieveFile(remoteFilePath, fos);
        fos.close();
        System.out.println(descargado ? "Archivo descargado con éxito." : " Error al descargar el archivo.");
    }
}
