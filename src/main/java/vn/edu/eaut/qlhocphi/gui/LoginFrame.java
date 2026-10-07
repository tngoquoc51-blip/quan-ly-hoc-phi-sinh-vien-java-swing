package vn.edu.eaut.qlhocphi.gui;

import vn.edu.eaut.qlhocphi.bus.AuthService;
import vn.edu.eaut.qlhocphi.bus.NhatKyHeThongService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.KetQuaDangNhap;
import vn.edu.eaut.qlhocphi.model.VaiTro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * Màn hình đăng nhập - toàn bộ nền là gradient xanh, bên trên là 2 thẻ trắng nổi:
 *  - Thẻ trái: giới thiệu + nút "Tra cứu công nợ" (không cần đăng nhập)
 *  - Thẻ phải: form đăng nhập
 * Thao tác xác thực (gọi CSDL) được chạy bằng SwingWorker để không làm treo giao diện.
 */
public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final NhatKyHeThongService nhatKyHeThongService = new NhatKyHeThongService();

    private JTextField txtTenDangNhap;
    private JPasswordField txtMatKhau;
    private JButton btnDangNhap;
    private JLabel lblThongBao;

    public LoginFrame() {
        setTitle("Đăng Nhập - Hệ Thống Quản Lý Học Phí");
        setSize(900, 680);
        setMinimumSize(new Dimension(820, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setContentPane(buildNenGradient());
    }

    /** Nền toàn màn hình: gradient xanh đậm -> xanh sáng, chứa tiêu đề + 2 thẻ trắng. */
    private JPanel buildNenGradient() {
        JPanel nen = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0D, 0x25, 0x66),
                        getWidth(), getHeight(), UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                UITheme.veHoaTietHocTap(g2, getWidth(), getHeight());
                g2.dispose();
                // KHÔNG gọi super.paintComponent(g) ở đây - vì panel opaque sẽ vẽ đè lên gradient vừa vẽ.
            }
        };
        nen.setOpaque(false);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));

        noiDung.add(buildTieuDeTren());
        noiDung.add(Box.createRigidArea(new Dimension(0, 26)));
        noiDung.add(buildHang2The());
        noiDung.add(Box.createRigidArea(new Dimension(0, 24)));
        noiDung.add(buildHangTinhNang());

        nen.add(noiDung, new GridBagConstraints());
        return nen;
    }

    // ================== Tiêu đề trên cùng (chữ đậm, trên nền xanh) ==================

    private JPanel buildTieuDeTren() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("\uD83C\uDF93", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        icon.setPreferredSize(new Dimension(56, 56));
        icon.setMaximumSize(new Dimension(56, 56));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("HỆ THỐNG QUẢN LÝ HỌC PHÍ VÀ CÔNG NỢ SINH VIÊN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(14, 0, 6, 0));

        JLabel lblSub = new JLabel("Tra Cứu Công Nợ Và Thanh Toán Học Phí Nhanh Chóng, Chính Xác", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSub.setForeground(new Color(0xDC, 0xE7, 0xFF));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(icon);
        box.add(lblTitle);
        box.add(lblSub);
        return box;
    }

    // ================== 2 thẻ trắng nằm ngang ==================

    private JPanel buildHang2The() {
        JPanel hang = new JPanel();
        hang.setOpaque(false);
        hang.setLayout(new BoxLayout(hang, BoxLayout.X_AXIS));

        JPanel theTraCuu = theTrang(460);
        theTraCuu.setLayout(new BoxLayout(theTraCuu, BoxLayout.Y_AXIS));
        dienNoiDungTraCuu(theTraCuu);

        JPanel theDangNhap = theTrang(360);
        theDangNhap.setLayout(new BoxLayout(theDangNhap, BoxLayout.Y_AXIS));
        dienNoiDungDangNhap(theDangNhap);

        hang.add(theTraCuu);
        hang.add(Box.createRigidArea(new Dimension(24, 0)));
        hang.add(theDangNhap);
        return hang;
    }

    /** Tạo 1 "thẻ" (card) trắng, bo góc, có đổ bóng nhẹ, độ rộng cố định. */
    private JPanel theTrang(int rong) {
        JPanel the = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fill(new RoundRectangle2D.Float(4, 6, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 6, 20, 20));
                g2.dispose();
            }
        };
        the.setOpaque(false);
        the.setBorder(new EmptyBorder(26, 30, 26, 30));
        the.setPreferredSize(new Dimension(rong, 380));
        the.setMaximumSize(new Dimension(rong, 380));
        return the;
    }

    private void dienNoiDungTraCuu(JPanel the) {
        JLabel iconTron = iconTronMau("\uD83D\uDC65", UITheme.PRIMARY, 40);
        iconTron.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tieuDe = new JLabel("DÀNH CHO SINH VIÊN TRA CỨU");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        tieuDe.setBorder(new EmptyBorder(12, 0, 6, 0));

        JLabel moTa = new JLabel("<html></html>");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTa.setBorder(new EmptyBorder(0, 0, 18, 0));

        the.add(iconTron);
        the.add(tieuDe);
        the.add(moTa);
        the.add(dongBuoc("1", "Nhập Mã Sinh Viên Để Tra Cứu"));
        the.add(Box.createRigidArea(new Dimension(0, 10)));
        the.add(dongBuoc("2", "Xem Danh Sách Hóa Đơn Và Công Nợ"));
        the.add(Box.createRigidArea(new Dimension(0, 10)));
        the.add(dongBuoc("3", "Thanh Toán Trực Tuyến (Mô Phỏng)"));
        the.add(Box.createVerticalGlue());
        the.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnTraCuu = UITheme.primaryButton("ĐĂNG NHẬP SINH VIÊN");
        btnTraCuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTraCuu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnTraCuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTraCuu.addActionListener(e -> new StudentLoginFrame(this).setVisible(true));
        the.add(btnTraCuu);
    }

    private void dienNoiDungDangNhap(JPanel the) {
        JLabel iconTron = iconTronMau("\uD83D\uDD12", UITheme.PRIMARY, 52);
        iconTron.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(14, 0, 22, 0));

        txtTenDangNhap = UIUtils.textField(18);

        txtMatKhau = new JPasswordField(18);
        txtMatKhau.setFont(UITheme.FONT_BASE);
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        lblThongBao = new JLabel(" ", SwingConstants.CENTER);
        lblThongBao.setForeground(UITheme.DANGER);
        lblThongBao.setFont(UITheme.FONT_BASE);
        lblThongBao.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnDangNhap = UITheme.primaryButton("ĐĂNG NHẬP");
        btnDangNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel lblTenDangNhap = boldLabel("Tên Đăng Nhập");
        lblTenDangNhap.setHorizontalAlignment(SwingConstants.CENTER);
        lblTenDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMatKhau = boldLabel("Mật Khẩu");
        lblMatKhau.setHorizontalAlignment(SwingConstants.CENTER);
        lblMatKhau.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtTenDangNhap.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtMatKhau.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtTenDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtMatKhau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton lnkQuenMK = UITheme.lienKetChu("Quên Mật Khẩu?");
        lnkQuenMK.setAlignmentX(Component.CENTER_ALIGNMENT);
        lnkQuenMK.addActionListener(e ->
                new vn.edu.eaut.qlhocphi.gui.sinhvien.QuenMatKhauDialog(this, this::moTiepSauKhiKhoiPhuc)
                        .setVisible(true));

        // Bấm Enter ở ô Tên đăng nhập -> nhảy xuống ô Mật khẩu (không submit form)
        txtTenDangNhap.addActionListener(e -> txtMatKhau.requestFocusInWindow());

        the.add(iconTron);
        the.add(lblTitle);
        the.add(lblTenDangNhap);
        the.add(Box.createRigidArea(new Dimension(0, 6)));
        the.add(txtTenDangNhap);
        the.add(Box.createRigidArea(new Dimension(0, 16)));
        the.add(lblMatKhau);
        the.add(Box.createRigidArea(new Dimension(0, 6)));
        the.add(txtMatKhau);
        the.add(Box.createRigidArea(new Dimension(0, 6)));
        the.add(lnkQuenMK);
        the.add(Box.createRigidArea(new Dimension(0, 6)));
        the.add(lblThongBao);
        the.add(Box.createVerticalGlue());
        the.add(btnDangNhap);

        btnDangNhap.addActionListener(e -> thucHienDangNhap());
        getRootPane().setDefaultButton(btnDangNhap);
    }

    /**
     * Sau khi "Quên mật khẩu" (mở từ panel Đăng nhập Admin/Kế toán) xác minh thành
     * công 1 tài khoản: nếu tài khoản đó đang bật cờ "bắt buộc đổi mật khẩu" (luôn
     * đúng ngay sau khi khôi phục), mở dialog đổi mật khẩu trước, chỉ mở MainFrame
     * SAU KHI đổi xong; nếu không thì vào thẳng.
     */
    private void moTiepSauKhiKhoiPhuc(TaiKhoan tk) {
        // Sinh viên không được vào cổng cán bộ (kể cả sau khi quên mật khẩu)
        if (tk.getVaiTro() == vn.edu.eaut.qlhocphi.model.VaiTro.SINHVIEN) {
            JOptionPane.showMessageDialog(this,
                    "Tài khoản sinh viên vui lòng dùng nút \"ĐĂNG NHẬP SINH VIÊN\".",
                    "Sai cổng đăng nhập", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (tk.isBatBuocDoiMatKhau()) {
            vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog dialog =
                    new vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog(this, tk, true, () -> {
                        new MainFrame(tk).setVisible(true);
                        dispose();
                    });
            dialog.setVisible(true);
        } else {
            new MainFrame(tk).setVisible(true);
            dispose();
        }
    }

    private JLabel boldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
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
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, kichThuoc / 2));
        icon.setForeground(Color.WHITE);
        icon.setPreferredSize(new Dimension(kichThuoc, kichThuoc));
        icon.setMaximumSize(new Dimension(kichThuoc, kichThuoc));
        return icon;
    }

    private JPanel dongBuoc(String so, String noiDung) {
        JPanel dong = new JPanel(new BorderLayout(10, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lblSo = new JLabel(so, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSo.setPreferredSize(new Dimension(24, 24));
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSo.setForeground(Color.WHITE);

        JLabel lblText = new JLabel(noiDung);
        lblText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblText.setForeground(UITheme.TEXT_PRIMARY);

        dong.add(lblSo, BorderLayout.WEST);
        dong.add(lblText, BorderLayout.CENTER);
        return dong;
    }

    // ================== Hàng tính năng dưới cùng (trên nền xanh) ==================

    private JPanel buildHangTinhNang() {
        JPanel hang = new JPanel(new GridLayout(1, 4, 18, 0));
        hang.setOpaque(false);
        hang.setMaximumSize(new Dimension(840, 60));

        hang.add(tinhNang("\uD83D\uDEE1\uFE0F", "Bảo Mật Tuyệt Đối"));
        hang.add(tinhNang("\uD83D\uDD10", "Thanh Toán An Toàn"));
        hang.add(tinhNang("\uD83C\uDFA7", "Hỗ Trợ 24/7"));
        hang.add(tinhNang("\u2705", "Xác Nhận Nhanh Chóng"));
        return hang;
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
                KetQuaDangNhap kq = authService.dangNhapChiTiet(tenDangNhap, matKhau, null);
                if (kq.isThanhCong() && kq.getTaiKhoan().getVaiTro() == VaiTro.SINHVIEN) {
                    return KetQuaDangNhap.thatBai(
                            "Sinh viên vui lòng dùng nút ĐĂNG NHẬP SINH VIÊN.");
                }
                return kq;
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
                    nhatKyHeThongService.ghi(tk, "DANG_NHAP", tk.getVaiTro().toString(),
                            "Đăng nhập thành công: " + tk.getTenDangNhap() + " (" + tk.getHoTen() + ")");
                    if (tk.isBatBuocDoiMatKhau()) {
                        vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog dialog =
                                new vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog(
                                        LoginFrame.this, tk, true, () -> {
                                    new MainFrame(tk).setVisible(true);
                                    dispose();
                                });
                        dialog.setVisible(true);
                        return;
                    }
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
    }
