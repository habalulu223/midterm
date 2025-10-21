package dbConnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Manages all database connections and business logic for the Retail System.
 * Uses SQLite (shop_system.db) for storage.
 */
public class dbConnect {
    
    private static final String DB_URL = "jdbc:sqlite:shop_system.db";

    // --- CORE CONNECTION & SETUP ---
    
    /**
     * Establishes a connection to the SQLite database.
     * @return A valid Connection object, or null if connection fails.
     */
    public static Connection connectDB() {
        Connection con = null;
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC"); 
            // Establish connection to the 'shop_system.db' file
            con = DriverManager.getConnection(DB_URL); 
        } catch (Exception e) {
            System.err.println("Connection Failed: " + e.getMessage());
        }
        return con;
    }
    
    /**
     * Creates all necessary tables for the system: tbl_user, tbl_product, tbl_customer, and tbl_transaction.
     */
    public void createTables() {
        String sqlUser = "CREATE TABLE IF NOT EXISTS tbl_user ("
                + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "email TEXT NOT NULL UNIQUE,"
                + "is_admin INTEGER DEFAULT 0," // 0=User, 1=Admin
                + "is_approved INTEGER DEFAULT 0" // 0=Pending, 1=Approved
                + ");";
        
        String sqlProduct = "CREATE TABLE IF NOT EXISTS tbl_product ("
                + "p_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "p_name TEXT NOT NULL UNIQUE,"
                + "p_price REAL NOT NULL,"
                + "p_stock INTEGER NOT NULL"
                + ");";
                
        String sqlCustomer = "CREATE TABLE IF NOT EXISTS tbl_customer ("
                + "c_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "c_fname TEXT NOT NULL,"
                + "c_lname TEXT NOT NULL,"
                + "c_email TEXT UNIQUE,"
                + "c_status TEXT"
                + ");";
                
        String sqlTransaction = "CREATE TABLE IF NOT EXISTS tbl_transaction ("
                + "t_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL," 
                + "product_id INTEGER NOT NULL," 
                + "quantity INTEGER NOT NULL,"
                + "total_amount REAL NOT NULL,"
                + "t_date TEXT NOT NULL,"
                + "status TEXT NOT NULL," // PENDING or PAID
                + "FOREIGN KEY (user_id) REFERENCES tbl_user(user_id),"
                + "FOREIGN KEY (product_id) REFERENCES tbl_product(p_id)"
                + ");";

        try (Connection conn = connectDB();
             PreparedStatement pstmtUser = conn.prepareStatement(sqlUser);
             PreparedStatement pstmtProduct = conn.prepareStatement(sqlProduct);
             PreparedStatement pstmtCustomer = conn.prepareStatement(sqlCustomer);
             PreparedStatement pstmtTransaction = conn.prepareStatement(sqlTransaction)) {
            
            pstmtUser.executeUpdate();
            pstmtProduct.executeUpdate();
            pstmtCustomer.executeUpdate();
            pstmtTransaction.executeUpdate();
            
            System.out.println("Database tables initialized successfully.");
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    // --- GENERIC CRUD HELPERS (Used by Customer Management) ---
    
    /**
     * Helper method to dynamically set values in a PreparedStatement based on object type.
     */
    private void setPreparedStatementValues(PreparedStatement pstmt, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            int paramIndex = i + 1;

            if (value instanceof Integer) {
                pstmt.setInt(paramIndex, (Integer) value);
            } else if (value instanceof Double) {
                pstmt.setDouble(paramIndex, (Double) value);
            } else if (value instanceof Date) {
                 pstmt.setString(paramIndex, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value));
            } else if (value != null) {
                pstmt.setString(paramIndex, value.toString()); 
            } else {
                pstmt.setObject(paramIndex, null);
            }
        }
    }

    /** Executes a dynamic INSERT statement. */
    public void addRecord(String sql, Object... values) {
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            pstmt.executeUpdate();
            System.out.println("Record added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding record: " + e.getMessage());
        }
    }
    
    /** Executes a dynamic UPDATE statement. */
    public void updateRecord(String sql, Object... values) {
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Record updated successfully! (" + rowsAffected + " rows affected)");
            } else {
                 System.out.println("No records were updated.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating record: " + e.getMessage());
        }
    }
    
    /** Executes a dynamic DELETE statement. */
    public void deleteRecord(String sql, Object... values) {
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Record deleted successfully! (" + rowsAffected + " rows affected)");
            } else {
                 System.out.println("No records were deleted.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting record: " + e.getMessage());
        }
    }
    
    /** Dynamic view method to display records from any table. */
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
        if (columnHeaders.length != columnNames.length) {
            System.err.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

            // Print Header
            StringBuilder headerLine = new StringBuilder();
            headerLine.append("-------------------------------------------------------------------------------------------------------------------------\n| ");
            for (String header : columnHeaders) {
                headerLine.append(String.format("%-15s | ", header));
            }
            headerLine.append("\n-------------------------------------------------------------------------------------------------------------------------");
            System.out.println(headerLine.toString());

            // Print Rows
            boolean foundRecords = false;
            while (rs.next()) {
                foundRecords = true;
                StringBuilder row = new StringBuilder("| ");
                for (String colName : columnNames) {
                    String value = rs.getString(colName);
                    row.append(String.format("%-15s | ", value != null ? value : "")); 
                }
                System.out.println(row.toString());
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            if (!foundRecords) {
                 System.out.println("No records found.");
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving records: " + e.getMessage());
        }
    }
    
    /** Executes a query to retrieve a single numeric value (e.g., COUNT, SUM, AVG). */
    public double getSingleValue(String sql, Object... params) {
        double result = 0.0;
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, params);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getDouble(1);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving single value: " + e.getMessage());
        }
        return result;
    }

    // --- USER / AUTHENTICATION MANAGEMENT METHODS ---
    
    public boolean registerUser(String username, String password, String email) {
        String sql = "INSERT INTO tbl_user (username, password, email, is_admin, is_approved) VALUES (?, ?, ?, 0, 0)";
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration error (Username/Email exists): " + e.getMessage());
            return false;
        }
    }
    
    public boolean loginUser(String email, String password) {
        String sql = "SELECT is_approved FROM tbl_user WHERE email = ? AND password = ?";
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // User found, check if approved
                return rs.getInt("is_approved") == 1; 
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean isAdmin(String email) {
        String sql = "SELECT is_admin FROM tbl_user WHERE email = ?";
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("is_admin") == 1;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Admin check error: " + e.getMessage());
            return false;
        }
    }
    
    public int getUserId(String email) {
        String sql = "SELECT user_id FROM tbl_user WHERE email = ?";
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
            return -1; // -1 indicates not found
        } catch (SQLException e) {
            System.err.println("Get User ID error: " + e.getMessage());
            return -1;
        }
    }

    public void viewPendingUsers() {
        String qry = "SELECT user_id, username, email FROM tbl_user WHERE is_approved = 0";
        String[] hrds = {"ID", "Username", "Email"};
        String[] clms = {"user_id", "username", "email"};
        viewRecords(qry, hrds, clms);
    }
    
    public boolean approveUser(String username) {
        String sql = "UPDATE tbl_user SET is_approved = 1 WHERE username = ? AND is_approved = 0";
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Approval error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean makeUserAdmin(String username, String adminUsername) {
         if (username.equalsIgnoreCase(adminUsername)) {
             return true; 
         }
        String sql = "UPDATE tbl_user SET is_admin = 1 WHERE username = ? AND is_admin = 0";
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Admin promotion error: " + e.getMessage());
            return false;
        }
    }
    
    public void viewUsers() {
        String qry = "SELECT user_id, username, email, is_admin, is_approved FROM tbl_user";
        String[] hrds = {"ID", "Username", "Email", "Admin (1/0)", "Approved (1/0)"};
        String[] clms = {"user_id", "username", "email", "is_admin", "is_approved"};
        viewRecords(qry, hrds, clms);
    }

    // --- PRODUCT MANAGEMENT METHODS (Admin) ---
    
    public void addProduct(String name, double price, int stock, String adminUsername) {
        String sql = "INSERT INTO tbl_product (p_name, p_price, p_stock) VALUES (?, ?, ?)";
        addRecord(sql, name, price, stock);
    }
    
    public void updateProduct(int id, String newName, double newPrice, int newStock, String adminUsername) {
        String sql = "UPDATE tbl_product SET p_name = ?, p_price = ?, p_stock = ? WHERE p_id = ?";
        updateRecord(sql, newName, newPrice, newStock, id);
    }
    
    public void deleteProduct(int id, String adminUsername) {
        String sql = "DELETE FROM tbl_product WHERE p_id = ?";
        deleteRecord(sql, id);
    }
    
    public void viewProducts() {
        String qry = "SELECT p_id, p_name, p_price, p_stock FROM tbl_product";
        String[] hrds = {"ID", "Name", "Price", "Stock"};
        String[] clms = {"p_id", "p_name", "p_price", "p_stock"};
        viewRecords(qry, hrds, clms);
    }
    
    // --- TRANSACTION MANAGEMENT METHODS ---

    public void makeTransaction(String username, int userId, int productId, int quantity) {
        if (quantity <= 0) {
            System.out.println("Transaction failed: Quantity must be positive.");
            return;
        }
        
        // 1. Get product price and stock
        String priceSql = "SELECT p_price, p_stock FROM tbl_product WHERE p_id = ?";
        double price = 0.0;
        int currentStock = 0;
        
        try (Connection conn = connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(priceSql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("p_price");
                currentStock = rs.getInt("p_stock");
            } else {
                System.out.println("Transaction failed: Product ID not found.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error checking stock/price: " + e.getMessage());
            return;
        }
        
        // 2. Check stock
        if (currentStock < quantity) {
            System.out.println("Transaction failed: Insufficient stock. Available: " + currentStock);
            return;
        }
        
        double totalAmount = price * quantity;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        
        // 3. Insert transaction and update stock using a transaction (ensuring atomicity)
        String insertSql = "INSERT INTO tbl_transaction (user_id, product_id, quantity, total_amount, t_date, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";
        String updateStockSql = "UPDATE tbl_product SET p_stock = p_stock - ? WHERE p_id = ?";

        Connection conn = null;
        try {
            conn = connectDB();
            conn.setAutoCommit(false); // Start transaction

            // Insert transaction
            try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSql)) {
                pstmtInsert.setInt(1, userId);
                pstmtInsert.setInt(2, productId);
                pstmtInsert.setInt(3, quantity);
                pstmtInsert.setDouble(4, totalAmount);
                pstmtInsert.setString(5, date);
                pstmtInsert.executeUpdate();
            }
            
            // Update stock (decrement)
            try (PreparedStatement pstmtUpdateStock = conn.prepareStatement(updateStockSql)) {
                pstmtUpdateStock.setInt(1, quantity);
                pstmtUpdateStock.setInt(2, productId);
                pstmtUpdateStock.executeUpdate();
            }

            conn.commit(); // Commit transaction
            System.out.printf("Transaction successful! Total: %.2f (Status: PENDING PAYMENT)\n", totalAmount);
        } catch (SQLException e) {
             try { if (conn != null) conn.rollback(); } catch (SQLException ex) { System.err.println("Rollback failed: " + ex.getMessage()); }
            System.err.println("Transaction failed (DB error/rollback): " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }
    
    public void makePayment(String email, int transactionId) {
        int userId = getUserId(email);
        if (userId == -1) {
            System.out.println("Payment failed: User not found.");
            return;
        }
        
        String sql = "UPDATE tbl_transaction SET status = 'PAID' WHERE t_id = ? AND user_id = ? AND status = 'PENDING'";
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, transactionId);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Payment successful! Transaction " + transactionId + " is now PAID.");
            } else {
                System.out.println("Payment failed: Transaction ID not found, already paid, or does not belong to your account.");
            }
        } catch (SQLException e) {
            System.err.println("Payment error: " + e.getMessage());
        }
    }
    
    public void updateTransaction(int transactionId, int newQuantity, String email) {
        int userId = getUserId(email);
        if (userId == -1) {
            System.out.println("Update failed: User not found.");
            return;
        }
        if (newQuantity <= 0) {
            System.out.println("Update failed: Quantity must be positive. Use delete to remove.");
            return;
        }
        
        String selectSql = "SELECT T.quantity, T.product_id, P.p_stock, P.p_price FROM tbl_transaction T JOIN tbl_product P ON T.product_id = P.p_id WHERE T.t_id = ? AND T.user_id = ? AND T.status = 'PENDING'";
        
        Connection conn = null;
        try {
            conn = connectDB();
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
                pstmtSelect.setInt(1, transactionId);
                pstmtSelect.setInt(2, userId);
                ResultSet rs = pstmtSelect.executeQuery();

                if (rs.next()) {
                    int oldQuantity = rs.getInt("quantity");
                    int productId = rs.getInt("product_id");
                    int currentStock = rs.getInt("p_stock");
                    double price = rs.getDouble("p_price");
                    
                    int stockChange = newQuantity - oldQuantity;
                    if (currentStock - stockChange < 0) {
                        System.out.println("Update failed: Not enough stock for the new quantity. Available: " + currentStock);
                        conn.rollback();
                        return;
                    }
                    
                    double newTotal = newQuantity * price;
                    
                    // 1. Update stock in tbl_product 
                    String updateStockSql = "UPDATE tbl_product SET p_stock = p_stock - ? WHERE p_id = ?";
                    try (PreparedStatement pstmtUpdateStock = conn.prepareStatement(updateStockSql)) {
                        pstmtUpdateStock.setInt(1, stockChange);
                        pstmtUpdateStock.setInt(2, productId);
                        pstmtUpdateStock.executeUpdate();
                    }
                    
                    // 2. Update transaction quantity and total
                    String updateTxnSql = "UPDATE tbl_transaction SET quantity = ?, total_amount = ? WHERE t_id = ?";
                    try (PreparedStatement pstmtUpdateTxn = conn.prepareStatement(updateTxnSql)) {
                        pstmtUpdateTxn.setInt(1, newQuantity);
                        pstmtUpdateTxn.setDouble(2, newTotal);
                        pstmtUpdateTxn.setInt(3, transactionId);
                        pstmtUpdateTxn.executeUpdate();
                    }

                    conn.commit();
                    System.out.printf("Transaction %d updated successfully. New Quantity: %d, New Total: %.2f\n", transactionId, newQuantity, newTotal);
                } else {
                    System.out.println("Update failed: Transaction not found, not pending, or does not belong to your account.");
                }
            }
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignored */ }
            System.err.println("Transaction update failed (DB error/rollback): " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }
    
    public void deleteTransaction(int transactionId, String email) {
        int userId = getUserId(email);
        if (userId == -1) {
            System.out.println("Deletion failed: User not found.");
            return;
        }

        String selectSql = "SELECT quantity, product_id FROM tbl_transaction WHERE t_id = ? AND user_id = ? AND status = 'PENDING'";
        
        Connection conn = null;
        try {
            conn = connectDB();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
                pstmtSelect.setInt(1, transactionId);
                pstmtSelect.setInt(2, userId);
                ResultSet rs = pstmtSelect.executeQuery();

                if (rs.next()) {
                    int quantityToRestore = rs.getInt("quantity");
                    int productId = rs.getInt("product_id");
                    
                    // 1. Restore stock in tbl_product
                    String updateStockSql = "UPDATE tbl_product SET p_stock = p_stock + ? WHERE p_id = ?";
                    try (PreparedStatement pstmtUpdateStock = conn.prepareStatement(updateStockSql)) {
                        pstmtUpdateStock.setInt(1, quantityToRestore);
                        pstmtUpdateStock.setInt(2, productId);
                        pstmtUpdateStock.executeUpdate();
                    }
                    
                    // 2. Delete the transaction
                    String deleteTxnSql = "DELETE FROM tbl_transaction WHERE t_id = ?";
                    try (PreparedStatement pstmtDeleteTxn = conn.prepareStatement(deleteTxnSql)) {
                        pstmtDeleteTxn.setInt(1, transactionId);
                        pstmtDeleteTxn.executeUpdate();
                    }

                    conn.commit(); 
                    System.out.println("Transaction " + transactionId + " deleted and product stock restored.");
                } else {
                    System.out.println("Deletion failed: Transaction not found, not pending, or does not belong to your account.");
                }
            }
        } catch (SQLException e) {
             try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignored */ }
            System.err.println("Transaction deletion failed (DB error/rollback): " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }
    
    public void adminDeleteTransaction(int transactionId, String adminUsername) {
        // NOTE: Admin force delete does NOT restore stock for simplicity of this console demo.
        System.out.println("WARNING: Admin '" + adminUsername + "' is force deleting transaction " + transactionId + " without stock restoration.");
        String sql = "DELETE FROM tbl_transaction WHERE t_id = ?";
        deleteRecord(sql, transactionId);
    }
    
    public void viewTransactions(String email) {
        int userId = getUserId(email);
        if (userId == -1) {
            System.out.println("Error: User ID not found.");
            return;
        }
        
        String qry = "SELECT T.t_id, P.p_name, T.quantity, T.total_amount, T.status, T.t_date FROM tbl_transaction T JOIN tbl_product P ON T.product_id = P.p_id WHERE T.user_id = ?";
        String[] hrds = {"Txn ID", "Product", "Qty", "Total", "Status", "Date"};
        String[] clms = {"t_id", "p_name", "quantity", "total_amount", "status", "t_date"};
        
        try (Connection conn = connectDB();
             PreparedStatement pstmt = conn.prepareStatement(qry)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            StringBuilder headerLine = new StringBuilder();
            headerLine.append("-------------------------------------------------------------------------------------------------------------------------\n| ");
            for (String header : hrds) {
                headerLine.append(String.format("%-15s | ", header));
            }
            headerLine.append("\n-------------------------------------------------------------------------------------------------------------------------");
            System.out.println("\n--- My Transactions ---");
            System.out.println(headerLine.toString());
            
            boolean foundRecords = false;
            while (rs.next()) {
                foundRecords = true;
                StringBuilder row = new StringBuilder("| ");
                for (String colName : clms) {
                    String value = rs.getString(colName);
                    row.append(String.format("%-15s | ", value != null ? value : "")); 
                }
                System.out.println(row.toString());
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            if (!foundRecords) {
                 System.out.println("No transactions found.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error viewing transactions: " + e.getMessage());
        }
    }
}
