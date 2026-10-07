package vn.edu.eaut.qlhocphi.gui;

import vn.edu.eaut.qlhocphi.bus.AuditContext;
import vn.edu.eaut.qlhocphi.bus.AuthService;
import vn.edu.eaut.qlhocphi.bus.NhatKyHeThongService;
import vn.edu.eaut.qlhocphi.bus.TaiKhoanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.google.GoogleAuthService;
import vn.edu.eaut.qlhocphi.google.GoogleOAuthConfig;
import vn.edu.eaut.qlhocphi.gui.sinhvien.ChonTaiKhoanDialog;
import vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog;
import vn.edu.eaut.qlhocphi.gui.sinhvien.QuenMatKhauDialog;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;
import vn.edu.eaut.qlhocphi.model.KetQuaDangNhap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Trang đăng nhập RIÊNG cho Sinh viên - nền gradient xanh navy đậm, có điểm sáng
 * trang trí, 2 thẻ trắng: trái là giới thiệu + link tra cứu không cần tài khoản,
 * phải là form đăng nhập đầy đủ (icon ô nhập, ẩn/hiện mật khẩu, quên mật khẩu,
 * trợ giúp, đăng nhập Google).
 */
public class StudentLoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private final NhatKyHeThongService nhatKyHeThongService = new NhatKyHeThongService();

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap, btnMatKhauEye;
    private JLabel lblThongBao;
    private boolean matKhauDangHien = false;

    public StudentLoginFrame(JFrame chaMe) {
        setTitle("Đăng Nhập Sinh Viên - Hệ Thống Quản Lý Học Phí");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(chaMe);
        setLayout(new BorderLayout());
        setContentPane(buildNen());
    }

    private JPanel buildNen() {
        JPanel nen = new JPanel(new BorderLayout()) {
            private final Random rd = new Random(7);
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0A, 0x18, 0x40),
                        getWidth(), getHeight(), new Color(0x1E, 0x3A, 0x8A));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                UITheme.veHoaTietHocTap(g2, getWidth(), getHeight());
                // Chấm sáng trang trí
                g2.setColor(new Color(255, 255, 255, 60));
                for (int i = 0; i < 26; i++) {
                    int x = rd.nextInt(Math.max(getWidth(), 1));
                    int y = rd.nextInt(Math.max(getHeight(), 1));
                    int s = 2 + rd.nextInt(3);
                    g2.fillOval(x, y, s, s);
                }
                g2.dispose();
            }
        };
        nen.setOpaque(true);

        JPanel giua = new JPanel(new GridBagLayout());
        giua.setOpaque(false);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.add(buildHang2The());
        noiDung.add(Box.createRigidArea(new Dimension(0, 24)));
        noiDung.add(buildHangTinhNang());

        giua.add(noiDung, new GridBagConstraints());
        nen.add(giua, BorderLayout.CENTER);
        return nen;
    }

    // ================== 2 thẻ ==================

    private JPanel buildHang2The() {
        JPanel hang = new JPanel();
        hang.setOpaque(false);
        hang.setLayout(new BoxLayout(hang, BoxLayout.X_AXIS));

        JPanel theTrai = theTrang(430);
        theTrai.setLayout(new BoxLayout(theTrai, BoxLayout.Y_AXIS));
        dienNoiDungGioiThieu(theTrai);

        JPanel thePhai = theTrang(380);
        thePhai.setLayout(new BoxLayout(thePhai, BoxLayout.Y_AXIS));
        dienNoiDungDangNhap(thePhai);

        hang.add(theTrai);
        hang.add(Box.createRigidArea(new Dimension(24, 0)));
        hang.add(thePhai);
        return hang;
    }

    private JPanel theTrang(int rong) {
        JPanel the = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.dispose();
            }
        };
        the.setOpaque(false);
        the.setBorder(new EmptyBorder(30, 32, 30, 32));
        the.setPreferredSize(new Dimension(rong, 460));
        the.setMaximumSize(new Dimension(rong, 460));
        return the;
    }

    // ================== Thẻ trái: giới thiệu ==================

    private void dienNoiDungGioiThieu(JPanel the) {
        JPanel dongIcon = new JPanel(new BorderLayout(12, 0));
        dongIcon.setOpaque(false);
        dongIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        dongIcon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        dongIcon.add(iconTronMau("\uD83C\uDF93", UITheme.PRIMARY, 44), BorderLayout.WEST);
        JLabel tieuDe = new JLabel("DÀNH CHO SINH VIÊN");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dongIcon.add(tieuDe, BorderLayout.CENTER);

        JLabel moTa = new JLabel("<html></html>");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTa.setBorder(new EmptyBorder(16, 0, 20, 0));

        the.add(dongIcon);
        the.add(moTa);

        the.add(dongBuoc("1", "Đăng Nhập Bằng Tài Khoản Sinh Viên"));
        the.add(Box.createRigidArea(new Dimension(0, 12)));
        the.add(dongBuoc("2", "Xem Danh Sách Hóa Đơn Và Công Nợ"));
        the.add(Box.createRigidArea(new Dimension(0, 12)));
        the.add(dongBuoc("3", "Thanh Toán Học Phí Trực Tuyến"));

        the.add(Box.createVerticalGlue());

        JButton btnTraCuu = UITheme.secondaryButton("Tra Cứu Không Cần Đăng Nhập");
        btnTraCuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTraCuu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnTraCuu.addActionListener(e -> new TraCuuFrame(this).setVisible(true));
        the.add(btnTraCuu);
    }

    private JPanel dongBuoc(String so, String noiDung) {
        JPanel dong = new JPanel(new BorderLayout(12, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        dong.add(iconTronMau(so, UITheme.PRIMARY, 26), BorderLayout.WEST);
        JLabel lblText = new JLabel(noiDung);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblText.setForeground(UITheme.TEXT_PRIMARY);
        dong.add(lblText, BorderLayout.CENTER);
        return dong;
    }

    // ================== Thẻ phải: form đăng nhập ==================

    private void dienNoiDungDangNhap(JPanel the) {
        JLabel iconTron = iconTronMau("\uD83D\uDD12", UITheme.PRIMARY, 52);
        iconTron.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(12, 0, 20, 0));

        // Ô tên đăng nhập có icon người dùng
        txtTenDangNhap = new JTextField();
        txtTenDangNhap.setFont(UITheme.FONT_BASE);
        txtTenDangNhap.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        JPanel oTenDangNhap = oNhapCoIcon("\uD83D\uDC64", txtTenDangNhap, null);
        oTenDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        oTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // Ô mật khẩu có icon khóa + nút mắt/ẩn
        txtMatKhau = new JPasswordField();
        txtMatKhau.setFont(UITheme.FONT_BASE);
        txtMatKhau.setBorder(BorderFactory.createEmptyBorder(8, 6, 8, 6));
        btnMatKhauEye = new JButton("\uD83D\uDC41");
        btnMatKhauEye.setBorderPainted(false);
        btnMatKhauEye.setContentAreaFilled(false);
        btnMatKhauEye.setFocusPainted(false);
        btnMatKhauEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMatKhauEye.addActionListener(e -> toggleHienMatKhau());
        JPanel oMatKhau = oNhapCoIcon("\uD83D\uDD12", txtMatKhau, btnMatKhauEye);
        oMatKhau.setAlignmentX(Component.CENTER_ALIGNMENT);
        oMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        // Hàng: Quên mật khẩu - Trợ giúp
        JPanel hangLink = new JPanel(new BorderLayout());
        hangLink.setOpaque(false);
        hangLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        hangLink.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JButton lnkQuenMK = UITheme.lienKetChu("Quên Mật Khẩu?");
        lnkQuenMK.addActionListener(e ->
                new QuenMatKhauDialog(this, this::moTiepSauKhiXacThuc).setVisible(true));
        JButton lnkTroGiup = UITheme.lienKetChu("Trợ Giúp");
        lnkTroGiup.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Hotline Hỗ Trợ: 0962817574\nEmail: tngoquoc51@gmail.com",
                "Trợ Giúp", JOptionPane.INFORMATION_MESSAGE));
        hangLink.add(lnkQuenMK, BorderLayout.WEST);
        hangLink.add(lnkTroGiup, BorderLayout.EAST);

        lblThongBao = new JLabel(" ", SwingConstants.CENTER);
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setFont(UITheme.FONT_BASE);
        lblThongBao.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnDangNhap = new JButton("ĐĂNG NHẬP");
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setForeground(Color.WHITE);
        btnDangNhap.setBackground(new Color(0x1E, 0x3A, 0x8A));
        btnDangNhap.setBorderPainted(false);
        btnDangNhap.setFocusPainted(false);
        btnDangNhap.setOpaque(true);
        btnDangNhap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnDangNhap.addActionListener(e -> thucHienDangNhap());

        JLabel lblChiaDoi = new JLabel("hoặc đăng nhập", SwingConstants.CENTER);
        lblChiaDoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChiaDoi.setForeground(UITheme.TEXT_MUTED);
        lblChiaDoi.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblChiaDoi.setBorder(new EmptyBorder(14, 0, 10, 0));

        JButton btnGoogle = new JButton("G   Đăng Nhập Với Google");
        btnGoogle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGoogle.setForeground(Color.WHITE);
        btnGoogle.setBackground(new Color(0xE0, 0x5A, 0x2B));
        btnGoogle.setBorderPainted(false);
        btnGoogle.setFocusPainted(false);
        btnGoogle.setOpaque(true);
        btnGoogle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGoogle.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGoogle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnGoogle.addActionListener(e -> dangNhapQuaGoogle(btnGoogle));

        JButton btnQRLogin = UITheme.secondaryButton("📷 Đăng nhập bằng mã QR");
        btnQRLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnQRLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnQRLogin.addActionListener(e -> dangNhapQuaQR());
        the.add(Box.createRigidArea(new Dimension(0, 8)));
        the.add(btnQRLogin);

        txtTenDangNhap.addActionListener(e -> txtMatKhau.requestFocusInWindow());
        getRootPane().setDefaultButton(btnDangNhap);

        the.add(iconTron);
        the.add(lblTitle);
        the.add(oTenDangNhap);
        the.add(Box.createRigidArea(new Dimension(0, 14)));
        the.add(oMatKhau);
        the.add(Box.createRigidArea(new Dimension(0, 8)));
        the.add(hangLink);
        the.add(Box.createRigidArea(new Dimension(0, 8)));
        the.add(lblThongBao);
        the.add(Box.createRigidArea(new Dimension(0, 4)));
        the.add(btnDangNhap);
        the.add(lblChiaDoi);
        the.add(btnGoogle);
    }

    private void toggleHienMatKhau() {
        matKhauDangHien = !matKhauDangHien;
        txtMatKhau.setEchoChar(matKhauDangHien ? (char) 0 : '\u2022');
        btnMatKhauEye.setText(matKhauDangHien ? "\uD83D\uDE48" : "\uD83D\uDC41");
    }

    /** Ô nhập dạng khung bo góc, có icon trái và (tùy chọn) nút/icon phải. */
    private JPanel oNhapCoIcon(String emojiTrai, JComponent oNhap, JComponent thanhPhanPhai) {
        JPanel bao = new JPanel(new BorderLayout(8, 0));
        bao.setBackground(Color.WHITE);
        bao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 12, 2, 12)));

        JLabel icon = new JLabel(emojiTrai);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        icon.setForeground(UITheme.TEXT_MUTED);
        bao.add(icon, BorderLayout.WEST);
        bao.add(oNhap, BorderLayout.CENTER);
        if (thanhPhanPhai != null) bao.add(thanhPhanPhai, BorderLayout.EAST);
        return bao;
    }

    private JButton lienKet(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(UITheme.PRIMARY);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel iconTronMau(String emoji, Color mau, int kichThuoc) {
        JLabel icon = new JLabel(emoji, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mau);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, Math.max(11, kichThuoc / 2)));
        icon.setForeground(Color.WHITE);
        icon.setPreferredSize(new Dimension(kichThuoc, kichThuoc));
        icon.setMaximumSize(new Dimension(kichThuoc, kichThuoc));
        return icon;
    }

    // ================== Hàng tính năng dưới cùng ==================

    private JPanel buildHangTinhNang() {
        JPanel thanh = new JPanel(new GridLayout(1, 4, 0, 0));
        thanh.setOpaque(true);
        thanh.setBackground(new Color(0x10, 0x24, 0x55));
        thanh.setBorder(new EmptyBorder(16, 24, 16, 24));
        thanh.setMaximumSize(new Dimension(860, 60));

        thanh.add(tinhNang("\uD83D\uDEE1\uFE0F", "Bảo Mật Tuyệt Đối"));
        thanh.add(tinhNang("\uD83D\uDD10", "Thanh Toán An Toàn"));
        thanh.add(tinhNang("\uD83C\uDFA7", "Hỗ Trợ 24/7"));
        thanh.add(tinhNang("\u2705", "Xác Nhận Nhanh Chóng"));
        return thanh;
    }

    private JPanel tinhNang(String emoji, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        JLabel icon = new JLabel(emoji);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        p.add(icon);
        p.add(lbl);
        return p;
    }

    // ================== Xử lý đăng nhập ==================

    private void thucHienDangNhap() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());

        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            lblThongBao.setText("Vui Lòng Nhập Đầy Đủ Thông Tin");
            return;
        }

        btnDangNhap.setEnabled(false);
        lblThongBao.setForeground(UITheme.TEXT_MUTED);
        lblThongBao.setText("Đang Kiểm Tra...");

        SwingWorker<KetQuaDangNhap, Void> worker = new SwingWorker<>() {
            @Override
            protected KetQuaDangNhap doInBackground() throws SQLException {
                return authService.dangNhapChiTiet(tenDangNhap, matKhau, VaiTro.SINHVIEN);
            }

            @Override
            protected void done() {
                btnDangNhap.setEnabled(true);
                try {
                    KetQuaDangNhap kq = get();
                    if (!kq.isThanhCong()) {
                        lblThongBao.setForeground(UITheme.DANGER);
                        lblThongBao.setText(kq.getThongBaoLoi());
                        return;
                    }
                    TaiKhoan tk = kq.getTaiKhoan();
                    AuditContext.datNguoiDung(tk);
                    nhatKyHeThongService.ghi(tk, "DANG_NHAP", tk.getVaiTro().toString(),
                            "Sinh viên đăng nhập: " + tk.getTenDangNhap() + " (" + tk.getHoTen() + ")");
                    new MainFrame(tk).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    lblThongBao.setForeground(UITheme.DANGER);
                    lblThongBao.setText("Không Thể Kết Nối CSDL. Kiểm Tra Cấu Hình.");
                }
            }
        };
        worker.execute();
    }

    // ================== Đăng nhập / Khôi phục mật khẩu qua Google ==================

    /**
     * "Đăng nhập bằng Google": xác thực qua Google rồi tìm tài khoản Sinh viên đã
     * được Admin gắn đúng Gmail này (cột GoogleEmail). Nếu tìm thấy và tài khoản
     * đang ở trạng thái "bắt buộc đổi mật khẩu" (lần đầu liên kết / vừa khôi phục),
     * bật dialog đổi mật khẩu trước khi vào trang chính; nếu không, vào thẳng.
     */
    private void dangNhapQuaGoogle(JButton btnGoogle) {
        if (!GoogleOAuthConfig.daCauHinh()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa Cấu Hình Google OAuth Client ID/Secret.\n"
                            + "Mở File GoogleOAuthConfig.java Trong Package 'google' Để Xem Hướng Dẫn.",
                    "Chưa Cấu Hình", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnGoogle.setEnabled(false);
        lblThongBao.setForeground(UITheme.TEXT_MUTED);
        lblThongBao.setText("Đang Mở Trình Duyệt Để Đăng Nhập Google...");

        SwingWorker<List<TaiKhoan>, Void> worker = new SwingWorker<>() {
            private String loiHienThi;

            @Override
            protected List<TaiKhoan> doInBackground() {
                try {
                    GoogleAuthService.KetQuaGoogle ketQua = googleAuthService.dangNhap();
                    List<TaiKhoan> danhSach = taiKhoanService.layDanhSachTheoGoogleEmail(ketQua.email);
                    danhSach = danhSach.stream().filter(TaiKhoan::isTrangThai).collect(java.util.stream.Collectors.toList());
                    if (danhSach.isEmpty()) {
                        loiHienThi = "Gmail " + ketQua.email + " Chưa Được Admin Liên Kết Với Tài Khoản Nào Đang Hoạt Động.\n"
                                + "Vui Lòng Liên Hệ Phòng Kế Toán Để Được Liên Kết Gmail Này.";
                        return null;
                    }
                    return danhSach;
                } catch (Exception ex) {
                    loiHienThi = "Đăng Nhập Google Thất Bại: " + rootMessage(ex);
                    return null;
                }
            }

            @Override
            protected void done() {
                btnGoogle.setEnabled(true);
                List<TaiKhoan> danhSach = get2();
                if (danhSach == null) {
                    lblThongBao.setForeground(UITheme.DANGER);
                    lblThongBao.setText(loiHienThi != null ? loiHienThi : "Đăng Nhập Google Thất Bại");
                    return;
                }
                lblThongBao.setForeground(UITheme.TEXT_MUTED);
                lblThongBao.setText(" ");
                if (danhSach.size() == 1) {
                    moTiepSauKhiXacThuc(danhSach.get(0));
                } else {
                    new ChonTaiKhoanDialog(StudentLoginFrame.this, danhSach,
                            StudentLoginFrame.this::moTiepSauKhiXacThuc).setVisible(true);
                }
            }


            /** Bọc get() để không phải try/catch InterruptedException/ExecutionException ở trên. */
            private List<TaiKhoan> get2() {
                try {
                    return get();
                } catch (Exception ex) {
                    return null;
                }
            }
        };
        worker.execute();
    }

    /**
     * Đăng nhập bằng cách quét/chọn ảnh thẻ QR đã được cấp trước đó.
     */
    private void dangNhapQuaQR() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh thẻ QR đăng nhập");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Ảnh (*.png, *.jpg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();

        lblThongBao.setForeground(UITheme.TEXT_MUTED);
        lblThongBao.setText("Đang xác thực mã QR...");

        SwingWorker<vn.edu.eaut.qlhocphi.model.TaiKhoan, Void> worker = new SwingWorker<>() {
            @Override
            protected vn.edu.eaut.qlhocphi.model.TaiKhoan doInBackground() throws Exception {
                String noiDung = vn.edu.eaut.qlhocphi.util.QrCodeUtils.docAnhQR(file);
                return new vn.edu.eaut.qlhocphi.bus.DangNhapQRService().xacThucNoiDungQR(noiDung);
            }

            @Override
            protected void done() {
                try {
                    vn.edu.eaut.qlhocphi.model.TaiKhoan tk = get();
                    if (tk == null) {
                        lblThongBao.setForeground(UITheme.DANGER);
                        lblThongBao.setText("Mã QR không hợp lệ hoặc đã bị thu hồi.");
                        return;
                    }
                    if (tk.getVaiTro() != VaiTro.SINHVIEN) {
                        lblThongBao.setForeground(UITheme.DANGER);
                        lblThongBao.setText("Mã QR này không phải tài khoản sinh viên.");
                        return;
                    }
                    AuditContext.datNguoiDung(tk);
                    nhatKyHeThongService.ghi(tk, "DANG_NHAP", tk.getVaiTro().toString(),
                            "Sinh viên đăng nhập qua thẻ QR: " + tk.getTenDangNhap());
                    new MainFrame(tk).setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    lblThongBao.setForeground(UITheme.DANGER);
                    lblThongBao.setText("Lỗi đọc mã QR: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Sau khi xác thực Google thành công (đăng nhập thẳng hoặc khôi phục mật khẩu):
     * nếu tài khoản đang bị bật "bắt buộc đổi mật khẩu" thì mở dialog bắt buộc đổi
     * trước, chỉ mở MainFrame SAU KHI đổi mật khẩu xong; nếu không thì vào thẳng.
     */
    private void moTiepSauKhiXacThuc(TaiKhoan tk) {
        if (tk == null) return;
        if (tk.getVaiTro() != VaiTro.SINHVIEN) {
            JOptionPane.showMessageDialog(this,
                    "Cổng này chỉ dành cho sinh viên.\nCán bộ (Admin / Phòng Đào tạo / Kế toán) vui lòng đăng nhập ở màn hình chính.",
                    "Sai cổng đăng nhập", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AuditContext.datNguoiDung(tk);
        nhatKyHeThongService.ghi(tk, "DANG_NHAP", tk.getVaiTro().toString(),
                "Sinh viên đăng nhập qua Google: " + tk.getTenDangNhap() + " (" + tk.getHoTen() + ")");
        if (tk.isBatBuocDoiMatKhau()) {
            DoiMatKhauDialog dialog = new DoiMatKhauDialog(this, tk, true, () -> {
                new MainFrame(tk).setVisible(true);
                dispose();
            });
            dialog.setVisible(true);
        } else {
            new MainFrame(tk).setVisible(true);
            dispose();
        }
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}