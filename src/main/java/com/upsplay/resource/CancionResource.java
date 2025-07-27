package com.upsplay.resource;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException; // Para manejar errores de parsing de fecha
import java.text.SimpleDateFormat; // Para parsear la fecha de String a Date
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Context;
import java.io.InputStream;
import java.io.File; // Este es necesario para el nuevo endpoint getAudioFile
import java.nio.file.Files; // Este también es necesario para el nuevo endpoint getAudioFile
import java.util.UUID;
import jakarta.ws.rs.core.HttpHeaders;
import com.upsplay.dao.CancionDAO;
import com.upsplay.model.Cancion;
import com.upsplay.servicios.LocalStorageService;


import jakarta.ws.rs.core.Context; // Necesario para @Context HttpHeaders
import java.io.InputStream;
import java.util.UUID; 
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/canciones")
public class CancionResource {

    @Inject
    private CancionDAO cancionDAO;
    @Inject 
    private LocalStorageService localStorageService;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCancion(Cancion cancion) {
        try {
            Cancion savedCancion = cancionDAO.saveCancion(cancion);
            return Response.status(Response.Status.CREATED).entity(savedCancion).build();
        } catch (RuntimeException e) {
            System.err.println("Error al crear canción: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo crear la canción: " + e.getMessage() + "\"}")
                           .build();
        }
    }
    @GET
    @Path("/nombre/{titulo}") 
    public Response getCancionByTitulo(@PathParam("titulo") String titulo) {
        Optional<Cancion> cancion = cancionDAO.getCancionByTitulo(titulo);
        if (cancion.isPresent()) {
            return Response.ok(cancion.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Canción con título '" + titulo + "' no encontrada.")
                           .build();
        }
    }

    @POST
    @Path("/upload-audio") 
    @Consumes("audio/mpeg")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadAudioFile(
            InputStream uploadedInputStream,
            @Context HttpHeaders headers,
            @QueryParam("fileName") String originalFileName
    ) {
        if (uploadedInputStream == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"No se proporcionó ningún archivo de audio.\"}").build();
        }
        if (originalFileName == null || originalFileName.isEmpty()) {
            originalFileName = "audio_desconocido"; 
        }

        System.out.println("DEBUG LocalStorage: Recibiendo archivo '" + originalFileName + "'");

        try {
            String localUrl = localStorageService.saveFileLocally(uploadedInputStream, originalFileName);
            System.out.println("DEBUG LocalStorage: Archivo guardado localmente. URL retornada: " + localUrl);

            return Response.ok("{\"message\": \"Archivo subido exitosamente al servidor local\", \"url\": \"" + localUrl + "\"}").build();
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo de audio localmente: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error al guardar el archivo de audio localmente: " + e.getMessage() + "\"}")
                           .build();
        } catch (Exception e) {
            System.err.println("Error inesperado al subir el archivo de audio: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error inesperado: " + e.getMessage() + "\"}")
                           .build();
        }
    }
    @GET
    @Path("/audios/{fileName}") // ¡NUEVO ENDPOINT! La URL será /api/audios/{nombre_unico_del_archivo}
    @Produces(MediaType.APPLICATION_OCTET_STREAM) // Indica que se enviará el archivo binario
    public Response getAudioFile(@PathParam("fileName") String fileName) {
        File audioFile = localStorageService.getLocalFile(fileName);

        if (!audioFile.exists() || !audioFile.isFile()) {
            System.err.println("ERROR LocalStorage: Archivo no encontrado: " + fileName + " en " + audioFile.getAbsolutePath());
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Archivo de audio no encontrado.\"}").build();
        }

        try {
            String contentType = Files.probeContentType(audioFile.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream"; // Fallback si no se puede determinar el tipo MIME
            }
            System.out.println("DEBUG LocalStorage: Sirviendo archivo '" + fileName + "' con Content-Type: " + contentType);

            // Response.ok(File) es una forma conveniente de devolver un archivo
            return Response.ok(audioFile)
                           .header("Content-Disposition", "inline; filename=\"" + fileName + "\"") // "inline" para reproducir directamente en el navegador
                           .header("Content-Length", String.valueOf(audioFile.length()))
                           .type(contentType)
                           .build();
        } catch (IOException e) {
            System.err.println("Error al servir el archivo de audio: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error al servir el archivo de audio: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCanciones() {
        try {
            List<Cancion> canciones = cancionDAO.getAllCanciones();
            if (canciones.isEmpty()) {
                return Response.ok(canciones).build(); 
            }
            return Response.ok(canciones).build();
        } catch (RuntimeException e) {
            System.err.println("Error al obtener todas las canciones: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudieron obtener las canciones: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/buscar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchCanciones(@QueryParam("query") String busqueda) {
        System.out.println("DEBUG: Recibida solicitud para buscar canciones. Valor de 'busqueda': [" + busqueda + "]");
        try {
            List<Cancion> canciones = cancionDAO.searchCancionesByTitulo(busqueda);
            System.out.println("DEBUG: Canciones encontradas y retornadas: " + canciones.size());
            return Response.ok(canciones).build();
        } catch (RuntimeException e) {
            System.err.println("Error al buscar canciones: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error al buscar canciones: " + e.getMessage() + "\"}")
                           .build();
        }
    }
    @GET
    @Path("/porAutor/{autorUuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCancionesByAutor(@PathParam("autorUuid") String autorUuid) {
        System.out.println("DEBUG: Recibida solicitud para obtener canciones del autor con UUID: [" + autorUuid + "]");
        try {
            List<Cancion> canciones = cancionDAO.getCancionesByAutorUuid(autorUuid);
            if (canciones.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("{\"message\": \"No se encontraron canciones para el autor con UUID: " + autorUuid + "\"}").build();
            }
            System.out.println("DEBUG: Canciones encontradas para el autor: " + canciones.size());
            return Response.ok(canciones).build();
        } catch (RuntimeException e) {
            System.err.println("Error al obtener canciones por autor UUID: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudieron obtener las canciones del autor: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCancionByUuid(@PathParam("uuid") String uuid) {
        try {
            Cancion cancion = cancionDAO.getCancionByUuid(uuid);
            if (cancion != null) {
                return Response.ok(cancion).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Canción no encontrada.\"}").build();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al obtener canción por UUID: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo obtener la canción: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/porFecha")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCancionesByDateRange(
        @QueryParam("fechaInicio") String fechaInicioStr, // Recibe la fecha como String
        @QueryParam("fechaFin") String fechaFinStr        // Recibe la fecha como String
    ) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaInicio = null;
        Date fechaFin = null;

        try {
            if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
                fechaInicio = formatter.parse(fechaInicioStr);
            }
            if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
                fechaFin = formatter.parse(fechaFinStr);
            }
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Formato de fecha inválido. Use YYYY-MM-DD. Error: " + e.getMessage())
                           .build();
        }

        try {
            // Llama a un nuevo método en tu DAO que acepte los rangos de fecha
            // Si fechaInicio o fechaFin son null, tu DAO debería manejarlos (ej. no aplicar el filtro)
            // O podrías tener lógica aquí para llamar a getAllCanciones() si ambos son null.
            List<Cancion> canciones = cancionDAO.getCancionesPorFecha(fechaInicio, fechaFin);
            if (canciones.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("No se encontraron canciones en el rango de fechas especificado.").build();
            }
            return Response.ok(canciones).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error al obtener canciones por rango de fechas: " + e.getMessage())
                           .build();
        }
    }
    
    @GET
    @Path("/generoC/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCancionesByGeneroId(
            @PathParam("uuid") String generoUuid 
        ) {
            if (generoUuid == null || generoUuid.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("El UUID del género no puede estar vacío.")
                               .build();
            }

            try {
                List<Cancion> canciones = cancionDAO.getCancionesPorGeneroID(generoUuid);

                if (canciones.isEmpty()) {
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity("No se encontraron canciones para el género con UUID: " + generoUuid)
                                   .build();
                }
                return Response.ok(canciones).build();
            } catch (RuntimeException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("Error al obtener canciones por UUID de género: " + e.getMessage())
                               .build();
            }
        }

    
    @GET
    @Path("/mayor-descarga") 
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCancionMayorDescargaByDateRange(
        @QueryParam("fechaInicio") String fechaInicioStr,
        @QueryParam("fechaFin") String fechaFinStr
    ) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaInicio = null;
        Date fechaFin = null;

        try {
            if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
                fechaInicio = formatter.parse(fechaInicioStr);
            }
            if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
                fechaFin = formatter.parse(fechaFinStr);
            }
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Formato de fecha inválido. Use YYYY-MM-DD. Error: " + e.getMessage())
                           .build();
        }

        if (fechaInicio == null || fechaFin == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Las fechas 'fechaInicio' y 'fechaFin' son obligatorias para esta consulta.")
                           .build();
        }


        try {
        	System.out.println(fechaInicio+" "+fechaFin);
            Optional<Cancion> cancionOptional = cancionDAO.getCancionMayorD(fechaInicio, fechaFin);

            if (cancionOptional.isPresent()) {
                return Response.ok(cancionOptional.get()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("No se encontraron canciones o no hay mayor descarga en el rango de fechas especificado.")
                               .build();
            }
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error al obtener la canción de mayor descarga por rango de fechas: " + e.getMessage())
                           .build();
        }
    }

}