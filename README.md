# Studi Kasus Java Thread & Database — Sistem Perpustakaan (JDBC + Swing)

Semua file kode berada langsung di dalam folder `src/` tanpa subfolder package:
- `src/DatabaseConnection.java` — koneksi MySQL
- `src/Book.java` — model data buku
- `src/BookDAO.java` — CRUD lengkap (`addBook`, `findAll` + pencarian, `updateBook`, `deleteBook`) dan transaksi `borrowBook` (`setAutoCommit(false)`, `commit`, `rollback`)
- `src/LibraryApp.java` — GUI Swing satu jendela dengan pola `Thread` -> `SwingUtilities.invokeLater` -> UI serta penguncian tombol dan status `"Saving..."`

## Cara Menjalankan

1. Unduh [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (pilih *Platform Independent*), ekstrak `mysql-connector-j-x.x.x.jar`, lalu salin ke folder `lib/`.
2. Buka proyek di VS Code dan pastikan JAR terdaftar di **JAVA PROJECTS -> Referenced Libraries**.
3. Jalankan `schema.sql` di MySQL untuk membuat database `library_db` dan 3 buku awal.
4. Sesuaikan `USER` dan `PASSWORD` di `src/DatabaseConnection.java`, lalu jalankan `src/LibraryApp.java`.
