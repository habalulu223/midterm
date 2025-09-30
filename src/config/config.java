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

    public void viewProducts() {
    String sql = "SELECT product_id, name, price, stock FROM tbl_products";
    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        System.out.println("-------------------------------------------------");
        System.out.printf("%-10s %-20s %-10s %-10s%n", "Product ID", "Name", "Price", "Stock");
        System.out.println("-------------------------------------------------");
        boolean hasProducts = false;
        while (rs.next()) {
            hasProducts = true;
            int productId = rs.getInt("product_id");
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            int stock = rs.getInt("stock");
            System.out.printf("%-10d %-20s %-10.2f %-10d%n", productId, name, price, stock);
        }
        if (!hasProducts) {
            System.out.println("No products available.");
        }
        System.out.println("-------------------------------------------------");
    } catch (SQLException e) {
        System.out.println("Error viewing products: " + e.getMessage());
    }
}

    public void makeTransaction(String username,int user_id, int productId, int quantity) {
    String usersql = "SELECT user_id From tbl_user = ?";
    String productSql = "SELECT price, stock FROM tbl_products WHERE product_id = ?";
    String insertTransactionSql = "INSERT INTO tbl_transactions(username, product_id, quantity, total_price, status, transaction_date) VALUES (?, ?, ?, ?, 'pending', CURRENT_TIMESTAMP)";
    String updateStockSql = "UPDATE tbl_products SET stock = stock - ? WHERE product_id = ?";

    try (Connection conn = this.connectDB()) {
        conn.setAutoCommit(false); // Start transaction

        // Check product availability
        try (PreparedStatement productStmt = conn.prepareStatement(productSql)) {
            productStmt.setInt(1, productId);
            try (ResultSet rs = productStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Product not found.");
                    conn.rollback();
                    return;
                }

                double price = rs.getDouble("price");
                int stock = rs.getInt("stock");

                if (stock < quantity) {
                    System.out.println("Insufficient stock. Available: " + stock);
                    conn.rollback();
                    return;
                }

                double totalPrice = price * quantity;

                // Insert transaction
                try (PreparedStatement insertStmt = conn.prepareStatement(insertTransactionSql)) {
                    insertStmt.setString(1, username);
                    insertStmt.setInt(2, productId);
                    insertStmt.setInt(3, quantity);
                    insertStmt.setDouble(4, totalPrice);

                    int rowsInserted = insertStmt.executeUpdate();
                    if (rowsInserted == 0) {
                        System.out.println("Failed to create transaction.");
                        conn.rollback();
                        return;
                    }
                }

                // Update product stock
                try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql)) {
                    updateStockStmt.setInt(1, quantity);
                    updateStockStmt.setInt(2, productId);

                    int rowsUpdated = updateStockStmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        System.out.println("Failed to update product stock.");
                        conn.rollback();
                        return;
                    }
                }

                conn.commit();
                System.out.println("Transaction successful! Total price: " + totalPrice);
            }
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error during transaction: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        System.out.println("Database error: " + e.getMessage());
    }
}
public void viewTransactions(String username) {
    String sql = "SELECT t.transaction_id, p.name AS product_name, t.quantity, t.total_price, t.status, t.transaction_date " +
                 "FROM tbl_transactions t " +
                 "JOIN tbl_products p ON t.product_id = p.product_id " +
                 "JOIN tbl_user u ON t.user_id = u.user_id " +
                 "WHERE u.username = ? " +
                 "ORDER BY t.transaction_date DESC";

    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);

        try (ResultSet rs = pstmt.executeQuery()) {
            System.out.println("\nTransactions for user: " + username);
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %-10s %-15s %-12s %-20s%n", 
                              "Transaction ID", "Product Name", "Quantity", "Total Price", "Status", "Date");
            System.out.println("--------------------------------------------------------------------------------");

            boolean hasTransactions = false;
            while (rs.next()) {
                hasTransactions = true;
                int transactionId = rs.getInt("transaction_id");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double totalPrice = rs.getDouble("total_price");
                String status = rs.getString("status");
                String date = rs.getString("transaction_date");

                System.out.printf("%-15d %-20s %-10d %-15.2f %-12s %-20s%n",
                                  transactionId, productName, quantity, totalPrice, status, date);
            }

            if (!hasTransactions) {
                System.out.println("No transactions found.");
            }
            System.out.println("--------------------------------------------------------------------------------");
        }

    } catch (SQLException e) {
        System.out.println("Error viewing transactions: " + e.getMessage());
    }
}


    public void makePayment(String username, int transactionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void updateProduct(int updateId, String newName, double newPrice, int newStock, String username) {
    if (!isAdmin(username)) {
        System.out.println("Access denied. Only admin can update products.");
        return;
    }

    String sql = "UPDATE tbl_products SET name = ?, price = ?, stock = ? WHERE product_id = ?";

    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, newName);
        pstmt.setDouble(2, newPrice);
        pstmt.setInt(3, newStock);
        pstmt.setInt(4, updateId);

        int rowsUpdated = pstmt.executeUpdate();

        if (rowsUpdated > 0) {
            System.out.println("Product updated successfully.");
        } else {
            System.out.println("Product with ID " + updateId + " not found.");
        }

    } catch (SQLException e) {
        System.out.println("Error updating product: " + e.getMessage());
    }
}
public void deleteProduct(int deleteId, String username) {
    if (!isAdmin(username)) {
        System.out.println("Access denied. Only admin can delete products.");
        return;
    }
    String sql = "DELETE FROM tbl_products WHERE product_id = ?";
    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, deleteId);
        int rowsDeleted = pstmt.executeUpdate();
        if (rowsDeleted > 0) {
            System.out.println("Product deleted successfully.");
        } else {
            System.out.println("Product with ID " + deleteId + " not found.");
        }
    } catch (SQLException e) {
        System.out.println("Error deleting product: " + e.getMessage());
    }
}
}