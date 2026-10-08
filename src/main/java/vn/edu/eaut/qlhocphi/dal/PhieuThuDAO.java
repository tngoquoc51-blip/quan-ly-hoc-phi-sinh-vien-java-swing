package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.PhieuThu;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PhieuThuDAO {

    public List<PhieuThu> layTheoHoaDon(int maHoaDon) throws SQLException {
        String sql = "SELECT * FROM PhieuThu WHERE MaHoaDon = ? ORDER BY NgayNop DESC";
        List<PhieuThu> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Cac cau JOIN dung chung cho cac truy van "giao dich gan day" ben duoi - kem ten
     *  Sinh vien + ten Hoc ky de hien thi truc tiep, khong can truy van rieng tung hoa don. */
    private static final String SELECT_JOIN =
            "SELECT pt.*, sv.HoTen AS TenSV, hk.TenHocKy AS TenHocKy " +
                    "FROM PhieuThu pt " +
                    "JOIN HoaDonHocPhi hd ON hd.MaHoaDon = pt.MaHoaDon " +
                    "JOIN SinhVien sv ON sv.MaSV = hd.MaSV " +
                    "JOIN HocKy hk ON hk.MaHocKy = hd.MaHocKy ";

    /** N giao dich gan day nhat toan truong (moi hoa don), dung cho Bang dieu khien Ke toan. */    public List<PhieuThu> layGanDayNhat(int gioiHan) throws SQLException {
        // MaPhieuThu tăng khi INSERT → phiếu vừa thu luôn lên đầu
        // (tránh data mẫu có NgayNop tương lai che mất giao dịch thật)
        String sql = SELECT_JOIN + "ORDER BY pt.MaPhieuThu DESC LIMIT ?";
        List<PhieuThu> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gioiHan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCoJoin(rs));
            }
        }
        return list;
    }

    /** Tong tien da thu trong 1 ngay cu the (thuong dung voi LocalDate.now() = "hom nay"). */
    public BigDecimal tongThuTheoNgay(LocalDate ngay) throws SQLException {
        String sql = "SELECT COALESCE(SUM(SoTienNop),0) AS Tong FROM PhieuThu WHERE DATE(NgayNop) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("Tong");
            }
        }
        return BigDecimal.ZERO;
    }

    /** So luong giao dich trong 1 ngay cu the. */
    public int demGiaoDichTheoNgay(LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) AS SoLuong FROM PhieuThu WHERE DATE(NgayNop) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ngay));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("SoLuong");
            }
        }
        return 0;
    }

    public int them(PhieuThu pt) throws SQLException {
        // Ghi chuỗi giờ máy — không qua Timestamp (tránh lệch UTC)
        String sql = "INSERT INTO PhieuThu (MaHoaDon,SoTienNop,NgayNop,HinhThuc,MaGiaoDich,NguoiThu) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            java.time.LocalDateTime lucNop = pt.getNgayNop() != null
                    ? pt.getNgayNop()
                    : java.time.LocalDateTime.now();
            String ngayStr = lucNop.format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ps.setInt(1, pt.getMaHoaDon());
            ps.setBigDecimal(2, pt.getSoTienNop());
            ps.setString(3, ngayStr);   // ← quan trọng: setString, không setTimestamp
            ps.setString(4, pt.getHinhThuc());
            ps.setString(5, pt.getMaGiaoDich());
            ps.setString(6, pt.getNguoiThu());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    private PhieuThu map(ResultSet rs) throws SQLException {
        PhieuThu pt = new PhieuThu();
        pt.setMaPhieuThu(rs.getInt("MaPhieuThu"));
        pt.setMaHoaDon(rs.getInt("MaHoaDon"));
        pt.setSoTienNop(rs.getBigDecimal("SoTienNop"));
        // Đọc LocalDateTime trực tiếp — không dùng Timestamp + toLocalDateTime()
        java.time.LocalDateTime ngay = rs.getObject("NgayNop", java.time.LocalDateTime.class);
        pt.setNgayNop(ngay);
        pt.setHinhThuc(rs.getString("HinhThuc"));
        pt.setMaGiaoDich(rs.getString("MaGiaoDich"));
        pt.setNguoiThu(rs.getString("NguoiThu"));
        return pt;
    }

    private PhieuThu mapCoJoin(ResultSet rs) throws SQLException {
        PhieuThu pt = map(rs);
        pt.setTenSV(rs.getString("TenSV"));
        pt.setTenHocKy(rs.getString("TenHocKy"));
        return pt;
    }
}