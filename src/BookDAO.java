import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    // CREATE
    public void addBook(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, available_copies) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getAvailableCopies());
            ps.executeUpdate();
        }
    }

    // READ + SEARCH 
    public List<Book> findAll(String keyword) throws SQLException {
        boolean hasFilter = keyword != null && !keyword.trim().isEmpty();
        String sql = hasFilter
            ? "SELECT id, title, author, available_copies FROM books "
              + "WHERE title LIKE ? OR author LIKE ? ORDER BY id DESC"
            : "SELECT id, title, author, available_copies FROM books ORDER BY id DESC";

        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (hasFilter) {
                String pattern = "%" + keyword.trim() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("available_copies")
                    ));
                }
            }
        }
        return books;
    }

    // UPDATE
    public void updateBook(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, available_copies = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setInt(3, book.getAvailableCopies());
            ps.setInt(4, book.getId());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteBook(int id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // TRANSACTION
    public void borrowBook(int bookId, String borrowerName) throws SQLException {
        String checkSql = "SELECT available_copies FROM books WHERE id = ?";
        String updateSql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ?";
        String logSql = "INSERT INTO borrow_logs (book_id, borrower_name) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Mulai transaksi

            int copies;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, bookId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Buku tidak ditemukan.");
                    }
                    copies = rs.getInt("available_copies");
                }
            }

            if (copies <= 0) {
                throw new SQLException("Stok buku habis, tidak bisa dipinjam.");
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, bookId);
                psUpdate.executeUpdate();
            }

            try (PreparedStatement psLog = conn.prepareStatement(logSql)) {
                psLog.setInt(1, bookId);
                psLog.setString(2, borrowerName);
                psLog.executeUpdate();
            }

            conn.commit(); // Commit jika kedua operasi sukses

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback(); // Batalkan semua perubahan jika gagal
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
