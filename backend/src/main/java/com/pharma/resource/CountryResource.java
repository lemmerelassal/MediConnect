package com.pharma.resource;

import com.pharma.entity.Country;
import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/countries")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CountryResource {

    @GET
    public List<Country> list() {
        return Country.listAll();
    }

    @GET
    @Path("/{id}")
    public Country get(@PathParam("id") Long id) {
        Country country = Country.findById(id);
        if (country == null) {
            throw new WebApplicationException("Country not found", Response.Status.NOT_FOUND);
        }
        return country;
    }

    @POST
    @Transactional
    @Authenticated
    public Response create(Country country) {
        country.persist();
        return Response.status(Response.Status.CREATED).entity(country).build();
    }
}
