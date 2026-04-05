package com.pharma.resource;

import com.pharma.entity.Tender;
import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("/api/tenders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class TenderResource {

    @GET
    public List<Tender> list(
            @QueryParam("status") String status,
            @QueryParam("shortage") Long shortageId) {
        
        StringBuilder query = new StringBuilder("1=1");
        
        if (status != null && !status.isEmpty()) {
            query.append(" and status = '").append(status).append("'");
        }
        if (shortageId != null) {
            query.append(" and shortage.id = ").append(shortageId);
        }
        
        return Tender.find(query.toString()).list();
    }

    @GET
    @Path("/{id}")
    public Tender get(@PathParam("id") Long id) {
        Tender tender = Tender.findById(id);
        if (tender == null) {
            throw new WebApplicationException("Tender not found", Response.Status.NOT_FOUND);
        }
        return tender;
    }

    @POST
    @Transactional
    public Response create(Tender tender) {
        tender.persist();
        return Response.status(Response.Status.CREATED).entity(tender).build();
    }

    @PUT
    @Path("/{id}/accept")
    @Transactional
    public Tender accept(@PathParam("id") Long id) {
        Tender tender = Tender.findById(id);
        if (tender == null) {
            throw new WebApplicationException("Tender not found", Response.Status.NOT_FOUND);
        }
        
        tender.status = Tender.TenderStatus.ACCEPTED;
        tender.reviewedAt = LocalDateTime.now();
        
        return tender;
    }

    @PUT
    @Path("/{id}/reject")
    @Transactional
    public Tender reject(@PathParam("id") Long id) {
        Tender tender = Tender.findById(id);
        if (tender == null) {
            throw new WebApplicationException("Tender not found", Response.Status.NOT_FOUND);
        }
        
        tender.status = Tender.TenderStatus.REJECTED;
        tender.reviewedAt = LocalDateTime.now();
        
        return tender;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Tender tender = Tender.findById(id);
        if (tender == null) {
            throw new WebApplicationException("Tender not found", Response.Status.NOT_FOUND);
        }
        
        tender.delete();
        return Response.ok(Map.of("message", "Tender deleted successfully")).build();
    }
}
