package vn.edu.eaut.qlhocphi.bus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TuDongThuHocPhiScheduler {
    private static final TuDongThuHocPhiScheduler INSTANCE = new TuDongThuHocPhiScheduler();
    private static final long CHU_KY_PHUT = 15;

    private final ThuTuDongService thuTuDongService = new ThuTuDongService();
    private final SchedulerTrangThaiService schedulerTrangThai = new SchedulerTrangThaiService();
    private ScheduledExecutorService executor;

    private TuDongThuHocPhiScheduler() {}

    public static TuDongThuHocPhiScheduler getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) return;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "tu-dong-thu-hoc-phi");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(this::quetAnToan, 0, CHU_KY_PHUT, TimeUnit.MINUTES);
        schedulerTrangThai.dangChay("THU_TU_DONG", "Scheduler 15 phut DA BAT");
    }

    public synchronized void stop() {
        if (executor != null) executor.shutdownNow();
        schedulerTrangThai.dung("THU_TU_DONG", "Scheduler 15 phut DA TAT");
    }

    private void quetAnToan() {
        try {
            schedulerTrangThai.dangChay("THU_TU_DONG", "Dang quet lich thu tu dong...");
            thuTuDongService.quetMotLan();
            schedulerTrangThai.dung("THU_TU_DONG", "Da quet xong 1 vong (scheduler 15 phut)");
        } catch (Exception ex) {
            System.err.println("Loi khi quet thu hoc phi tu dong: " + ex.getMessage());
            schedulerTrangThai.dung("THU_TU_DONG", "Loi: " + ex.getMessage());
        }
    }
}