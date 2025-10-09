package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class config {

    public Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");            
            con = DriverManager.getConnection("jdbc:sqlite:gradingsystemdb.db");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

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

    
    public boolean registerUser(String username, String password, String Gmail) {
        
        String checkSql = "SELECT user_id FROM tbl_user WHERE username = ?";

        String insertSql = "INSERT INTO tbl_user(username, email, password, role, status) VALUES (?, ?, ?, 'user', 'pending')";

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
                insertStmt.setString(2,Gmail);
                insertStmt.setString(3, password); 
                int rows = insertStmt.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            System.out.println("Error in register: User " + e.getMessage());
            return false;
        }
    }

    
    public boolean loginUser(String loginUser, String loginPass) {
      
        String sql = "SELECT password FROM tbl_user WHERE email = ? AND status = 'approved'";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loginUser);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPass = rs.getString("password");
               
                return storedPass.equals(loginPass);
            } else {
              
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Error in login: User " + e.getMessage());
            return false;
        }
    }

    // Check if the user is admin
    public boolean isAdmin(String username) {
        String sql = "SELECT role FROM tbl_user WHERE email = ?";
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
    
    // ⚠️ CORRECTED IMPLEMENTATION: Use user_id from the main class argument
    public void makeTransaction(String username, int user_id, int productId, int quantity) {
        // The user_id is passed from main.java (after calling getUserId(username)), 
        // but the method logic had a redundant/incorrect call inside. 
        // I'll assume the passed 'user_id' is the correct one.
        int userId = user_id; 

        if (userId <= 0) {
            System.out.println("User ID is invalid. Please contact admin.");
            return;
        }

        String productSql = "SELECT price, stock FROM tbl_products WHERE product_id = ?";
        String insertTransactionSql = "INSERT INTO tbl_transactions(user_id, product_id, quantity, total_price, status, transaction_date) VALUES ( ?, ?, ?, ?, 'pending', CURRENT_TIMESTAMP)";
        String updateStockSql = "UPDATE tbl_products SET stock = stock - ? WHERE product_id = ?";

        try (Connection conn = this.connectDB()) {
            conn.setAutoCommit(false); // Start Transaction

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
                        insertStmt.setInt(1, userId);
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
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("User not found.");
            return;
        }

        String sql = "SELECT t.transaction_id, p.name, t.quantity, t.total_price, t.status, t.transaction_date " +
                     "FROM tbl_transactions t " +
                     "JOIN tbl_products p ON t.product_id = p.product_id " +
                     "WHERE t.user_id = ?";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("------------------------------------------------------------------------------------");
                System.out.printf("%-15s %-20s %-10s %-15s %-10s %-20s%n",
                                  "Transaction ID", "Product Name", "Quantity", "Total Price", "Status", "Date");
                System.out.println("------------------------------------------------------------------------------------");

                boolean hasTransactions = false;
                while (rs.next()) {
                    hasTransactions = true;
                    int transactionId = rs.getInt("transaction_id");
                    String productName = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    double totalPrice = rs.getDouble("total_price");
                    String status = rs.getString("status");
                    String date = rs.getString("transaction_date");

                    System.out.printf("%-15d %-20s %-10d %-15.2f %-10s %-20s%n",
                                      transactionId, productName, quantity, totalPrice, status, date);
                }

                if (!hasTransactions) {
                    System.out.println("No transactions found for user: " + username);
                }
                System.out.println("------------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error viewing transactions: " + e.getMessage());
        }
    }
    
    // ⚠️ CORRECTED IMPLEMENTATION: Updated SQL table name from tbl_transaction to tbl_transactions
    public void updateTransaction(int transactionId, int newQuantity, String newStatus, String username) {
        
        // This method currently receives 'newStatus' but in your main class the user is only prompted for newQuantity.
        // I will update the signature and logic to match the main class's use case, which only passes username twice (likely a typo, should be username and an irrelevant string, but I'll use the one for the user's name).
        
        // Corrected signature to match main.java's call: cf.updateTransaction(transactionid, newquantity, username, username);
        // I will assume the third argument is the USERNAME (to check ownership) and the fourth is irrelevant or a mistake.
        
        // **I'll redefine the method to be cleaner, assuming the user only updates quantity.**
        updateTransaction(transactionId, newQuantity, username);
    }
    
    // 🚀 NEW METHOD SIGNATURE for cleaner update (matching common use case)
    public void updateTransaction(int transactionId, int newQuantity, String username) {
        
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("User not found or not logged in.");
            return;
        }

        String selectTransactionSql = "SELECT user_id, product_id, quantity, status FROM tbl_transactions WHERE transaction_id = ?";
        String selectProductStockSql = "SELECT stock, price FROM tbl_products WHERE product_id = ?";
        String updateTransactionSql = "UPDATE tbl_transactions SET quantity = ?, total_price = ? WHERE transaction_id = ?";
        String updateProductStockSql = "UPDATE tbl_products SET stock = ? WHERE product_id = ?";

        try (Connection conn = this.connectDB()) {
            conn.setAutoCommit(false); // Start transaction

            int productId;
            int oldQuantity;
            int currentStock;
            double pricePerUnit;
            String status;
            int ownerId;

            // Get current transaction details
            try (PreparedStatement selectTransactionStmt = conn.prepareStatement(selectTransactionSql)) {
                selectTransactionStmt.setInt(1, transactionId);
                try (ResultSet rs = selectTransactionStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Transaction with ID " + transactionId + " not found.");
                        conn.rollback();
                        return;
                    }
                    ownerId = rs.getInt("user_id");
                    productId = rs.getInt("product_id");
                    oldQuantity = rs.getInt("quantity");
                    status = rs.getString("status");
                }
            }
            
            if (ownerId != userId) {
                System.out.println("Access denied. You can only update your own transactions.");
                conn.rollback();
                return;
            }
            
            if ("paid".equalsIgnoreCase(status)) {
                System.out.println("Cannot update a paid transaction.");
                conn.rollback();
                return;
            }
            
            if (newQuantity <= 0) {
                System.out.println("Quantity must be greater than zero. To remove, use the delete option.");
                conn.rollback();
                return;
            }

            // Get current product stock and price
            try (PreparedStatement selectProductStockStmt = conn.prepareStatement(selectProductStockSql)) {
                selectProductStockStmt.setInt(1, productId);
                try (ResultSet rs = selectProductStockStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Product with ID " + productId + " not found.");
                        conn.rollback();
                        return;
                    }
                    currentStock = rs.getInt("stock");
                    pricePerUnit = rs.getDouble("price");
                }
            }

            int quantityDifference = newQuantity - oldQuantity; // Positive if increased, negative if decreased.
            int newRequiredStock = currentStock - quantityDifference;

            // Check if stock is sufficient if quantity increased
            if (newRequiredStock < 0) {
                System.out.println("Insufficient stock to increase quantity. Available stock: " + currentStock);
                conn.rollback();
                return;
            }

            double newTotalPrice = pricePerUnit * newQuantity;

            // Update transaction
            try (PreparedStatement updateTransactionStmt = conn.prepareStatement(updateTransactionSql)) {
                updateTransactionStmt.setInt(1, newQuantity);
                updateTransactionStmt.setDouble(2, newTotalPrice);
                updateTransactionStmt.setInt(3, transactionId);

                int rowsUpdated = updateTransactionStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("Failed to update transaction record.");
                    conn.rollback();
                    return;
                }
            }

            // Update product stock
            try (PreparedStatement updateProductStockStmt = conn.prepareStatement(updateProductStockSql)) {
                updateProductStockStmt.setInt(1, newRequiredStock);
                updateProductStockStmt.setInt(2, productId);

                int rowsUpdated = updateProductStockStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("Failed to update product stock.");
                    conn.rollback();
                    return;
                }
            }

            conn.commit();
            System.out.println("Transaction updated successfully. New total: " + newTotalPrice);

        } catch (SQLException e) {
            System.out.println("Error updating transaction: " + e.getMessage());
            // Rollback is implicitly handled by the try-with-resources if an exception occurs
        }
    }
    
    // ⚠️ CORRECTED IMPLEMENTATION: Updated SQL table name from tbl_treansaction to tbl_transactions
    // Also re-added the quantity refund for deleted transactions
    public void deleteTransaction(int transactionId, String username) {
        
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("User not found.");
            return;
        }

        String checkOwnershipSql = "SELECT user_id, status, product_id, quantity FROM tbl_transactions WHERE transaction_id = ?";
        String deleteSql = "DELETE FROM tbl_transactions WHERE transaction_id = ?";
        String updateStockSql = "UPDATE tbl_products SET stock = stock + ? WHERE product_id = ?";


        try (Connection conn = this.connectDB()) {
            conn.setAutoCommit(false);
            
            int ownerId = -1;
            String status = null;
            int productId = -1;
            int quantity = 0;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkOwnershipSql)) {
                checkStmt.setInt(1, transactionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Transaction with ID " + transactionId + " not found.");
                        conn.rollback();
                        return;
                    }
                    ownerId = rs.getInt("user_id");
                    status = rs.getString("status");
                    productId = rs.getInt("product_id");
                    quantity = rs.getInt("quantity");

                    if (ownerId != userId) {
                        System.out.println("Access denied. You can only delete your own transactions.");
                        conn.rollback();
                        return;
                    }
                    
                    if ("paid".equalsIgnoreCase(status)) {
                        System.out.println("Cannot delete a paid transaction.");
                        conn.rollback();
                        return;
                    }
                }
            }

            // 1. Delete transaction
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, transactionId);
                int rowsDeleted = deleteStmt.executeUpdate();
                
                if (rowsDeleted == 0) {
                    System.out.println("Failed to delete transaction.");
                    conn.rollback();
                    return;
                }
            }
            
            // 2. Refund stock
            try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql)) {
                updateStockStmt.setInt(1, quantity);
                updateStockStmt.setInt(2, productId);
                
                int rowsUpdated = updateStockStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("Failed to refund product stock. Deletion failed.");
                    conn.rollback();
                    return;
                }
            }

            conn.commit();
            System.out.println("Transaction deleted and stock refunded successfully.");
            
        } catch (SQLException e) {
            System.out.println("Error deleting transaction: " + e.getMessage());
        }
    }


    // 🚀 NEW IMPLEMENTATION: Update Product
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
                System.out.println("Product with ID " + updateId + " updated successfully.");
            } else {
                System.out.println("Product with ID " + updateId + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating product: " + e.getMessage());
        }
    }

    // 🚀 NEW IMPLEMENTATION: Get User ID
    public int getUserId(String username) {
        String sql = "SELECT user_id FROM tbl_user WHERE username = ?";
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            System.out.println("Error getting user ID: " + e.getMessage());
        }
        return -1;
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

    // 🚀 NEW IMPLEMENTATION: Make Payment
    public void makePayment(String username, int transactionId) {
        int userId = getUserId(username);
        if (userId == -1) {
            System.out.println("User not found.");
            return;
        }

        String sql = "UPDATE tbl_transactions SET status = 'paid' WHERE transaction_id = ? AND user_id = ? AND status = 'pending'";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            pstmt.setInt(2, userId);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Payment for Transaction ID " + transactionId + " successful! Status set to 'paid'.");
            } else {
                // This could mean the ID wasn't found, the user doesn't own it, or it was already paid.
                System.out.println("Payment failed. Transaction ID " + transactionId + " not found, not owned by you, or already paid.");
            }

        } catch (SQLException e) {
            System.out.println("Error making payment: " + e.getMessage());
        }
    }

    public void viewPendingUsers() {
        // Assume a 'status' column exists in tbl_user
        String sql = "SELECT username FROM tbl_user WHERE status = 'pending'";
        try (Connection conn = connectDB(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("Pending Users:");
            boolean hasPending = false;
            while (rs.next()) {
                hasPending = true;
                System.out.println("- Username: " + rs.getString("username"));
            }
            if (!hasPending) {
                System.out.println("No pending users.");
            }
        } catch (SQLException e) {
            System.out.println("Error viewing pending users: " + e.getMessage());
        }
    }
    
    public boolean approveUser(String username) {
        // Assume a 'status' column exists in tbl_user
        String sql = "UPDATE tbl_user SET status = 'approved' WHERE username = ? AND status = 'pending'";
        try (Connection conn = connectDB(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.out.println("Error approving user: " + e.getMessage());
            return false;
        }
    }
 public void adminDeleteTransaction(int transactionId, String adminUsername) {
    String deleteSql = "DELETE FROM tbl_transactions WHERE transaction_id = ?";
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
        // 1. Get database connection
        conn = connectDB(); // Assumes connectDB() handles the connection details

        if (conn == null) {
            System.out.println("❌ Database connection failed. Cannot delete transaction.");
            return;
        }

        // 2. Optional: Check if the transaction exists before attempting to delete
        if (!checkTransactionExists(conn, transactionId)) {
            System.out.println("⚠️ Error: Transaction ID " + transactionId + " does not exist.");
            return;
        }

        // 3. Prepare and execute the DELETE statement
        pstmt = conn.prepareStatement(deleteSql);
        pstmt.setInt(1, transactionId);

        int rowsAffected = pstmt.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("✅ ADMIN ACTION: Transaction ID " + transactionId + " successfully deleted by " + adminUsername + ".");
        } else {
            System.out.println("Error: Failed to delete transaction ID " + transactionId + ".");
        }

    } catch (SQLException e) {
        System.out.println("SQL Error during admin transaction deletion: " + e.getMessage());
    } finally {
        // 4. Close resources
        try {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }
}

private boolean checkTransactionExists(Connection conn, int transactionId) throws SQLException {
    String checkSql = "SELECT COUNT(*) FROM tbl_transactions WHERE transaction_id = ?";
    try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
        checkPstmt.setInt(1, transactionId);
        try (ResultSet rs = checkPstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    }
    
    return false;
}
  public void viewUsers() {
    String sql = "SELECT user_id, username, email, status, role FROM tbl_user ORDER BY user_id";

    System.out.println("\n------------------------------------------------------------------");
    System.out.println("                         ALL REGISTERED USERS");
    System.out.println("------------------------------------------------------------------");
    System.out.printf("| %-4s | %-15s | %-25s | %-10s | %-8s |\n", "ID", "USERNAME", "EMAIL", "STATUS", "ROLE");
    System.out.println("------------------------------------------------------------------");

    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        boolean hasUsers = false;
        while (rs.next()) {
            hasUsers = true;
            int id = rs.getInt("user_id");
            String username = rs.getString("username");
           
            String email = rs.getString("email");
            
            String status = rs.getString("status");
            String role = rs.getString("role");

            //
            String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1); 

            System.out.printf("| %-4d | %-15s | %-25s | %-10s | %-8s |\n", id, username, email, displayStatus, role.toUpperCase());
        }

        if (!hasUsers) {
            System.out.println("| No users found.                                                  |");
        }

        System.out.println("------------------------------------------------------------------");

    } catch (SQLException e) { // ⚠️ BETTER: Catch SQLException instead of generic Exception for better error handling
        System.out.println("❌ Database Error: Could not retrieve user list. " + e.getMessage());
    }
}
    public boolean makeUserAdmin(String targetUsername, String adminUsername) {
       
        String sql = "UPDATE users SET role = 'admin' WHERE username = ? AND role != 'admin'";

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, targetUsername);

            // executeUpdate returns the number of rows affected
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Log the action for auditing purposes
                System.out.println("✅ Audit Log: Admin " + adminUsername + " successfully promoted user " + targetUsername + ".");
                return true;
            } else {
                // If 0 rows were affected, the user was either not found or already an admin.
                System.out.println("⚠️ Promotion failed: User '" + targetUsername + "' not found, or is already an admin.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Database Error during Admin Promotion: " + e.getMessage());
            
            return false;
        }
    }
}
