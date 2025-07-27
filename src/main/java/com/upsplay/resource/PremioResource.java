package com.upsplay.resource;

import com.upsplay.dao.PremioDAO;
import com.upsplay.model.Premio;

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

@Path("/premios")
public class PremioResource {

    @Inject
    private PremioDAO premioDAO;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPremio(Premio premio) {
        try {
            Premio savedPremio = premioDAO.savePremio(premio);
            return Response.status(Response.Status.CREATED).entity(savedPremio).build();
        } catch (RuntimeException e) {
            System.err.println("Error al crear premio: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo crear el premio: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPremios() {
        try {
            List<Premio> premios = premioDAO.getAllPremios();
            return Response.ok(premios).build();
        } catch (RuntimeException e) {
            System.err.println("Error al obtener todos los premios: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudieron obtener los premios: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPremioByUuid(@PathParam("uuid") String uuid) {
        try {
            Premio premio = premioDAO.getPremioByUuid(uuid);
            if (premio != null) {
                return Response.ok(premio).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Premio no encontrado.\"}").build();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al obtener premio por UUID: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo obtener el premio: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @PUT
    @Path("/update/{uuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePremio(@PathParam("uuid") String uuid, Premio premio) {
        try {
            premio.setUuid(uuid);
            Premio updatedPremio = premioDAO.updatePremio(premio);
            if (updatedPremio != null) {
                return Response.ok(updatedPremio).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Premio no encontrado para actualizar.\"}").build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        } catch (RuntimeException e) {
            System.err.println("Error al actualizar premio: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo actualizar el premio: " + e.getMessage() + "\"}")
                           .build();
        }
    }

    @DELETE
    @Path("/delete/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePremio(@PathParam("uuid") String uuid) {
        try {
            boolean deleted = premioDAO.deletePremio(uuid);
            if (deleted) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Premio no encontrado para eliminar.\"}").build();
            }
        } catch (RuntimeException e) {
            System.err.println("Error al eliminar premio: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"No se pudo eliminar el premio: " + e.getMessage() + "\"}")
                           .build();
        }
    }
}