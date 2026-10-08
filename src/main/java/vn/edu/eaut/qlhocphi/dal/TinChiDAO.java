package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.TinChiSinhVien;
import vn.edu.eaut.qlhocphi.model.TinChiTheoNam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TinChiDAO {

    public TinChiSinhVien layTheoMaSV(String maSV) throws SQLException {
        String sql = "SELECT t.*, sv.HoTen, sv.NamNhapHoc, sv.NamThu, sv.TrangThaiHoc "
                + "FROM tinchi_sinhvien t "
                + "JOIN SinhVien sv ON sv.MaSV = t.MaSV WHERE t.MaSV = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        String sql2 = "SELECT MaSV, HoTen, NamNhapHoc, NamThu, TrangThaiHoc FROM SinhVien WHERE MaSV = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql2)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TinChiSinhVien t = new TinChiSinhVien();
                    t.setMaSV(rs.getString("MaSV"));
                    t.setHoTen(rs.getString("HoTen"));
                    t.setNamNhapHoc((Integer) rs.getObject("NamNhapHoc"));
                    t.setNamThu((Integer) rs.getObject("NamThu"));
                    t.setTrangThaiHoc(rs.getString("TrangThaiHoc"));
                    return t;
                }
            }
        }
        return null;
    }

    public List<TinChiTheoNam> layLichSuTheoNam(String maSV) throws SQLException {
        String sql = "SELECT * FROM tinchi_theonam WHERE MaSV = ? ORDER BY NamHoc";
        List<TinChiTheoNam> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TinChiTheoNam n = new TinChiTheoNam();
                    n.setId(rs.getInt("Id"));
                    n.setMaSV(rs.getString("MaSV"));
                    n.setNamHoc(rs.getInt("NamHoc"));
                    n.setTinChiDat(rs.getInt("TinChiDat"));
                    n.setTinChiRut(rs.getInt("TinChiRut"));
                    n.setGhiChu(rs.getString("GhiChu"));
                    n.setDiemTB10(getDoubleSafe(rs, "DtbHe10"));
                    n.setDiemTB4(getDoubleSafe(rs, "DtbHe4"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    private TinChiSinhVien map(ResultSet rs) throws SQLException {
        TinChiSinhVien t = new TinChiSinhVien();
        t.setMaSV(rs.getString("MaSV"));
        t.setTinChiTichLuy(rs.getInt("TinChiTichLuy"));
        t.setTinChiBiRut(rs.getInt("TinChiBiRut"));
        t.setTinChiDangKy(rs.getInt("TinChiDangKy"));
        t.setTinChiDaHoc(rs.getInt("TinChiTichLuy"));
        Timestamp ts = rs.getTimestamp("CapNhatLuc");
        if (ts != null) t.setCapNhatLuc(ts.toLocalDateTime());
        try {
            t.setHoTen(rs.getString("HoTen"));
            t.setNamNhapHoc((Integer) rs.getObject("NamNhapHoc"));
            t.setNamThu((Integer) rs.getObject("NamThu"));
            t.setTrangThaiHoc(rs.getString("TrangThaiHoc"));
        } catch (SQLException ignored) {}
        // Cột điểm từ migration (DtbHe10 / DtbHe4 / ...)
        double d10 = getDoubleSafe(rs, "DtbHe10");
        double d4 = getDoubleSafe(rs, "DtbHe4");
        double tl10 = getDoubleSafe(rs, "DtbTichLuyHe10");
        double tl4 = getDoubleSafe(rs, "DtbTichLuyHe4");
        t.setDiemTB10(d10);
        t.setDiemTB4(d4);
        t.setDiemTBTichLuy10(tl10 > 0 ? tl10 : d10);
        t.setDiemTBTichLuy4(tl4 > 0 ? tl4 : d4);
        return t;
    }

    private static double getDoubleSafe(ResultSet rs, String col) {
        try {
            Object o = rs.getObject(col);
            if (o == null) return 0;
            return rs.getDouble(col);
        } catch (SQLException e) {
            return 0;
        }
    }
}