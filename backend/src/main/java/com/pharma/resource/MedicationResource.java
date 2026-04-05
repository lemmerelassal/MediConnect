package com.pharma.resource;

import com.pharma.entity.Medication;
import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/medications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MedicationResource {

    @GET
    public List<Medication> list() {
        return Medication.listAll();
    }

    @GET
    @Path("/{id}")
    public Medication get(@PathParam("id") Long id) {
        Medication medication = Medication.findById(id);
        if (medication == null) {
            throw new WebApplicationException("Medication not found", Response.Status.NOT_FOUND);
        }
        return medication;
    }

    @POST
    @Transactional
    @Authenticated
    public Response create(Medication medication) {
        medication.persist();
        return Response.status(Response.Status.CREATED).entity(medication).build();
    }
}
