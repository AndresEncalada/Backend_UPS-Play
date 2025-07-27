package com.upsplay.resource;


import com.upsplay.dao.GeneroDAO;
import com.upsplay.model.Genero;

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

@Path("/generos")
public class GeneroResource {

    @Inject
    private GeneroDAO generoDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGenero(Genero genero) {
        try {
            Genero savedGenero = generoDAO.saveGenero(genero);
            return Response.status(Response.Status.CREATED).entity(savedGenero).build();
        } catch (RuntimeException e) {
            System.err.println("Error al crear género: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo crear el género: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllGeneros() {
        try {
            List<Genero> generos = generoDAO.getAllGeneros();
            return Response.ok(generos).build();
        } catch (RuntimeException e) {
            System.err.println("Error al obtener todos los géneros: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudieron obtener los géneros: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneroByUuid(@PathParam("uuid") String uuid) {
        try {
            Genero genero = generoDAO.getGeneroByUuid(uuid);
            if (genero != null) {
                return Response.ok(genero).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Género no encontrado.\"}").build();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al obtener género por UUID: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo obtener el género: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @PUT
    @Path("/update/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGenero(@PathParam("uuid") String uuid, Genero genero) {
        try {
            genero.setUuid(uuid);
            Genero updatedGenero = generoDAO.updateGenero(genero);
            if (updatedGenero != null) {
                return Response.ok(updatedGenero).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Género no encontrado para actualizar.\"}").build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        } catch (RuntimeException e) {
            System.err.println("Error al actualizar género: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo actualizar el género: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @DELETE
    @Path("/delete/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGenero(@PathParam("uuid") String uuid) {
        try {
            boolean deleted = generoDAO.deleteGenero(uuid);
            if (deleted) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Género no encontrado para eliminar.\"}").build();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al eliminar género: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo eliminar el género: " + e.getMessage() + "\"}")
                           .build();
        }
    }
}