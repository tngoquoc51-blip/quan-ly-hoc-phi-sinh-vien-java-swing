package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.AdminConfigDAO;
import vn.edu.eaut.qlhocphi.dal.DangNhapSaiDAO;
import vn.edu.eaut.qlhocphi.dal.TaiKhoanDAO;
import vn.edu.eaut.qlhocphi.model.CauHinhBaoMat;
import vn.edu.eaut.qlhocphi.model.KetQuaDangNhap;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;
import vn.edu.eaut.qlhocphi.util.PasswordUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Xác thực + chống brute-force:
 * - Sai MK: tăng bộ đếm dangnhap_sai
 * - Đạt ngưỡng (mặc định 5): khóa tạm ThoiGianKhoaPhut phút
 * - Sai đến 2× ngưỡng: vô hiệu hóa tài khoản (TrangThai=0)
 * - Đăng nhập đúng: xóa bộ đếm sai
 */
public class AuthService {
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final DangNhapSaiDAO dangNhapSaiDAO = new DangNhapSaiDAO();
    private final AdminConfigDAO configDAO = new AdminConfigDAO();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        KetQuaDangNhap kq = dangNhapChiTiet(tenDangNhap, matKhau, null);
        return kq.isThanhCong() ? kq.getTaiKhoan() : null;
    }

    /**
     * @param chiChoPhepVaiTro null = mọi vai trò; SINHVIEN = chỉ cổng SV
     */
    public KetQuaDangNhap dangNhapChiTiet(String tenDangNhap, String matKhau, VaiTro chiChoPhepVaiTro)
            throws SQLException {
        if (tenDangNhap == null || tenDangNhap.isBlank() || matKhau == null) {
            return KetQuaDangNhap.thatBai("Vui lòng nhập đầy đủ thông tin");
        }
        tenDangNhap = tenDangNhap.trim();

        CauHinhBaoMat cfg = layCauHinhAnToan();
        int maxSai = Math.max(1, cfg.getSoLanDangNhapSaiToiDa());
        int khoaPhut = Math.max(1, cfg.getThoiGianKhoaPhut());

        DangNhapSaiDAO.BanGhi banGhi = dangNhapSaiDAO.lay(tenDangNhap);
        if (banGhi != null && banGhi.khoaDen != null && banGhi.khoaDen.isAfter(LocalDateTime.now())) {
            long conPhut = ChronoUnit.MINUTES.between(LocalDateTime.now(), banGhi.khoaDen) + 1;
            return KetQuaDangNhap.khoaTam(
                    "Tài khoản bị khóa tạm do đăng nhập sai nhiều lần. Thử lại sau ~"
                            + conPhut + " phút (đến " + banGhi.khoaDen.format(FMT) + ").");
        }

        TaiKhoan tk = taiKhoanDAO.timTheoTenDangNhap(tenDangNhap);
        if (tk == null) {
            ghiNhanLanSai(tenDangNhap, banGhi, maxSai, khoaPhut);
            return KetQuaDangNhap.thatBai("Sai tên đăng nhập hoặc mật khẩu");
        }

        if (!tk.isTrangThai()) {
            return KetQuaDangNhap.voHieuHoa(
                    "Tài khoản đã bị vô hiệu hóa. Liên hệ Admin / Phòng Đào tạo để mở lại.");
        }

        if (chiChoPhepVaiTro != null && tk.getVaiTro() != chiChoPhepVaiTro) {
            return KetQuaDangNhap.thatBai(
                    chiChoPhepVaiTro == VaiTro.SINHVIEN
                            ? "Cổng này chỉ dành cho Sinh viên. Cán bộ vui lòng dùng form đăng nhập chính."
                            : "Tài khoản không được phép đăng nhập tại đây.");
        }

        if (!PasswordUtils.matches(matKhau, tk.getMatKhauHash())) {
            int soLan = ghiNhanLanSai(tenDangNhap, banGhi, maxSai, khoaPhut);
            if (soLan >= maxSai * 2) {
                taiKhoanDAO.datTrangThai(tk.getMaTK(), false);
                return KetQuaDangNhap.voHieuHoa(
                        "Đăng nhập sai quá nhiều lần. Tài khoản đã bị VÔ HIỆU HÓA. Liên hệ Admin để mở khóa.");
            }
            if (soLan >= maxSai) {
                return KetQuaDangNhap.khoaTam(
                        "Sai mật khẩu " + soLan + "/" + maxSai
                                + " lần. Tài khoản bị KHÓA TẠM " + khoaPhut + " phút.");
            }
            return KetQuaDangNhap.thatBai(
                    "Sai tên đăng nhập hoặc mật khẩu. Còn " + (maxSai - soLan)
                            + " lần thử trước khi bị khóa tạm.");
        }

        dangNhapSaiDAO.xoa(tenDangNhap);
        return KetQuaDangNhap.thanhCong(tk);
    }

    private int ghiNhanLanSai(String ten, DangNhapSaiDAO.BanGhi cu, int maxSai, int khoaPhut)
            throws SQLException {
        int soLan = (cu == null ? 0 : cu.soLanSai) + 1;
        LocalDateTime khoaDen = null;
        if (soLan >= maxSai) {
            khoaDen = LocalDateTime.now().plusMinutes(khoaPhut);
        }
        dangNhapSaiDAO.ghiNhanSai(ten, soLan, khoaDen);
        return soLan;
    }

    private CauHinhBaoMat layCauHinhAnToan() {
        try {
            CauHinhBaoMat b = configDAO.layCauHinhBaoMat();
            return b != null ? b : new CauHinhBaoMat();
        } catch (Exception e) {
            return new CauHinhBaoMat();
        }
    }
}