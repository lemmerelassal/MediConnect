package com.pharma.service;

import com.pharma.entity.Shortage;
import com.pharma.entity.Tender;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnalyticsService {

    @Inject
    EntityManager em;

    public Map<String, Object> getShortageAnalytics(String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();

        // Basic counts
        long totalShortages = Shortage.count();
        long activeShortages = Shortage.count("status", Shortage.ShortageStatus.ACTIVE);
        long fulfilledShortages = Shortage.count("status", Shortage.ShortageStatus.FULFILLED);

        analytics.put("total_shortages", totalShortages);
        analytics.put("active_shortages", activeShortages);
        analytics.put("fulfilled_shortages", fulfilledShortages);

        // Average fulfillment time
        Double avgFulfillmentTime = em.createQuery(
            "SELECT AVG(EXTRACT(EPOCH FROM (s.updatedAt - s.createdAt)) / 3600) " +
            "FROM Shortage s WHERE s.status = 'FULFILLED'",
            Double.class
        ).getSingleResult();
        analytics.put("avg_fulfillment_time_hours", avgFulfillmentTime != null ? avgFulfillmentTime : 0.0);

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
        analytics.put("shortages_by_urgency", urgencyMap);

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
        analytics.put("shortages_by_country", countryMap);

        // Time series data
        List<Object[]> timeSeries = em.createQuery(
            "SELECT DATE(s.createdAt), COUNT(s), s.urgencyLevel " +
            "FROM Shortage s " +
            "WHERE s.createdAt BETWEEN :start AND :end " +
            "GROUP BY DATE(s.createdAt), s.urgencyLevel " +
            "ORDER BY DATE(s.createdAt)",
            Object[].class
        ).setParameter("start", startDate.atStartOfDay())
         .setParameter("end", endDate.atTime(23, 59, 59))
         .getResultList();

        List<Map<String, Object>> timeSeriesList = timeSeries.stream()
            .map(row -> {
                Map<String, Object> point = new HashMap<>();
                point.put("date", row[0].toString());
                point.put("count", row[1]);
                point.put("urgency", row[2].toString());
                return point;
            })
            .collect(Collectors.toList());
        analytics.put("time_series", timeSeriesList);

        // Top medications by shortage count
        List<Object[]> topMeds = em.createQuery(
            "SELECT m.id, m.genericName, COUNT(s), SUM(s.quantityNeeded) " +
            "FROM Shortage s JOIN s.medication m " +
            "GROUP BY m.id, m.genericName " +
            "ORDER BY COUNT(s) DESC",
            Object[].class
        ).setMaxResults(10)
         .getResultList();

        List<Map<String, Object>> topMedsList = topMeds.stream()
            .map(row -> {
                Map<String, Object> med = new HashMap<>();
                med.put("medication_id", row[0]);
                med.put("medication_name", row[1]);
                med.put("shortage_count", row[2]);
                med.put("total_quantity_needed", row[3]);
                return med;
            })
            .collect(Collectors.toList());
        analytics.put("top_medications", topMedsList);

        return analytics;
    }

    public Map<String, Object> getTenderAnalytics(String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();

        // Basic counts
        long totalTenders = Tender.count();
        long acceptedTenders = Tender.count("status", Tender.TenderStatus.ACCEPTED);
        long rejectedTenders = Tender.count("status", Tender.TenderStatus.REJECTED);

        analytics.put("total_tenders", totalTenders);
        analytics.put("accepted_tenders", acceptedTenders);
        analytics.put("rejected_tenders", rejectedTenders);

        // Average response time
        Double avgResponseTime = em.createQuery(
            "SELECT AVG(EXTRACT(EPOCH FROM (t.reviewedAt - t.createdAt)) / 3600) " +
            "FROM Tender t WHERE t.reviewedAt IS NOT NULL",
            Double.class
        ).getSingleResult();
        analytics.put("avg_response_time_hours", avgResponseTime != null ? avgResponseTime : 0.0);

        // Average tender value
        Double avgValue = em.createQuery(
            "SELECT AVG(t.pricePerUnit * t.quantityOffered) FROM Tender t",
            Double.class
        ).getSingleResult();
        analytics.put("avg_tender_value", avgValue != null ? avgValue : 0.0);

        // Tenders by status
        List<Object[]> byStatus = em.createQuery(
            "SELECT t.status, COUNT(t) FROM Tender t GROUP BY t.status",
            Object[].class
        ).getResultList();
        
        Map<String, Long> statusMap = byStatus.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> (Long) row[1]
            ));
        analytics.put("tenders_by_status", statusMap);

        // Time series
        List<Object[]> timeSeries = em.createQuery(
            "SELECT DATE(t.createdAt), COUNT(t), AVG(t.pricePerUnit * t.quantityOffered) " +
            "FROM Tender t " +
            "WHERE t.createdAt BETWEEN :start AND :end " +
            "GROUP BY DATE(t.createdAt) " +
            "ORDER BY DATE(t.createdAt)",
            Object[].class
        ).setParameter("start", startDate.atStartOfDay())
         .setParameter("end", endDate.atTime(23, 59, 59))
         .getResultList();

        List<Map<String, Object>> timeSeriesList = timeSeries.stream()
            .map(row -> {
                Map<String, Object> point = new HashMap<>();
                point.put("date", row[0].toString());
                point.put("count", row[1]);
                point.put("avg_value", row[2]);
                return point;
            })
            .collect(Collectors.toList());
        analytics.put("time_series", timeSeriesList);

        // Top suppliers
        List<Object[]> topSuppliers = em.createQuery(
            "SELECT c.id, c.name, COUNT(t), SUM(t.pricePerUnit * t.quantityOffered), " +
            "CAST(SUM(CASE WHEN t.status = 'ACCEPTED' THEN 1 ELSE 0 END) AS double) / COUNT(t) * 100 " +
            "FROM Tender t JOIN t.supplierCountry c " +
            "GROUP BY c.id, c.name " +
            "ORDER BY COUNT(t) DESC",
            Object[].class
        ).setMaxResults(10)
         .getResultList();

        List<Map<String, Object>> topSuppliersList = topSuppliers.stream()
            .map(row -> {
                Map<String, Object> supplier = new HashMap<>();
                supplier.put("country_id", row[0]);
                supplier.put("country_name", row[1]);
                supplier.put("tender_count", row[2]);
                supplier.put("total_value", row[3]);
                supplier.put("acceptance_rate", row[4]);
                return supplier;
            })
            .collect(Collectors.toList());
        analytics.put("top_suppliers", topSuppliersList);

        return analytics;
    }

    public Map<String, Object> getCountryAnalytics(Long countryId, String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();

        // Shortages reported by this country
        Long shortagesReported = Shortage.count("country.id", countryId);
        analytics.put("total_shortages_reported", shortagesReported);

        // Tenders submitted by this country
        Long tendersSubmitted = Tender.count("supplierCountry.id", countryId);
        analytics.put("total_tenders_submitted", tendersSubmitted);

        // Tenders received for this country's shortages
        Long tendersReceived = em.createQuery(
            "SELECT COUNT(t) FROM Tender t JOIN t.shortage s WHERE s.country.id = :countryId",
            Long.class
        ).setParameter("countryId", countryId)
         .getSingleResult();
        analytics.put("total_tenders_received", tendersReceived);

        // Fulfillment rate
        Double fulfillmentRate = em.createQuery(
            "SELECT CAST(COUNT(CASE WHEN s.status = 'FULFILLED' THEN 1 END) AS double) / COUNT(*) * 100 " +
            "FROM Shortage s WHERE s.country.id = :countryId",
            Double.class
        ).setParameter("countryId", countryId)
         .getSingleResult();
        analytics.put("fulfillment_rate", fulfillmentRate != null ? fulfillmentRate : 0.0);

        // Total value traded
        Double totalValue = em.createQuery(
            "SELECT SUM(t.pricePerUnit * t.quantityOffered) " +
            "FROM Tender t WHERE t.supplierCountry.id = :countryId AND t.status = 'ACCEPTED'",
            Double.class
        ).setParameter("countryId", countryId)
         .getSingleResult();
        analytics.put("total_value_traded", totalValue != null ? totalValue : 0.0);

        return analytics;
    }
}
