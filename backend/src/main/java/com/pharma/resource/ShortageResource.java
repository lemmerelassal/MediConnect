package com.pharma.resource;

import com.pharma.entity.Shortage;
import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api/shortages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class ShortageResource {

    @GET
    public List<Shortage> list(
            @QueryParam("status") String status,
            @QueryParam("urgency") String urgency,
            @QueryParam("country") Long countryId) {
        
        StringBuilder query = new StringBuilder("1=1");
        
        if (status != null && !status.isEmpty()) {
            query.append(" and status = '").append(status).append("'");
        }
        if (urgency != null && !urgency.isEmpty()) {
            query.append(" and urgencyLevel = '").append(urgency).append("'");
        }
        if (countryId != null) {
            query.append(" and country.id = ").append(countryId);
        }
        
        return Shortage.find(query.toString()).list();
    }

    @GET
    @Path("/{id}")
    public Shortage get(@PathParam("id") Long id) {
        Shortage shortage = Shortage.findById(id);
        if (shortage == null) {
            throw new WebApplicationException("Shortage not found", Response.Status.NOT_FOUND);
        }
        return shortage;
    }

    @POST
    @Transactional
    public Response create(Shortage shortage) {
        shortage.persist();
        return Response.status(Response.Status.CREATED).entity(shortage).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Shortage update(@PathParam("id") Long id, Shortage updatedShortage) {
        Shortage shortage = Shortage.findById(id);
        if (shortage == null) {
            throw new WebApplicationException("Shortage not found", Response.Status.NOT_FOUND);
        }
        
        shortage.quantityNeeded = updatedShortage.quantityNeeded;
        shortage.urgencyLevel = updatedShortage.urgencyLevel;
        shortage.status = updatedShortage.status;
        shortage.deadline = updatedShortage.deadline;
        shortage.reason = updatedShortage.reason;
        
        return shortage;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Shortage shortage = Shortage.findById(id);
        if (shortage == null) {
            throw new WebApplicationException("Shortage not found", Response.Status.NOT_FOUND);
        }
        
        shortage.delete();
        return Response.ok(Map.of("message", "Shortage deleted successfully")).build();
    }
}
