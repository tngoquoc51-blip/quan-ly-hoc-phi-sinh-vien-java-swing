package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SchedulerTrangThaiService {

    public void capNhat(String maScheduler, String trangThai, String ketQua) {
        String sql =
                "UPDATE scheduler_trangthai " +
                        "SET TrangThai = ?, LanChayGanNhat = NOW(), " +
                        "    KetQuaGanNhat = ?, CapNhatLuc = NOW() " +
                        "WHERE MaScheduler = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, ketQua == null ? "" : ketQua);
            ps.setString(3, maScheduler);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Loi cap nhat scheduler_trangthai: " + e.getMessage());
        }
    }

    public void dangChay(String ma, String ketQua) {
        capNhat(ma, "DANG_CHAY", ketQua);
    }

    public void dung(String ma, String ketQua) {
        capNhat(ma, "DUNG", ketQua);
    }
}