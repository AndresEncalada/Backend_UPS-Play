package com.upsplay.resource;

import jakarta.inject.Inject; // De javax.inject.Inject
import jakarta.ws.rs.*; // De javax.ws.rs.*
import jakarta.ws.rs.core.MediaType; // De javax.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response; // De javax.ws.rs.core.Response

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Path("/audios") // La URL será (tu_base_url_wildfly)/api/audios/{filename}
public class AudioResource {

 // ¡¡¡CAMBIA ESTA RUTA!!!
 // Debe ser la ruta ABSOLUTA en tu servidor Wildfly donde están guardados los archivos MP3.
 // Ejemplo para Windows: "C:\\Users\\USER\\wildfly-27.0.0.Final\\bin\\Audios\\"
 // Ejemplo para Linux/Unix: "/opt/wildfly/bin/Audios/" o "/var/www/audios/"
 private static final String AUDIO_DIRECTORY = "C:\\Users\\USER\\wildfly-27.0.0.Final\\bin\\Audios\\";

 @GET
 @Path("/{filename}") // Captura el nombre del archivo (ej. 5f59909c-d0a6-45b8-96c2-16ff6219176d.mp3)
 @Produces("audio/mpeg") // ¡Tipo MIME crucial para que el navegador sepa que es un MP3!
 public Response getAudioFile(@PathParam("filename") String filename) {
     File audioFile = new File(AUDIO_DIRECTORY + filename);

     if (!audioFile.exists() || !audioFile.isFile()) {
         System.err.println("Archivo de audio no encontrado: " + audioFile.getAbsolutePath());
         return Response.status(Response.Status.NOT_FOUND)
                        .entity("Archivo de audio '" + filename + "' no encontrado en el servidor.")
                        .build();
     }

     try {
         FileInputStream fis = new FileInputStream(audioFile);
         return Response.ok(fis, "audio/mpeg") // Devuelve el archivo como un stream con el tipo MIME correcto
                        .header("Content-Disposition", "inline; filename=\"" + filename + "\"") // "inline" para reproducir en el navegador
                        // Opcional: Añade cabeceras CORS si no tienes un filtro global para ellos
                        // .header("Access-Control-Allow-Origin", "http://localhost:4200") // O "*" para todos
                        // .header("Access-Control-Allow-Methods", "GET")
                        .build();
     } catch (IOException e) {
         System.err.println("Error al leer archivo de audio: " + e.getMessage());
         return Response.serverError().entity("Error al procesar el archivo de audio.").build();
     }
 }
}