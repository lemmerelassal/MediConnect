package com.pharma.service;

import com.pharma.entity.Shortage;
import com.pharma.entity.Tender;
import com.pharma.entity.User;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @Inject
    Template shortageCreatedEmail;

    @Inject
    Template tenderSubmittedEmail;

    @Inject
    Template tenderAcceptedEmail;

    @Inject
    Template tenderRejectedEmail;

    @ConfigProperty(name = "email.templates.base-url")
    String baseUrl;

    public void sendShortageCreatedEmail(Shortage shortage, User recipient) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("recipientName", recipient.firstName + " " + recipient.lastName);
            data.put("medicationName", shortage.medication.genericName);
            data.put("country", shortage.country.name);
            data.put("quantity", shortage.quantityNeeded);
            data.put("unit", shortage.unit);
            data.put("urgency", shortage.urgencyLevel.name());
            data.put("shortageUrl", baseUrl + "/shortages/" + shortage.id);

            String html = shortageCreatedEmail.data(data).render();

            mailer.send(Mail.withHtml(
                recipient.email,
                "New Medication Shortage: " + shortage.medication.genericName,
                html
            ));
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendTenderSubmittedEmail(Tender tender, User recipient) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("recipientName", recipient.firstName + " " + recipient.lastName);
            data.put("medicationName", tender.shortage.medication.genericName);
            data.put("supplierCountry", tender.supplierCountry.name);
            data.put("quantity", tender.quantityOffered);
            data.put("price", tender.pricePerUnit);
            data.put("currency", tender.currency);
            data.put("tenderUrl", baseUrl + "/shortages/" + tender.shortage.id);

            String html = tenderSubmittedEmail.data(data).render();

            mailer.send(Mail.withHtml(
                recipient.email,
                "New Tender Submitted for " + tender.shortage.medication.genericName,
                html
            ));
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendTenderAcceptedEmail(Tender tender, User supplier) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("recipientName", supplier.firstName + " " + supplier.lastName);
            data.put("medicationName", tender.shortage.medication.genericName);
            data.put("quantity", tender.quantityOffered);
            data.put("totalValue", tender.quantityOffered * tender.pricePerUnit.doubleValue());
            data.put("currency", tender.currency);
            data.put("tenderUrl", baseUrl + "/shortages/" + tender.shortage.id);

            String html = tenderAcceptedEmail.data(data).render();

            mailer.send(Mail.withHtml(
                supplier.email,
                "Tender Accepted - " + tender.shortage.medication.genericName,
                html
            ));
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendTenderRejectedEmail(Tender tender, User supplier) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("recipientName", supplier.firstName + " " + supplier.lastName);
            data.put("medicationName", tender.shortage.medication.genericName);
            data.put("tenderUrl", baseUrl + "/shortages/" + tender.shortage.id);

            String html = tenderRejectedEmail.data(data).render();

            mailer.send(Mail.withHtml(
                supplier.email,
                "Tender Update - " + tender.shortage.medication.genericName,
                html
            ));
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
