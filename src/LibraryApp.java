import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LibraryApp extends JFrame {
    private final BookDAO dao = new BookDAO();

    private final JTextField txtTitle = new JTextField();
    private final JTextField txtAuthor = new JTextField();
    private final JTextField txtCopies = new JTextField();
    private final JTextField txtSearch = new JTextField(18);

    private final JButton btnAdd = new JButton("Tambah");
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDelete = new JButton("Hapus");
    private final JButton btnClear = new JButton("Reset");
    private final JButton btnBorrow = new JButton("Pinjam Buku Terpilih");
    private final JButton btnSearch = new JButton("Cari");
    private final JButton btnResetSearch = new JButton("Semua");

    private final JLabel lblStatus = new JLabel("Siap");
    private final JLabel lblCount = new JLabel("0 buku");

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[]{"ID", "Judul Buku", "Penulis", "Tersedia"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    public LibraryApp() {
        super("Sistem Perpustakaan");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 540);
        setMinimumSize(new Dimension(780, 460));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeaderBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        configureTable();
        wireEvents();
        loadBooksAsync("");
    }

    // Header atas
    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(215, 220, 226)),
            new EmptyBorder(12, 16, 12, 16)
        ));
        header.setBackground(new Color(248, 250, 252));

        JLabel title = new JLabel("Sistem Peminjaman Perpustakaan");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Cari:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnResetSearch);

        header.add(title, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);
        return header;
    }

    // Area utama
    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(14, 0));
        content.setBorder(new EmptyBorder(14, 16, 14, 16));

        content.add(buildSidebarForm(), BorderLayout.WEST);
        content.add(buildTablePanel(), BorderLayout.CENTER);
        return content;
    }

    private JPanel buildSidebarForm() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 14));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Detail Buku"),
            new EmptyBorder(10, 12, 12, 12)
        ));

        JPanel fields = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        gc.gridy = 0; gc.insets = new Insets(0, 0, 4, 0);
        fields.add(new JLabel("Judul Buku"), gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 12, 0);
        txtTitle.setPreferredSize(new Dimension(0, 28));
        fields.add(txtTitle, gc);

        gc.gridy = 2; gc.insets = new Insets(0, 0, 4, 0);
        fields.add(new JLabel("Penulis"), gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 12, 0);
        txtAuthor.setPreferredSize(new Dimension(0, 28));
        fields.add(txtAuthor, gc);

        gc.gridy = 4; gc.insets = new Insets(0, 0, 4, 0);
        fields.add(new JLabel("Stok Tersedia"), gc);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 12, 0);
        txtCopies.setPreferredSize(new Dimension(0, 28));
        fields.add(txtCopies, gc);

        // Tombol CRUD 
        JPanel crudGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        crudGrid.add(btnAdd);
        crudGrid.add(btnUpdate);
        crudGrid.add(btnDelete);
        crudGrid.add(btnClear);

        // Tombol aksi transaksi 
        JPanel bottomActions = new JPanel(new BorderLayout(0, 10));
        bottomActions.add(crudGrid, BorderLayout.NORTH);
        bottomActions.add(new JSeparator(), BorderLayout.CENTER);
        btnBorrow.setPreferredSize(new Dimension(0, 34));
        btnBorrow.setFont(btnBorrow.getFont().deriveFont(Font.BOLD));
        bottomActions.add(btnBorrow, BorderLayout.SOUTH);

        sidebar.add(fields, BorderLayout.NORTH);
        sidebar.add(bottomActions, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Daftar Buku"),
            new EmptyBorder(6, 8, 8, 8)
        ));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        return tablePanel;
    }

    // Status bar bawh
    private JPanel buildStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(215, 220, 226)),
            new EmptyBorder(8, 16, 8, 16)
        ));
        statusBar.setBackground(new Color(248, 250, 252));
        statusBar.add(lblStatus, BorderLayout.WEST);
        statusBar.add(lblCount, BorderLayout.EAST);
        return statusBar;
    }

    private void configureTable() {
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
    }

    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtTitle.setText(String.valueOf(tableModel.getValueAt(row, 1)));
                    txtAuthor.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                    txtCopies.setText(String.valueOf(tableModel.getValueAt(row, 3)));
                }
            }
        });

        btnAdd.addActionListener(e -> handleAdd());
        btnUpdate.addActionListener(e -> handleUpdate());
        btnDelete.addActionListener(e -> handleDelete());
        btnClear.addActionListener(e -> {
            table.clearSelection();
            clearForm();
            lblStatus.setText("Form dibersihkan.");
        });
        btnBorrow.addActionListener(e -> handleBorrow());
        btnSearch.addActionListener(e -> loadBooksAsync(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadBooksAsync(txtSearch.getText()));
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadBooksAsync("");
        });
    }

    private void setBusy(boolean busy, String message) {
        btnAdd.setEnabled(!busy);
        btnUpdate.setEnabled(!busy);
        btnDelete.setEnabled(!busy);
        btnClear.setEnabled(!busy);
        btnBorrow.setEnabled(!busy);
        btnSearch.setEnabled(!busy);
        btnResetSearch.setEnabled(!busy);
        lblStatus.setText(message);
    }

    
    private void handleAdd() {
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        int copies;
        try {
            if (title.isEmpty() || author.isEmpty()) {
                throw new IllegalArgumentException("Judul dan Penulis wajib diisi.");
            }
            copies = Integer.parseInt(txtCopies.getText().trim());
            if (copies < 0) throw new IllegalArgumentException("Stok tidak boleh negatif.");
        } catch (IllegalArgumentException ex) {
            lblStatus.setText("Input tidak valid: " + ex.getMessage());
            return;
        }

        setBusy(true, "Saving...");
        Book newBook = new Book(title, author, copies);

        new Thread(() -> {
            try {
                dao.addBook(newBook);
                List<Book> refreshed = dao.findAll(txtSearch.getText());
                SwingUtilities.invokeLater(() -> {
                    populateTable(refreshed);
                    clearForm();
                    setBusy(false, "Buku berhasil ditambahkan.");
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() ->
                    setBusy(false, "Gagal menambah buku: " + ex.getMessage())
                );
            }
        }, "db-add-thread").start();
    }

    private void handleUpdate() {
        int row = table.getSelectedRow();
        if (row < 0) {
            lblStatus.setText("Pilih buku di tabel untuk diperbarui.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String title = txtTitle.getText().trim();
        String author = txtAuthor.getText().trim();
        int copies;
        try {
            if (title.isEmpty() || author.isEmpty()) {
                throw new IllegalArgumentException("Judul dan Penulis wajib diisi.");
            }
            copies = Integer.parseInt(txtCopies.getText().trim());
            if (copies < 0) throw new IllegalArgumentException("Stok tidak boleh negatif.");
        } catch (IllegalArgumentException ex) {
            lblStatus.setText("Input tidak valid: " + ex.getMessage());
            return;
        }

        setBusy(true, "Saving...");
        Book updated = new Book(id, title, author, copies);

        new Thread(() -> {
            try {
                dao.updateBook(updated);
                List<Book> refreshed = dao.findAll(txtSearch.getText());
                SwingUtilities.invokeLater(() -> {
                    populateTable(refreshed);
                    setBusy(false, "Buku #" + id + " berhasil diperbarui.");
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() ->
                    setBusy(false, "Gagal memperbarui: " + ex.getMessage())
                );
            }
        }, "db-update-thread").start();
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            lblStatus.setText("Pilih buku di tabel untuk dihapus.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        setBusy(true, "Menghapus...");

        new Thread(() -> {
            try {
                dao.deleteBook(id);
                List<Book> refreshed = dao.findAll(txtSearch.getText());
                SwingUtilities.invokeLater(() -> {
                    populateTable(refreshed);
                    clearForm();
                    setBusy(false, "Buku #" + id + " berhasil dihapus.");
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() ->
                    setBusy(false, "Gagal menghapus: " + ex.getMessage())
                );
            }
        }, "db-delete-thread").start();
    }

    private void handleBorrow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            lblStatus.setText("Pilih buku yang ingin dipinjam.");
            return;
        }

        int bookId = (int) tableModel.getValueAt(row, 0);
        String title = String.valueOf(tableModel.getValueAt(row, 1));
        String borrower = JOptionPane.showInputDialog(this, "Nama peminjam untuk \"" + title + "\":");
        if (borrower == null || borrower.trim().isEmpty()) {
            return;
        }

        setBusy(true, "Saving... memproses peminjaman");

        new Thread(() -> {
            try {
                dao.borrowBook(bookId, borrower.trim());
                List<Book> refreshed = dao.findAll(txtSearch.getText());
                SwingUtilities.invokeLater(() -> {
                    populateTable(refreshed);
                    setBusy(false, "Peminjaman berhasil dicatat untuk " + borrower.trim() + ".");
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() ->
                    setBusy(false, "Peminjaman gagal (rollback): " + ex.getMessage())
                );
            }
        }, "db-borrow-thread").start();
    }

    private void loadBooksAsync(String keyword) {
        setBusy(true, "Memuat data...");
        new Thread(() -> {
            try {
                List<Book> books = dao.findAll(keyword);
                SwingUtilities.invokeLater(() -> {
                    populateTable(books);
                    setBusy(false, "Data berhasil dimuat.");
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() ->
                    setBusy(false, "Gagal memuat data: " + ex.getMessage())
                );
            }
        }, "db-load-thread").start();
    }

    private void populateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book b : books) {
            tableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getAvailableCopies()});
        }
        lblCount.setText(books.size() + " buku");
    }

    private void clearForm() {
        txtTitle.setText("");
        txtAuthor.setText("");
        txtCopies.setText("");
    }

}
