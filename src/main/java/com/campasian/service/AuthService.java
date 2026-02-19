package com.campasian.service;

import com.campasian.database.DatabaseManager;
import com.campasian.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles authentication: signup, login, password hashing, and EIN-to-university mapping.
 */
public final class AuthService {

    private static final AuthService INSTANCE = new AuthService();

    private AuthService() {}

    public static AuthService getInstance() {
        return INSTANCE;
    }

    /**
     * Maps EIN prefix to university name. Extend as needed for real EIN schemes.
     */
    public String resolveUniversityByEin(String ein) {
        if (ein == null || ein.isBlank()) return "";
        String prefix = ein.trim().substring(0, Math.min(3, ein.trim().length()));
        return switch (prefix) {
            case "101" -> "AIUB";
            case "102" -> "BUET";
            case "103" -> "DU";
            case "104" -> "NSU";
            case "105" -> "BRAC";
            case "106" -> "EWU";
            case "107" -> "IUB";
            case "108" -> "SUB";
            default -> "Unknown University";
        };
    }

    /**
     * Registers a new user. Validates and hashes password before saving.
     */
    public void signup(String fullName, String email, String einNumber, String department,
                       String password) throws SQLException {
        String universityName = resolveUniversityByEin(einNumber);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String sql = """
            INSERT INTO users (full_name, email, ein_number, university_name, department, password)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, einNumber);
            stmt.setString(4, universityName);
            stmt.setString(5, department);
            stmt.setString(6, hashedPassword);
            stmt.executeUpdate();
        }
    }

    /**
     * Verifies credentials and returns the user if valid.
     */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT id, full_name, email, ein_number, university_name, department, password, created_at FROM users WHERE email = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;

            String storedHash = rs.getString("password");
            if (!BCrypt.checkpw(password, storedHash)) return null;

            User user = new User();
            user.setId(rs.getInt("id"));
            user.setFullName(rs.getString("full_name"));
            user.setEmail(rs.getString("email"));
            user.setEinNumber(rs.getString("ein_number"));
            user.setUniversityName(rs.getString("university_name"));
            user.setDepartment(rs.getString("department"));
            user.setPasswordHash(storedHash);
            var ts = rs.getTimestamp("created_at");
            if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
            return user;
        }
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}
