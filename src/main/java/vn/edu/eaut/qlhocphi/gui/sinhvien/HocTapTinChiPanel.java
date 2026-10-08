package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.TinChiService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.TinChiSinhVien;
import vn.edu.eaut.qlhocphi.model.TinChiTheoNam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Locale;

/**
 * Tiến độ học tập & tín chỉ — điểm TB hệ 10/4, double-click năm xem chi tiết.
 */
public class HocTapTinChiPanel extends JPanel {
    private static final int TONG_TC_MAC_DINH = 183;

    private final TaiKhoan taiKhoan;
    private final TinChiService service = new TinChiService();

    private JLabel lblNamThu, lblNamNhap, lblTrangThai;
    private JLabel lblTongCT, lblDaHoc, lblTichLuy, lblConThieu;
    private JLabel lblBiRut, lblDangKy, lblHieuLuc;
    private JLabel lblTB10, lblTB4, lblTBTL10, lblTBTL4;
    private JProgressBar barTienDo;
    private DefaultTableModel modelNam;
    private JTable tableNam;
    private List<TinChiTheoNam> lichSuNam;

    public HocTapTinChiPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        add(buildBanner(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildTheTienDo());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildTheTinChiChinh());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildTheTinChiPhu());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildTheDiem());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildBangTheoNam());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
    }

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 88));
        JLabel title = new JLabel("Tiến độ học tập & Tín chỉ");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Tổng CT 183 TC · Tín chỉ tích lũy · Điểm TB hệ 10 / hệ 4");
        sub.setFont(UITheme.FONT_BASE);
        sub.setForeground(new Color(255, 255, 255, 210));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(sub);
        banner.add(text, BorderLayout.WEST);
        return banner;
    }

    private JPanel buildTheTienDo() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        JPanel c1 = UITheme.statCard("Năm học thứ", "—", UITheme.TINT_BLUE, UITheme.TEXT_BLUE);
        JPanel c2 = UITheme.statCard("Năm nhập học", "—", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET);
        JPanel c3 = UITheme.statCard("Trạng thái", "—", UITheme.TINT_GREEN, UITheme.TEXT_GREEN);
        row.add(c1); row.add(c2); row.add(c3);
        lblNamThu = findGiaTri(c1);
        lblNamNhap = findGiaTri(c2);
        lblTrangThai = findGiaTri(c3);
        return row;
    }

    private JPanel buildTheTinChiChinh() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.add(UITheme.sectionLabel("Tín chỉ chương trình"), BorderLayout.NORTH);
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        JPanel a = UITheme.statCard("Tổng TC chương trình", String.valueOf(TONG_TC_MAC_DINH),
                new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8));
        JPanel b = UITheme.statCard("TC đã học", "0",
                new Color(0xF0, 0xFD, 0xFA), new Color(0x0F, 0x76, 0x6E));
        JPanel c = UITheme.statCard("TC đã tích lũy", "0",
                new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69));
        JPanel d = UITheme.statCard("Còn thiếu", "0",
                new Color(0xFF, 0xF7, 0xED), new Color(0xEA, 0x58, 0x0C));
        row.add(a); row.add(b); row.add(c); row.add(d);
        lblTongCT = findGiaTri(a);
        lblDaHoc = findGiaTri(b);
        lblTichLuy = findGiaTri(c);
        lblConThieu = findGiaTri(d);

        barTienDo = new JProgressBar(0, TONG_TC_MAC_DINH);
        barTienDo.setStringPainted(true);
        barTienDo.setString("0 / " + TONG_TC_MAC_DINH);
        barTienDo.setFont(UITheme.FONT_BOLD);
        barTienDo.setForeground(new Color(0x05, 0x96, 0x69));
        barTienDo.setBackground(new Color(0xE5, 0xE7, 0xEB));
        barTienDo.setPreferredSize(new Dimension(10, 20));
        barTienDo.setBorderPainted(false);

        JPanel mid = new JPanel(new BorderLayout(0, 8));
        mid.setOpaque(false);
        mid.add(row, BorderLayout.CENTER);
        mid.add(barTienDo, BorderLayout.SOUTH);
        card.add(mid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTheTinChiPhu() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        JPanel a = UITheme.statCard("TC bị rút", "0", new Color(0xFE, 0xF2, 0xF2), new Color(0xDC, 0x26, 0x26));
        JPanel b = UITheme.statCard("Đang đăng ký", "0", new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8));
        JPanel c = UITheme.statCard("TC có hiệu lực", "0", new Color(0xF5, 0xF3, 0xFF), new Color(0x7C, 0x3A, 0xED));
        row.add(a); row.add(b); row.add(c);
        lblBiRut = findGiaTri(a);
        lblDangKy = findGiaTri(b);
        lblHieuLuc = findGiaTri(c);
        return row;
    }

    private JPanel buildTheDiem() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.add(UITheme.sectionLabel("Điểm trung bình"), BorderLayout.NORTH);
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        JPanel a = UITheme.statCard("ĐTB hệ 10", "—", new Color(0xEF, 0xF6, 0xFF), new Color(0x25, 0x63, 0xEB));
        JPanel b = UITheme.statCard("ĐTB hệ 4", "—", new Color(0xF5, 0xF3, 0xFF), new Color(0x7C, 0x3A, 0xED));
        JPanel c = UITheme.statCard("ĐTB tích lũy hệ 10", "—", new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69));
        JPanel d = UITheme.statCard("ĐTB tích lũy hệ 4", "—", new Color(0xFE, 0xF3, 0xC7), new Color(0xD9, 0x77, 0x06));
        row.add(a); row.add(b); row.add(c); row.add(d);
        lblTB10 = findGiaTri(a);
        lblTB4 = findGiaTri(b);
        lblTBTL10 = findGiaTri(c);
        lblTBTL4 = findGiaTri(d);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JLabel findGiaTri(JPanel the) {
        for (Component comp : the.getComponents()) {
            if (comp instanceof JLabel && "giaTri".equals(comp.getName())) return (JLabel) comp;
        }
        JLabel found = null;
        for (Component comp : the.getComponents()) {
            if (comp instanceof JLabel lbl) {
                if (found == null || lbl.getFont().getSize() > found.getFont().getSize()) found = lbl;
            }
        }
        return found != null ? found : new JLabel("—");
    }

    private JPanel buildBangTheoNam() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        JLabel tip = UITheme.sectionLabel("Chi tiết theo năm  ·  Double-click 1 dòng để xem điểm năm đó");
        card.add(tip, BorderLayout.NORTH);

        modelNam = new DefaultTableModel(
                new String[]{"Năm học", "Tín chỉ đạt", "Tín chỉ rút", "ĐTB hệ 10", "ĐTB hệ 4", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableNam = new JTable(modelNam);
        tableNam.setRowHeight(36);
        tableNam.setFont(UITheme.FONT_BASE);
        tableNam.getTableHeader().setFont(UITheme.FONT_BOLD);
        tableNam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableNam.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = tableNam.getSelectedRow();
                    if (r >= 0 && lichSuNam != null && r < lichSuNam.size()) {
                        hienChiTietNam(lichSuNam.get(r));
                    }
                }
            }
        });
        JScrollPane sp = new JScrollPane(tableNam);
        sp.setPreferredSize(new Dimension(10, 220));
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

       private void hienChiTietNam(TinChiTheoNam n) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChiTietNamHocDialog dlg = new ChiTietNamHocDialog(owner, n);
        dlg.setVisible(true);
    }

    private static String fmtDiem(TinChiTheoNam n) {
        try { return fmt(n.getDiemTB10()); } catch (Throwable t) { return "—"; }
    }

    private static String fmtDiem4(TinChiTheoNam n) {
        try { return fmt(n.getDiemTB4()); } catch (Throwable t) { return "—"; }
    }

    /**
     * Dialog chi tiết 1 năm học — đồng bộ Modern Sky.
     */
    /**
     * Dialog chi tiết năm học — phong cách hệ thống quản lý đại học.
     */
    private static class ChiTietNamHocDialog extends JDialog {
        ChiTietNamHocDialog(Window owner, TinChiTheoNam n) {
            super(owner, "Chi tiết năm học " + n.getNamHoc(), ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(480, 460);
            setLocationRelativeTo(owner);
            setResizable(false);

            String ghiChu = (n.getGhiChu() != null && !n.getGhiChu().isBlank())
                    ? n.getGhiChu() : "—";

            JPanel root = new JPanel(new BorderLayout(0, 0));
            root.setBackground(UITheme.BG_MAIN != null ? UITheme.BG_MAIN : new Color(0xF8, 0xFA, 0xFC));

            // HEADER
            JPanel header = new JPanel(new BorderLayout(14, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color a = UITheme.PRIMARY != null ? UITheme.PRIMARY : new Color(0x1D, 0x4E, 0xD8);
                    Color b = UITheme.PRIMARY_DARK != null ? UITheme.PRIMARY_DARK : new Color(0x1E, 0x3A, 0x8A);
                    g2.setPaint(new GradientPaint(0, 0, a, getWidth(), 0, b));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(20, 24, 20, 24));
            header.setPreferredSize(new Dimension(10, 96));

            JPanel badge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.setColor(new Color(255, 255, 255, 90));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            badge.setPreferredSize(new Dimension(56, 56));
            badge.setLayout(new GridBagLayout());
            String yy = String.valueOf(n.getNamHoc());
            if (yy.length() >= 2) yy = yy.substring(yy.length() - 2);
            JLabel ic = new JLabel(yy);
            ic.setFont(new Font("Segoe UI", Font.BOLD, 18));
            ic.setForeground(Color.WHITE);
            badge.add(ic);

            JPanel titles = new JPanel();
            titles.setOpaque(false);
            titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
            JLabel t1 = new JLabel("Năm học " + n.getNamHoc());
            t1.setFont(new Font("Segoe UI", Font.BOLD, 22));
            t1.setForeground(Color.WHITE);
            JLabel t2 = new JLabel(ghiChu);
            t2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            t2.setForeground(new Color(255, 255, 255, 220));
            titles.add(t1);
            titles.add(Box.createRigidArea(new Dimension(0, 6)));
            titles.add(t2);
            header.add(badge, BorderLayout.WEST);
            header.add(titles, BorderLayout.CENTER);
            root.add(header, BorderLayout.NORTH);

            // BODY
            JPanel body = new JPanel();
            body.setOpaque(false);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(new EmptyBorder(20, 22, 8, 22));

            body.add(sectionLabel("TÍN CHỈ"));
            JPanel rowTc = new JPanel(new GridLayout(1, 2, 12, 0));
            rowTc.setOpaque(false);
            rowTc.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowTc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
            rowTc.add(uniCard("Tín chỉ đạt", String.valueOf(n.getTinChiDat()),
                    new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69)));
            rowTc.add(uniCard("Tín chỉ rút", String.valueOf(n.getTinChiRut()),
                    new Color(0xFE, 0xF2, 0xF2), new Color(0xDC, 0x26, 0x26)));
            body.add(rowTc);
            body.add(Box.createRigidArea(new Dimension(0, 16)));

            body.add(sectionLabel("ĐIỂM TRUNG BÌNH"));
            JPanel rowDiem = new JPanel(new GridLayout(1, 2, 12, 0));
            rowDiem.setOpaque(false);
            rowDiem.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowDiem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
            rowDiem.add(uniCard("Hệ 10", fmtDiem(n),
                    new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8)));
            rowDiem.add(uniCard("Hệ 4", fmtDiem4(n),
                    new Color(0xF5, 0xF3, 0xFF), new Color(0x7C, 0x3A, 0xED)));
            body.add(rowDiem);
            body.add(Box.createRigidArea(new Dimension(0, 16)));

            JPanel noteCard = new JPanel(new BorderLayout(0, 6));
            noteCard.setOpaque(true);
            noteCard.setBackground(UITheme.BG_CARD != null ? UITheme.BG_CARD : Color.WHITE);
            noteCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            noteCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            UITheme.BORDER != null ? UITheme.BORDER : new Color(0xE2, 0xE8, 0xF0), 1, true),
                    new EmptyBorder(12, 14, 12, 14)));
            noteCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
            JLabel nl = new JLabel("GHI CHÚ");
            nl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            nl.setForeground(UITheme.TEXT_MUTED);
            JLabel nv = new JLabel(ghiChu);
            nv.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nv.setForeground(UITheme.TEXT_PRIMARY);
            noteCard.add(nl, BorderLayout.NORTH);
            noteCard.add(nv, BorderLayout.CENTER);
            body.add(noteCard);
            root.add(body, BorderLayout.CENTER);

            // FOOTER
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            foot.setOpaque(true);
            foot.setBackground(UITheme.BG_MAIN != null ? UITheme.BG_MAIN : new Color(0xF8, 0xFA, 0xFC));
            foot.setBorder(new EmptyBorder(8, 22, 16, 22));
            JButton btn = UITheme.primaryButton("Đóng");
            btn.setPreferredSize(new Dimension(128, 42));
            btn.addActionListener(e -> dispose());
            foot.add(btn);
            root.add(foot, BorderLayout.SOUTH);

            setContentPane(root);
            getRootPane().setDefaultButton(btn);
        }

        private static JLabel sectionLabel(String text) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l.setForeground(UITheme.TEXT_MUTED);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            l.setBorder(new EmptyBorder(0, 2, 8, 0));
            return l;
        }

        private static JPanel uniCard(String nhan, String giaTri, Color nen, Color chu) {
            JPanel the = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(nen);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g2.setColor(chu);
                    g2.fillRoundRect(0, 4, 4, getHeight() - 9, 4, 4);
                    g2.setColor(new Color(chu.getRed(), chu.getGreen(), chu.getBlue(), 35));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                    g2.dispose();
                }
            };
            the.setOpaque(false);
            the.setLayout(new BorderLayout());
            the.setBorder(new EmptyBorder(14, 18, 14, 14));
            JLabel l1 = new JLabel(nhan);
            l1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l1.setForeground(chu);
            JLabel l2 = new JLabel(giaTri);
            l2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            l2.setForeground(chu);
            JPanel col = new JPanel();
            col.setOpaque(false);
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
            l1.setAlignmentX(Component.LEFT_ALIGNMENT);
            l2.setAlignmentX(Component.LEFT_ALIGNMENT);
            col.add(l1);
            col.add(Box.createRigidArea(new Dimension(0, 6)));
            col.add(l2);
            the.add(col, BorderLayout.CENTER);
            return the;
        }
    }
    private static JLabel boldLbl(String s) {
        JLabel l = new JLabel(s);
        l.setFont(UITheme.FONT_BOLD);
        return l;
    }

    private void taiDuLieu() {
        String maSV = taiKhoan.getMaSV() != null && !taiKhoan.getMaSV().isBlank()
                ? taiKhoan.getMaSV() : taiKhoan.getTenDangNhap();
        if (maSV != null && maSV.contains("@")) {
            maSV = maSV.substring(0, maSV.indexOf('@'));
        }
        final String ma = maSV;
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{service.layTienDo(ma), service.layLichSuNam(ma)};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] kq = get();
                    TinChiSinhVien tc = (TinChiSinhVien) kq[0];
                    lichSuNam = (List<TinChiTheoNam>) kq[1];
                    if (tc == null) {
                        UIUtils.thongBaoLoi(HocTapTinChiPanel.this,
                                "Không tìm thấy hồ sơ tín chỉ. Kiểm tra mã SV liên kết tài khoản.");
                        return;
                    }
                    setTxt(lblNamThu, tc.getNamThu() != null ? ("Năm " + tc.getNamThu()) : "—");
                    setTxt(lblNamNhap, tc.getNamNhapHoc() != null ? String.valueOf(tc.getNamNhapHoc()) : "—");
                    setTxt(lblTrangThai, nhanTT(tc.getTrangThaiHoc()));

                    int tong = tc.getTongTcChuongTrinh() > 0 ? tc.getTongTcChuongTrinh() : TONG_TC_MAC_DINH;
                    int tichLuy = tc.getTinChiTichLuy();
                    int daHoc = tc.getTinChiDaHoc() > 0 ? tc.getTinChiDaHoc() : tichLuy;
                    int conThieu = Math.max(0, tong - tichLuy);
                    int pct = tong > 0 ? (int) Math.round(tichLuy * 100.0 / tong) : 0;

                    setTxt(lblTongCT, String.valueOf(tong));
                    setTxt(lblDaHoc, String.valueOf(daHoc));
                    setTxt(lblTichLuy, String.valueOf(tichLuy));
                    setTxt(lblConThieu, String.valueOf(conThieu));
                    setTxt(lblBiRut, String.valueOf(tc.getTinChiBiRut()));
                    setTxt(lblDangKy, String.valueOf(tc.getTinChiDangKy()));
                    setTxt(lblHieuLuc, String.valueOf(tc.getTinChiConHieuLuc()));

                    setTxt(lblTB10, fmt(tc.getDiemTB10()));
                    setTxt(lblTB4, fmt(tc.getDiemTB4()));
                    setTxt(lblTBTL10, fmt(tc.getDiemTBTichLuy10()));
                    setTxt(lblTBTL4, fmt(tc.getDiemTBTichLuy4()));

                    if (barTienDo != null) {
                        barTienDo.setMaximum(tong);
                        barTienDo.setValue(Math.min(tichLuy, tong));
                        barTienDo.setString(tichLuy + " / " + tong + " TC (" + pct + "%)");
                    }

                    modelNam.setRowCount(0);
                    if (lichSuNam != null) {
                        for (TinChiTheoNam n : lichSuNam) {
                            modelNam.addRow(new Object[]{
                                    n.getNamHoc(), n.getTinChiDat(), n.getTinChiRut(),
                                    fmt(n.getDiemTB10()), fmt(n.getDiemTB4()),
                                    n.getGhiChu() != null ? n.getGhiChu() : ""
                            });
                        }
                    }
                    if (modelNam.getRowCount() == 0) {
                        modelNam.addRow(new Object[]{"—", "—", "—", "—", "—", "Chưa có lịch sử theo năm"});
                    }
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HocTapTinChiPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private static void setTxt(JLabel l, String s) {
        if (l != null) l.setText(s);
    }

    private static String fmt(double d) {
        if (d <= 0) return "—";
        return String.format(Locale.US, "%.2f", d);
    }

    private static String nhanTT(String tt) {
        if (tt == null) return "—";
        return switch (tt) {
            case "DANG_HOC" -> "Đang học";
            case "RA_TRUONG" -> "Ra trường";
            case "THOI_HOC" -> "Thôi học";
            default -> tt;
        };
    }
}