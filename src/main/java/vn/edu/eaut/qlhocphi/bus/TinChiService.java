package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.TinChiDAO;
import vn.edu.eaut.qlhocphi.model.TinChiSinhVien;
import vn.edu.eaut.qlhocphi.model.TinChiTheoNam;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Tín chỉ SV – tính năm học + đồng bộ TC/ĐTB từ lịch sử theo năm.
 */
public class TinChiService {
    private final TinChiDAO dao = new TinChiDAO();

    public static final int TIN_CHI_MOI_NAM = 30;
    public static final int TONG_TC_CHUONG_TRINH = 183;

    public TinChiSinhVien layTienDo(String maSV) throws SQLException {
        if (maSV == null || maSV.isBlank()) return null;
        TinChiSinhVien tc = dao.layTheoMaSV(maSV.trim());
        if (tc == null) return null;
        List<TinChiTheoNam> ls = dao.layLichSuTheoNam(maSV.trim());
        dongBoVaTinhNam(tc, ls);
        return tc;
    }

    public List<TinChiTheoNam> layLichSuNam(String maSV) throws SQLException {
        if (maSV == null || maSV.isBlank()) return List.of();
        return dao.layLichSuTheoNam(maSV.trim());
    }

    private void dongBoVaTinhNam(TinChiSinhVien tc, List<TinChiTheoNam> ls) {
        if (ls != null && !ls.isEmpty()) {
            int tongDat = ls.stream().mapToInt(TinChiTheoNam::getTinChiDat).sum();
            int tongRut = ls.stream().mapToInt(TinChiTheoNam::getTinChiRut).sum();
            if (tongDat > 0) {
                tc.setTinChiTichLuy(tongDat);
                tc.setTinChiDaHoc(tongDat);
                tc.setTinChiBiRut(tongRut);
            }
            // ĐTB tích lũy có trọng số TC
            double sum10 = 0, sum4 = 0, w = 0;
            for (TinChiTheoNam n : ls) {
                if (n.getDiemTB10() <= 0) continue;
                double wi = Math.max(n.getTinChiDat(), 1);
                sum10 += n.getDiemTB10() * wi;
                sum4 += n.getDiemTB4() * wi;
                w += wi;
            }
            if (w > 0) {
                double a10 = Math.round(sum10 / w * 100.0) / 100.0;
                double a4 = Math.round(sum4 / w * 100.0) / 100.0;
                if (tc.getDiemTB10() <= 0) tc.setDiemTB10(a10);
                if (tc.getDiemTB4() <= 0) tc.setDiemTB4(a4);
                if (tc.getDiemTBTichLuy10() <= 0) tc.setDiemTBTichLuy10(a10);
                if (tc.getDiemTBTichLuy4() <= 0) tc.setDiemTBTichLuy4(a4);
            }
            // Điểm năm gần nhất làm ĐTB "học kỳ/năm gần nhất"
            TinChiTheoNam last = ls.get(ls.size() - 1);
            if (last.getDiemTB10() > 0 && tc.getDiemTB10() <= 0) {
                tc.setDiemTB10(last.getDiemTB10());
                tc.setDiemTB4(last.getDiemTB4());
            }

            int namMin = ls.stream().mapToInt(TinChiTheoNam::getNamHoc).min().orElse(0);
            if (namMin > 0 && tc.getNamNhapHoc() == null) {
                tc.setNamNhapHoc(namMin);
            }
        }

        if (tc.getTongTcChuongTrinh() <= 0) {
            tc.setTongTcChuongTrinh(TONG_TC_CHUONG_TRINH);
        }

        if (tc.getNamNhapHoc() != null && tc.getNamNhapHoc() > 0) {
            int namHt = LocalDate.now().getYear();
            tc.setNamThu(clampNamThu(namHt - tc.getNamNhapHoc() + 1));
            return;
        }
        if (ls != null && !ls.isEmpty()) {
            int namMin = ls.stream().mapToInt(TinChiTheoNam::getNamHoc).min().orElse(0);
            int namMax = ls.stream().mapToInt(TinChiTheoNam::getNamHoc).max().orElse(0);
            if (namMin > 0) {
                tc.setNamNhapHoc(namMin);
                tc.setNamThu(clampNamThu(namMax - namMin + 1));
                return;
            }
        }
        if (tc.getTinChiTichLuy() > 0) {
            int namThu = (int) Math.ceil(tc.getTinChiTichLuy() / (double) TIN_CHI_MOI_NAM);
            tc.setNamThu(clampNamThu(namThu));
            int namHt = LocalDate.now().getYear();
            tc.setNamNhapHoc(namHt - tc.getNamThu() + 1);
            return;
        }
        if (tc.getNamThu() == null || tc.getNamThu() < 1) {
            tc.setNamThu(1);
        }
    }

    private static int clampNamThu(int n) {
        if (n < 1) return 1;
        if (n > 5) return 5;
        return n;
    }
}