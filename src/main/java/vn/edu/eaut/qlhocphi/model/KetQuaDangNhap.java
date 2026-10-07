package vn.edu.eaut.qlhocphi.model;

/** Kết quả xác thực đăng nhập – kèm thông báo lỗi rõ. */
public class KetQuaDangNhap {
    private final TaiKhoan taiKhoan;
    private final String thongBaoLoi;
    private final boolean biKhoaTam;
    private final boolean biVoHieuHoa;

    private KetQuaDangNhap(TaiKhoan tk, String loi, boolean khoaTam, boolean voHieu) {
        this.taiKhoan = tk;
        this.thongBaoLoi = loi;
        this.biKhoaTam = khoaTam;
        this.biVoHieuHoa = voHieu;
    }

    public static KetQuaDangNhap thanhCong(TaiKhoan tk) {
        return new KetQuaDangNhap(tk, null, false, false);
    }

    public static KetQuaDangNhap thatBai(String loi) {
        return new KetQuaDangNhap(null, loi, false, false);
    }

    public static KetQuaDangNhap khoaTam(String loi) {
        return new KetQuaDangNhap(null, loi, true, false);
    }

    public static KetQuaDangNhap voHieuHoa(String loi) {
        return new KetQuaDangNhap(null, loi, false, true);
    }

    public boolean isThanhCong() { return taiKhoan != null; }
    public TaiKhoan getTaiKhoan() { return taiKhoan; }
    public String getThongBaoLoi() {
        return thongBaoLoi != null ? thongBaoLoi : "Đăng nhập thất bại";
    }
    public boolean isBiKhoaTam() { return biKhoaTam; }
    public boolean isBiVoHieuHoa() { return biVoHieuHoa; }
}