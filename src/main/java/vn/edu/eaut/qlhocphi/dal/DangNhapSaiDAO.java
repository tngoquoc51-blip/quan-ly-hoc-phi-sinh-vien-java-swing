package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;

/** Bảng dangnhap_sai: đếm lần sai + thời điểm khóa tạm. */
public class DangNhapSaiDAO {

    public static class BanGhi {
        public String tenDangNhap;
        public int soLanSai;
        public LocalDateTime khoaDen;
    }

    public BanGhi lay(String tenDangNhap) throws SQLException {
        String sql = "SELECT TenDangNhap, SoLanSai, KhoaDen FROM dangnhap_sai WHERE TenDangNhap=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                BanGhi b = new BanGhi();
                b.tenDangNhap = rs.getString("TenDangNhap");
                b.soLanSai = rs.getInt("SoLanSai");
                Timestamp ts = rs.getTimestamp("KhoaDen");
                b.khoaDen = ts != null ? ts.toLocalDateTime() : null;
                return b;
            }
        }
    }

    public void ghiNhanSai(String tenDangNhap, int soLanSaiMoi, LocalDateTime khoaDen)
            throws SQLException {
        String sql = "INSERT INTO dangnhap_sai (TenDangNhap, SoLanSai, KhoaDen, CapNhatLuc) "
                + "VALUES (?,?,?,NOW()) "
                + "ON DUPLICATE KEY UPDATE SoLanSai=VALUES(SoLanSai), "
                + "KhoaDen=VALUES(KhoaDen), CapNhatLuc=NOW()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setInt(2, soLanSaiMoi);
            if (khoaDen != null) ps.setTimestamp(3, Timestamp.valueOf(khoaDen));
            else ps.setNull(3, Types.TIMESTAMP);
            ps.executeUpdate();
        }
    }

    public void xoa(String tenDangNhap) throws SQLException {
        String sql = "DELETE FROM dangnhap_sai WHERE TenDangNhap=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.executeUpdate();
        }
    }
}