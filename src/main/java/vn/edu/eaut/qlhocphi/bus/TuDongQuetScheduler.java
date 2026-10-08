package vn.edu.eaut.qlhocphi.bus;

import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Bo lich tu dong quet thu hoc phi tu Vi dien tu cua sinh vien, thay the viec
 * Admin phai bam tay "Quet Thu Ngay". Tu thoi diem duoc cau hinh, cu moi
 * CHU KY PHUT se tu dong goi lai ThuTuDongService.quetMotLan() - CUNG 1
 * nghiep vu voi nut thu cong, dam bao ket qua dong nhat.
 * Singleton dung chung toan app.
 */
public class TuDongQuetScheduler {
    private static final TuDongQuetScheduler INSTANCE = new TuDongQuetScheduler();
    public static TuDongQuetScheduler getInstance() { return INSTANCE; }

    private final ThuTuDongService thuTuDongService = new ThuTuDongService();
    private final List<Consumer<ThuTuDongService.KetQuaQuet>> nguoiNgheKetQua = new ArrayList<>();

    private Timer timerKiemTra;
    private LocalDateTime thoiDiemBatDau;
    private int chuKyPhut = 60;
    private boolean dangBat = false;
    private boolean daChayLanDau = false;
    private LocalDateTime lanQuetGanNhat;

    private TuDongQuetScheduler() {}

    /** Bat tu dong quet, ke tu thoiDiemBatDau, moi chuKyPhut quet lai 1 lan. */
    public void batDau(LocalDateTime thoiDiemBatDau, int chuKyPhut) {
        dungLai();
        this.thoiDiemBatDau = thoiDiemBatDau;
        this.chuKyPhut = Math.max(1, chuKyPhut);
        this.dangBat = true;
        this.daChayLanDau = false;

        timerKiemTra = new Timer(30_000, e -> kiemTraVaQuetNeuDenGio());
        timerKiemTra.setInitialDelay(0);
        timerKiemTra.start();

        // Ghi Health: đang bật
        schedulerTrangThai.dangChay("THU_TU_DONG",
                "Tu dong quet DANG BAT, chu ky " + this.chuKyPhut + " phut");
    }


    public void dungLai() {
        if (timerKiemTra != null) {
            timerKiemTra.stop();
            timerKiemTra = null;
        }
        boolean truocDoBat = dangBat;
        dangBat = false;
        if (truocDoBat) {
            schedulerTrangThai.dung("THU_TU_DONG", "Tu dong quet DA TAT");
        }
    }

    private void kiemTraVaQuetNeuDenGio() {
        LocalDateTime bayGio = LocalDateTime.now();
        if (thoiDiemBatDau == null || bayGio.isBefore(thoiDiemBatDau)) return;

        boolean denLuotQuet = !daChayLanDau || lanQuetGanNhat == null
                || Duration.between(lanQuetGanNhat, bayGio).toMinutes() >= chuKyPhut;
        if (!denLuotQuet) return;

        daChayLanDau = true;
        lanQuetGanNhat = bayGio;
        thucHienQuetNen();
    }

    private void thucHienQuetNen() {
        schedulerTrangThai.dangChay("THU_TU_DONG", "Dang quet lich thu...");

        SwingWorker<ThuTuDongService.KetQuaQuet, Void> worker = new SwingWorker<>() {
            @Override
            protected ThuTuDongService.KetQuaQuet doInBackground() throws Exception {
                return thuTuDongService.quetMotLan();
            }

            @Override
            protected void done() {
                try {
                    ThuTuDongService.KetQuaQuet kq = get();
                    // Đổi getSoDaThu() / toString() cho khớp class KetQuaQuet của bạn
                    String ketQua = "Quet xong: " + (kq != null ? kq.toString() : "OK");
                    schedulerTrangThai.dung("THU_TU_DONG", ketQua);

                    for (Consumer<ThuTuDongService.KetQuaQuet> nghe : nguoiNgheKetQua) {
                        nghe.accept(kq);
                    }
                } catch (Exception ex) {
                    schedulerTrangThai.dung("THU_TU_DONG", "Loi quet: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
    private final SchedulerTrangThaiService schedulerTrangThai = new SchedulerTrangThaiService();
    public void themNguoiNghe(Consumer<ThuTuDongService.KetQuaQuet> n) { nguoiNgheKetQua.add(n); }
    public void xoaNguoiNghe(Consumer<ThuTuDongService.KetQuaQuet> n) { nguoiNgheKetQua.remove(n); }

    public boolean dangBat() { return dangBat; }
    public LocalDateTime layThoiDiemBatDau() { return thoiDiemBatDau; }
    public int layChuKyPhut() { return chuKyPhut; }

    public String moTaTrangThai() {
        if (!dangBat || thoiDiemBatDau == null) return "Tự động: ĐANG TẮT";
        DateTimeFormatter dd = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        String lanQuet = lanQuetGanNhat == null ? "chưa quét lần nào"
                : "lần quét gần nhất " + dd.format(lanQuetGanNhat);
        return "Tự động: ĐANG BẬT (từ " + dd.format(thoiDiemBatDau)
                + ", mỗi " + chuKyPhut + " phút, " + lanQuet + ")";
    }
}