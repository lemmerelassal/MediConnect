package com.pharma.resource;

import com.pharma.entity.Document;
import com.pharma.service.FileUploadService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/documents")
@Authenticated
public class DocumentResource {

    @Inject
    FileUploadService fileUploadService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadDocument(@MultipartForm DocumentUploadForm form) {
        try {
            Long userId = Long.parseLong(jwt.getClaim("userId").toString());
            
            Document document = fileUploadService.uploadDocument(
                form.file,
                form.tenderId,
                form.documentType,
                form.description,
                userId
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", document.id);
            response.put("uuid", document.uuid.toString());
            response.put("fileName", document.fileName);
            response.put("fileSize", document.fileSize);
            response.put("documentType", document.documentType.name());

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDocument(@PathParam("uuid") String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            Document document = Document.findByUuid(uuid);
            
            if (document == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            File file = fileUploadService.getDocument(uuid);

            return Response.ok(file)
                    .header("Content-Disposition", "attachment; filename=\"" + document.fileName + "\"")
                    .header("Content-Type", document.mimeType)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/tender/{tenderId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> getDocumentsByTender(@PathParam("tenderId") Long tenderId) {
        List<Document> documents = Document.find("tender.id", tenderId).list();
        
        return documents.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.id);
            map.put("uuid", doc.uuid.toString());
            map.put("fileName", doc.fileName);
            map.put("fileSize", doc.fileSize);
            map.put("mimeType", doc.mimeType);
            map.put("documentType", doc.documentType.name());
            map.put("description", doc.description);
            map.put("verified", doc.verified);
            map.put("createdAt", doc.createdAt);
            return map;
        }).toList();
    }

    @DELETE
    @Path("/{uuid}")
    public Response deleteDocument(@PathParam("uuid") String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            fileUploadService.deleteDocument(uuid);
            return Response.ok(Map.of("message", "Document deleted successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    public static class DocumentUploadForm {
        @FormParam("file")
        public FileUpload file;

        @FormParam("tenderId")
        public Long tenderId;

        @FormParam("documentType")
        public String documentType;

        @FormParam("description")
        public String description;
    }
}
