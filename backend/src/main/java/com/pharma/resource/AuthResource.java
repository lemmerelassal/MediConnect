package com.pharma.resource;

import com.pharma.entity.Country;
import com.pharma.entity.User;
import com.pharma.service.TokenService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    TokenService tokenService;

    @POST
    @Path("/login")
    public Response login(Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        User user = User.findByEmail(email);
        
        if (user == null || !user.isActive) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid credentials"))
                    .build();
        }

        if (!BCrypt.checkpw(password, user.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid credentials"))
                    .build();
        }

        updateLastLogin(user);

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", Map.of(
            "id", user.id,
            "email", user.email,
            "firstName", user.firstName != null ? user.firstName : "",
            "lastName", user.lastName != null ? user.lastName : "",
            "role", user.role,
            "countryId", user.country != null ? user.country.id : null
        ));

        return Response.ok(response).build();
    }

    @POST
    @Path("/register")
    @Transactional
    public Response register(Map<String, Object> data) {
        String email = (String) data.get("email");
        String password = (String) data.get("password");
        String firstName = (String) data.get("firstName");
        String lastName = (String) data.get("lastName");
        String role = (String) data.get("role");
        Long countryId = data.get("countryId") != null ? 
            ((Number) data.get("countryId")).longValue() : null;

        // Check if user already exists
        if (User.findByEmail(email) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "User already exists"))
                    .build();
        }

        // Create new user
        User user = new User();
        user.email = email;
        user.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        user.firstName = firstName;
        user.lastName = lastName;
        user.role = role != null ? role : "VIEWER";
        
        if (countryId != null) {
            user.country = Country.findById(countryId);
        }

        user.persist();

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user", Map.of(
            "id", user.id,
            "email", user.email,
            "firstName", user.firstName != null ? user.firstName : "",
            "lastName", user.lastName != null ? user.lastName : "",
            "role", user.role,
            "countryId", user.country != null ? user.country.id : null
        ));

        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/refresh")
    public Response refresh(Map<String, String> data) {
        String refreshToken = data.get("refreshToken");
        
        // In production, validate the refresh token and check if it's in the database
        // For now, we'll just generate a new access token
        
        try {
            // TODO: Validate refresh token
            // For demo, we'll just return an error
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Token refresh not implemented"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid refresh token"))
                    .build();
        }
    }

    @Transactional
    void updateLastLogin(User user) {
        user.lastLogin = LocalDateTime.now();
    }
}
