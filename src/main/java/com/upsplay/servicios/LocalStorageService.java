package com.upsplay.servicios;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@ApplicationScoped
public class LocalStorageService {

 // Define la carpeta donde se guardarán los archivos de audio
 // Usamos System.getProperty("user.dir") para obtener el directorio actual de trabajo
 // que suele ser la raíz de tu proyecto cuando lo ejecutas desde Eclipse/Maven.
 private static final String UPLOAD_DIRECTORY = System.getProperty("user.dir") + File.separator + "Audios";

 public LocalStorageService() {
     Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
     if (!Files.exists(uploadPath)) {
         try {
             Files.createDirectories(uploadPath);
             System.out.println("DEBUG LocalStorage: Directorio de subida creado: " + UPLOAD_DIRECTORY);
         } catch (IOException e) {
             System.err.println("ERROR LocalStorage: No se pudo crear el directorio de subida: " + UPLOAD_DIRECTORY + " - " + e.getMessage());
             throw new RuntimeException("Error al inicializar el servicio de almacenamiento local.", e);
         }
     } else {
         System.out.println("DEBUG LocalStorage: Directorio de subida ya existe: " + UPLOAD_DIRECTORY);
     }
 }
 /**
  * Guarda un archivo de InputStream a una ubicación local.
  * @param inputStream El InputStream del archivo.
  * @param originalFileName El nombre original del archivo (para extraer la extensión).
  * @return La URL local relativa (ej: "/audios/nombre_unico.mp3") para ser accedida por el frontend.
  * @throws IOException Si ocurre un error al escribir el archivo.
  */
 public String saveFileLocally(InputStream inputStream, String originalFileName) throws IOException {
     String fileExtension = "";
     int dotIndex = originalFileName.lastIndexOf('.');
     if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
         fileExtension = originalFileName.substring(dotIndex);
     }

     String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
     File fileToSave = new File(UPLOAD_DIRECTORY, uniqueFileName);

     try (OutputStream outputStream = new FileOutputStream(fileToSave)) {
         byte[] buffer = new byte[4096]; // Buffer de 4KB
         int bytesRead;
         while ((bytesRead = inputStream.read(buffer)) != -1) {
             outputStream.write(buffer, 0, bytesRead);
         }
         outputStream.flush();
     }

     return "/api/audios/" + uniqueFileName;
 }
 /**
  * Método para obtener el archivo por su nombre único.
  * @param uniqueFileName El nombre único del archivo.
  * @return El archivo File.
  */
 public File getLocalFile(String uniqueFileName) {
     return new File(UPLOAD_DIRECTORY, uniqueFileName);
 }
}