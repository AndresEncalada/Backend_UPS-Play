package com.upsplay.resource;

import com.upsplay.dao.AutorDAO;
import com.upsplay.model.Autor;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/autores")
public class AutorResource {

    @Inject
    private AutorDAO autorDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAutor(Autor autor) {
        try {
            Autor savedAutor = autorDAO.saveAutor(autor);
            return Response.status(Response.Status.CREATED).entity(savedAutor).build();
        } catch (RuntimeException e) {
            System.err.println("Error al crear autor: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo crear el autor: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAutores() {
        try {
            List<Autor> autores = autorDAO.getAllAutores();
            return Response.ok(autores).build();
        } catch (RuntimeException e) {
            System.err.println("Error al obtener todos los autores: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudieron obtener los autores: " + e.getMessage() + "\"}")
                           .build();
        }
    }
    @GET
    @Path("/{autorUuid}/colaboradores") 
    @Produces(MediaType.APPLICATION_JSON)
    public Response getColaboradoresDeAutor(
        @PathParam("autorUuid") String autorUuid 
    ) {
        if (autorUuid == null || autorUuid.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("El UUID del autor no puede estar vacío.")
                           .build();
        }

        try {
            List<Autor> colaboradores = autorDAO.getColaboradoresByAutorUuid(autorUuid);

            if (colaboradores.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                               .entity("No se encontraron colaboradores para el autor con UUID: " + autorUuid)
                               .build();
            }
            return Response.ok(colaboradores).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("Error al obtener colaboradores para el autor " + autorUuid + ": " + e.getMessage())
                           .build();
        }
    }

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAutorByUuid(@PathParam("uuid") String uuid) {
        try {
            Autor autor = autorDAO.getAutorByUuid(uuid);
            if (autor != null) {
                return Response.ok(autor).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Autor no encontrado.\"}").build();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al obtener autor por UUID: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo obtener el autor: " + e.getMessage() + "\"}")
                           .build();
        }
    }

   
}