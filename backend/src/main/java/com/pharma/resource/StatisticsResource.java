package com.pharma.resource;

import com.pharma.entity.Shortage;
import com.pharma.entity.Tender;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/statistics")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class StatisticsResource {

    @Inject
    EntityManager em;

    @GET
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Basic counts
        stats.put("total_shortages", Shortage.count());
        stats.put("active_shortages", Shortage.count("status", Shortage.ShortageStatus.ACTIVE));
        stats.put("total_tenders", Tender.count());
        stats.put("pending_tenders", Tender.count("status", Tender.TenderStatus.PENDING));

        // Shortages by urgency
        List<Object[]> byUrgency = em.createQuery(
            "SELECT s.urgencyLevel, COUNT(s) FROM Shortage s GROUP BY s.urgencyLevel",
            Object[].class
        ).getResultList();
        
        Map<String, Long> urgencyMap = byUrgency.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
        stats.put("shortages_by_urgency", urgencyMap);

        // Shortages by country
        List<Object[]> byCountry = em.createQuery(
            "SELECT c.name, COUNT(s) FROM Shortage s JOIN s.country c GROUP BY c.name",
            Object[].class
        ).getResultList();
        
        Map<String, Long> countryMap = byCountry.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1]
            ));
        stats.put("shortages_by_country", countryMap);

        return stats;
    }
}
