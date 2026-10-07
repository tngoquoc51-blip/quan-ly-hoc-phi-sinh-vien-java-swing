package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Truy cap du lieu bang TaiKhoan. Luon dung PreparedStatement de chong SQL Injection. */
public class TaiKhoanDAO {

    public List<TaiKhoan> layTatCa() throws SQLException {
        String sql = "SELECT * FROM TaiKhoan ORDER BY MaTK";
        List<TaiKhoan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public TaiKhoan timTheoMa(int maTK) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE MaTK = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** Cap nhat ho ten, vai tro, ma SV lien ket, email Google, trang thai (khong doi mat khau o day). */
    public boolean capNhat(TaiKhoan tk) throws SQLException {
        String sql = "UPDATE TaiKhoan SET HoTen=?, VaiTro=?, MaSV=?, GoogleEmail=?, TrangThai=? WHERE MaTK=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tk.getHoTen());
            ps.setString(2, tk.getVaiTro().name());
            ps.setString(3, tk.getMaSV());
            ps.setString(4, tk.getGoogleEmail());
            ps.setBoolean(5, tk.isTrangThai());
            ps.setInt(6, tk.getMaTK());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean doiMatKhau(int maTK, String matKhauHashMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET MatKhauHash=? WHERE MaTK=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauHashMoi);
            ps.setInt(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /** Doi mat khau VA dat/bo co "bat buoc doi mat khau" trong cung 1 lan cap nhat. */
    public boolean doiMatKhauVaCoBatBuoc(int maTK, String matKhauHashMoi, boolean batBuocDoiMatKhau) throws SQLException {
        String sql = "UPDATE TaiKhoan SET MatKhauHash=?, BatBuocDoiMatKhau=? WHERE MaTK=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauHashMoi);
            ps.setBoolean(2, batBuocDoiMatKhau);
            ps.setInt(3, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /** Chi doi co "bat buoc doi mat khau" (dung sau khi nguoi dung tu doi mat khau xong -> bo co). */
    public boolean datCoBatBuocDoiMatKhau(int maTK, boolean batBuoc) throws SQLException {
        String sql = "UPDATE TaiKhoan SET BatBuocDoiMatKhau=? WHERE MaTK=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, batBuoc);
            ps.setInt(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /** Gan/go Gmail dang nhap-khoi phuc cho 1 tai khoan (Admin thao tac). */
    public boolean ganGoogleEmail(int maTK, String googleEmail) throws SQLException {
        String sql = "UPDATE TaiKhoan SET GoogleEmail=? WHERE MaTK=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleEmail == null || googleEmail.isBlank() ? null : googleEmail.trim());
            ps.setInt(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    /** Tim tai khoan theo Gmail da duoc Admin gan - dung khi dang nhap/khoi phuc mat khau bang Google. */
    public TaiKhoan timTheoGoogleEmail(String googleEmail) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE GoogleEmail = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleEmail);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /**
     * Lay TOAN BO tai khoan dang gan chung 1 Gmail (Gmail khong con la UNIQUE - 1
     * Gmail co the dung chung cho nhieu tai khoan, vi du 1 tai khoan Sinh vien va 1
     * tai khoan Ke toan cung Gmail cua 1 nguoi). Dung cho man hinh "chon tai khoan"
     * khi dang nhap/khoi phuc mat khau bang Google ma Gmail do khop nhieu tai khoan.
     */
    public List<TaiKhoan> layDanhSachTheoGoogleEmail(String googleEmail) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE GoogleEmail = ? ORDER BY VaiTro, HoTen";
        List<TaiKhoan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, googleEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean xoa(int maTK) throws SQLException {
        String sql = "DELETE FROM TaiKhoan WHERE MaTK = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            return ps.executeUpdate() > 0;
        }
    }

    public TaiKhoan timTheoTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE TenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean themTaiKhoan(TaiKhoan tk) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (TenDangNhap, MatKhauHash, HoTen, VaiTro, MaSV, GoogleEmail, TrangThai) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tk.getTenDangNhap());
            ps.setString(2, tk.getMatKhauHash());
            ps.setString(3, tk.getHoTen());
            ps.setString(4, tk.getVaiTro().name());
            ps.setString(5, tk.getMaSV());
            ps.setString(6, tk.getGoogleEmail());
            ps.setBoolean(7, tk.isTrangThai());
            return ps.executeUpdate() > 0;
        }
    }

    private TaiKhoan map(ResultSet rs) throws SQLException {
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK(rs.getInt("MaTK"));
        tk.setTenDangNhap(rs.getString("TenDangNhap"));
        tk.setMatKhauHash(rs.getString("MatKhauHash"));
        tk.setHoTen(rs.getString("HoTen"));
        tk.setVaiTro(VaiTro.valueOf(rs.getString("VaiTro")));
        tk.setMaSV(rs.getString("MaSV"));
        tk.setGoogleEmail(rs.getString("GoogleEmail"));
        tk.setTrangThai(rs.getBoolean("TrangThai"));
        tk.setBatBuocDoiMatKhau(rs.getBoolean("BatBuocDoiMatKhau"));
        return tk;
    }

    /** Bật/tắt tài khoản (vô hiệu hóa khi đăng nhập sai quá nhiều hoặc Admin khóa). */
    public boolean datTrangThai(int maTK, boolean trangThai) throws SQLException {
        String sql = "UPDATE TaiKhoan SET TrangThai=? WHERE MaTK=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThai);
            ps.setInt(2, maTK);
            return ps.executeUpdate() > 0;
        }
    }
}