package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sao lưu / phục hồi CSDL MySQL bằng mysqldump / mysql.
 * Dùng đường dẫn tuyệt đối tới MySQL Server 9.4 (không phụ thuộc PATH Windows).
 */
public class BackupService {

    /** Đường dẫn tuyệt đối – máy bạn đã xác nhận chạy được */
    private static final String MYSQLDUMP =
            "C:\\Program Files\\MySQL\\MySQL Server 9.4\\bin\\mysqldump.exe";
    private static final String MYSQL =
            "C:\\Program Files\\MySQL\\MySQL Server 9.4\\bin\\mysql.exe";

    private static class ThongTinKetNoi {
        String host = "localhost";
        String port = "3310";
        String database = "qlhocphi";
    }

    /** Sao lưu toàn bộ CSDL ra 1 file .sql. */
    public void saoLuu(File fileDich) throws IOException, InterruptedException {
        ThongTinKetNoi tt = docThongTinKetNoi();
        String user = AppConfig.get("db.username");
        String pass = AppConfig.get("db.password");

        ProcessBuilder pb = new ProcessBuilder(
                MYSQLDUMP,
                "-h", tt.host,
                "-P", tt.port,
                "-u", user,
                "-p" + pass,
                "--databases", tt.database,
                "--result-file=" + fileDich.getAbsolutePath()
        );
        pb.redirectErrorStream(false);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String loi = new String(process.getErrorStream().readAllBytes());
            throw new IOException("mysqldump kết thúc với mã lỗi " + exitCode
                    + (loi.isBlank() ? "" : (": " + loi)));
        }
    }

    /** Phục hồi CSDL từ file .sql (ghi đè dữ liệu hiện tại). */
    public void phucHoi(File fileNguon) throws IOException, InterruptedException {
        ThongTinKetNoi tt = docThongTinKetNoi();
        String user = AppConfig.get("db.username");
        String pass = AppConfig.get("db.password");

        ProcessBuilder pb = new ProcessBuilder(
                MYSQL,
                "-h", tt.host,
                "-P", tt.port,
                "-u", user,
                "-p" + pass,
                tt.database
        );
        pb.redirectInput(fileNguon);
        pb.redirectErrorStream(false);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String loi = new String(process.getErrorStream().readAllBytes());
            throw new IOException("mysql kết thúc với mã lỗi " + exitCode
                    + (loi.isBlank() ? "" : (": " + loi)));
        }
    }

    /** Tách host/port/database từ db.url */
    private ThongTinKetNoi docThongTinKetNoi() {
        ThongTinKetNoi tt = new ThongTinKetNoi();
        String url = AppConfig.get("db.url");
        if (url == null) return tt;
        Pattern pattern = Pattern.compile("jdbc:mysql://([^:/]+)(:(\\d+))?/([^?]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            tt.host = matcher.group(1);
            if (matcher.group(3) != null) tt.port = matcher.group(3);
            tt.database = matcher.group(4);
        }
        return tt;
    }
}