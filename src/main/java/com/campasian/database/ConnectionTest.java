package com.campasian.database;

import com.campasian.config.DatabaseConfig;

import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Standalone connectivity test for the Supabase PostgreSQL database.
 * <p>
 * Run with: {@code gradlew run "-PmainClass=com.campasian.database.ConnectionTest"}
 * <p>
 * No extra module-info permissions needed for networking—{@code java.net} and
 * {@code java.sql} are in java.base (implicit). UnknownHostException usually means
 * DNS/network unreachable (firewall, VPN, or host down).
 */
public final class ConnectionTest {

    public static void main(String[] args) {
        String host = DatabaseConfig.HOST;
        int port = DatabaseConfig.PORT;

        System.out.println("=== Campasian Database Connection Test ===");
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        System.out.println();

        boolean dnsOk = testDns(host);
        boolean socketOk = testSocket(host, port);
        boolean jdbcOk = testJdbc();

        System.out.println();
        System.out.println("--- Summary ---");
        System.out.println("DNS resolution:   " + (dnsOk ? "OK" : "FAILED"));
        System.out.println("Socket connect:   " + (socketOk ? "OK" : "FAILED"));
        System.out.println("JDBC connection:  " + (jdbcOk ? "OK" : "FAILED"));
        System.out.println();
        System.out.println(jdbcOk ? "Database is reachable. Connection test passed."
            : "Database unreachable. Check network, firewall, VPN, or Supabase status.");
    }

    private static boolean testDns(String host) {
        System.out.print("1. DNS resolution... ");
        try {
            InetAddress addr = InetAddress.getByName(host);
            System.out.println("OK (" + addr.getHostAddress() + ")");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    private static boolean testSocket(String host, int port) {
        System.out.print("2. Socket to " + host + ":" + port + "... ");
        try (Socket s = new Socket()) {
            s.connect(new java.net.InetSocketAddress(host, port), 5000);
            System.out.println("OK");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return false;
        }
    }

    private static boolean testJdbc() {
        System.out.print("3. JDBC connection... ");
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.getJdbcUrl(),
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD)) {
            System.out.println("OK");
            return true;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            return false;
        }
    }
}
