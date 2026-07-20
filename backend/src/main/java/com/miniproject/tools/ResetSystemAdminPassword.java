package com.miniproject.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * One-off: reset admin@system.local password.
 * Run: mvnw -q -DskipTests exec:java -Dexec.mainClass=com.miniproject.tools.ResetSystemAdminPassword
 */
public final class ResetSystemAdminPassword {

    public static void main(String[] args) throws Exception {
        String password = args.length > 0 ? args[0] : "roqkfwk!23";
        String email = "admin@system.local";
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/postgres");
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "admin");

        String encoded = new BCryptPasswordEncoder().encode(password);

        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, dbPassword);
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE pjt_users SET password = ?, password_changed_at = NOW(), enabled = TRUE, failed_login_attempts = 0 WHERE email = ?"
             )) {
            ps.setString(1, encoded);
            ps.setString(2, email);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                System.err.println("No user found for email: " + email);
                System.exit(1);
            }
            System.out.println("Password reset OK for " + email + " (rows=" + updated + ")");
        }
    }

    private ResetSystemAdminPassword() {}
}
