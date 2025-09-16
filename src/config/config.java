package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class config {

    // Connect to SQLite database
    public Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:gradingsystemdb.db"); // Establish connection
            System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

    // Generic method to add records with variable arguments
    public void addRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) values[i]);
                } else if (values[i] instanceof Double) {
                    pstmt.setDouble(i + 1, (Double) values[i]);
                } else if (values[i] instanceof Float) {
                    pstmt.setFloat(i + 1, (Float) values[i]);
                } else if (values[i] instanceof Long) {
                    pstmt.setLong(i + 1, (Long) values[i]);
                } else if (values[i] instanceof Boolean) {
                    pstmt.setBoolean(i + 1, (Boolean) values[i]);
                } else if (values[i] instanceof java.util.Date) {
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
                } else if (values[i] instanceof java.sql.Date) {
                    pstmt.setDate(i + 1, (java.sql.Date) values[i]);
                } else if (values[i] instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);
                } else {
                    pstmt.setString(i + 1, values[i].toString());
                }
            }

            pstmt.executeUpdate();
            System.out.println("Record added successfully!");
        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

    // Register a new user if username does not exist
    public boolean registerUser (String username, String password) {
        String checkSql = "SELECT user_id FROM tbl_user WHERE username = ?";
        String insertSql = "INSERT INTO tbl_user(username, password, role) VALUES (?, ?, 'user')";

        try (Connection conn = this.connectDB();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Username already exists
                return false;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password); // For production, hash the password!
                int rows = insertStmt.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error in register:User  " + e.getMessage());
            return false;
        }
    }

    // Login user by verifying username and password
    public boolean loginUser (String loginUser , String loginPass) {
        String sql = "SELECT password FROM tbl_user WHERE username = ?";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginUser );
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPass = rs.getString("password");
                // For production, compare hashed passwords
                return storedPass.equals(loginPass);
            } else {
                // Username not found
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error in login:User  " + e.getMessage());
            return false;
        }
    }

    // Check if the user is admin
    public boolean isAdmin(String username) {
        String sql = "SELECT role FROM tbl_user WHERE username = ?";
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                return "admin".equalsIgnoreCase(role);
            }
        } catch (SQLException e) {
            System.out.println("Error checking admin role: " + e.getMessage());
        }
        return false;
    }

    // Add product - only admin can add products
    public boolean addProduct(String name, double price, int stock, String username) {
        if (!isAdmin(username)) {
            System.out.println("Access denied. Only admin can add products.");
            return false;
        }

        String sql = "INSERT INTO tbl_products(name, price, stock) VALUES (?, ?, ?)";
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Product added successfully.");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
        return false;
    }

    // Placeholder methods to implement later
    public void viewProducts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void makeTransaction(String username, int productId, int quantity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void makePayment(String username, int transactionId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateProduct(int updateId, String newName, double newPrice, int newStock, String username) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void deleteProduct(int deleteId, String username) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}