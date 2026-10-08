package vn.edu.eaut.qlhocphi.bus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Chay nen trong suot vong doi ung dung, dinh ky quet toan bo hoa don qua han de
 * tu dong gui nhac no (SV qua email -> Phu huynh qua SMS khi qua han lau).
 * Khoi dong 1 lan duy nhat trong App.main(), tuong tu TuDongThuHocPhiScheduler.
 */
public class NhacNoTuDongScheduler {
    private static final NhacNoTuDongScheduler INSTANCE = new NhacNoTuDongScheduler();

    public static NhacNoTuDongScheduler getInstance() {
        return INSTANCE;
    }

    private final NhacNoTuDongService nhacNoTuDongService = new NhacNoTuDongService();
    private final SchedulerTrangThaiService schedulerTrangThai = new SchedulerTrangThaiService();
    private ScheduledExecutorService executor;

    private NhacNoTuDongScheduler() {}

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) return;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "nhac-no-tu-dong");
            t.setDaemon(true);
            return t;
        });
        // Giữ chu kỳ bạn đang dùng (1 phút = test; production nên 24h)
        executor.scheduleAtFixedRate(this::quetAnToan, 0, 1, TimeUnit.MINUTES);
        schedulerTrangThai.dangChay("NHAC_NO", "Scheduler nhac no DA BAT");
    }

    public synchronized void stop() {
        if (executor != null) executor.shutdownNow();
        schedulerTrangThai.dung("NHAC_NO", "Scheduler nhac no DA TAT");
    }

    private void quetAnToan() {
        try {
            schedulerTrangThai.dangChay("NHAC_NO", "Dang quet va gui nhac no...");
            nhacNoTuDongService.quetVaGuiNhacNo();
            schedulerTrangThai.dung("NHAC_NO", "Da quet xong 1 vong nhac no");
        } catch (Exception ex) {
            System.err.println("Loi khi quet nhac no tu dong: " + ex.getMessage());
            schedulerTrangThai.dung("NHAC_NO", "Loi: " + ex.getMessage());
        }
    }
}