package vn.edu.eaut.qlhocphi.gui.dashboard;

import vn.edu.eaut.qlhocphi.bus.KeToanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * "Bang dieu khien Ke toan" - man hinh trang chu RIENG cho tai khoan vai tro KETOAN,
 * thay the trang Tong Quan chung voi Admin. Tap trung dung vao cong viec hang ngay
 * cua ke toan hoc phi: thu duoc bao nhieu hom nay, con bao nhieu sinh vien no qua han
 * can nhac, va danh sach giao dich gan day de doi soat nhanh - thay vi thong ke
 * tong quan he thong chung chung nhu ban Admin.
 *
 * Cac man hinh nghiep vu con lai (Sinh vien, Hoc ky, Hoa don, Thanh toan, Cong no,
 * Bao cao) van dung chung code voi Admin nhu truoc - chi rieng trang "Tong quan" nay
 * la khac nhau giua 2 vai tro, vi day la 2 nhu cau thong tin dau tien khac nhau ro ret.
 */
public class KeToanDashboardPanel extends JPanel {
    private static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final KeToanService keToanService = new KeToanService();

    private final JPanel theThongKeBox = new JPanel(new GridLayout(1, 4, 16, 0));
    private GiaoDichTableModel giaoDichModel;
    private QuaHanTableModel quaHanModel;
    private JLabel lblSoLuongQuaHan;

    public KeToanDashboardPanel(TaiKhoan taiKhoan, Consumer<String> dieuHuong) {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        add(buildBanner(taiKhoan), BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);

        JPanel dauGiua = new JPanel();
        dauGiua.setOpaque(false);
        dauGiua.setLayout(new BoxLayout(dauGiua, BoxLayout.Y_AXIS));

        theThongKeBox.setOpaque(false);
        theThongKeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        theThongKeBox.add(UITheme.statCard("Thu hôm nay", "…", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        theThongKeBox.add(UITheme.statCard("Giao dịch hôm nay", "…", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        theThongKeBox.add(UITheme.statCard("Tổng còn nợ", "…", UITheme.TINT_RED, UITheme.TEXT_RED));
        theThongKeBox.add(UITheme.statCard("Hóa đơn quá hạn", "…", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        dauGiua.add(theThongKeBox);
        dauGiua.add(Box.createRigidArea(new Dimension(0, 16)));
        dauGiua.add(buildQuickActions(dieuHuong));

        giua.add(dauGiua, BorderLayout.NORTH);
        giua.add(buildHaiCot(dieuHuong), BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        taiDuLieu();
        // Làm mới mỗi 10 giây khi đang xem Tổng quan (KPI + 2 bảng)
        AutoRefreshTimer.gan(this, 10, this::taiDuLieu);

        // Quay lại tab Tổng quan → đọc DB ngay, không đợi timer
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                taiDuLieu();
            }
        });
    }

    /** Gọi từ ngoài nếu cần ép làm mới sau khi thu tiền. */
    public void lamMoi() {
        taiDuLieu();
    }

    // ================== Banner ==================

    private JPanel buildBanner(TaiKhoan taiKhoan) {
        JPanel banner = UITheme.gradientBanner();
        banner.setPreferredSize(new Dimension(0, 120));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));
        banner.setLayout(new BorderLayout());

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));

        JLabel loiChao = new JLabel("Bảng điều khiển Kế toán");
        loiChao.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loiChao.setForeground(Color.WHITE);

        Locale locVi = new Locale.Builder().setLanguage("vi").build();
        String ngayText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", locVi));
        ngayText = Character.toUpperCase(ngayText.charAt(0)) + ngayText.substring(1);
        JLabel phu = new JLabel("Xin chào " + taiKhoan.getHoTen() + " · " + ngayText);
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(0xDB, 0xE6, 0xFF));

        trai.add(loiChao);
        trai.add(Box.createRigidArea(new Dimension(0, 6)));
        trai.add(phu);
        banner.add(trai, BorderLayout.WEST);
        return banner;
    }

    // ================== Thao tac nhanh ==================

    private JPanel buildQuickActions(Consumer<String> dieuHuong) {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        JButton the1 = UITheme.quickActionCard("\uD83D\uDCB3", "Ghi nhận", "Thanh toán", UITheme.SUCCESS);
        JButton the2 = UITheme.quickActionCard("\u26A0", "Theo dõi", "Công nợ", UITheme.DANGER);
        JButton the3 = UITheme.quickActionCard("\uD83D\uDCC4", "Quản lý", "Hóa đơn học phí", UITheme.PRIMARY);
        JButton the4 = UITheme.quickActionCard("\uD83D\uDCCA", "Xem", "Thống kê & Báo cáo", UITheme.WARNING);

        the1.addActionListener(e -> dieuHuong.accept("thanhtoan"));
        the2.addActionListener(e -> dieuHuong.accept("congno"));
        the3.addActionListener(e -> dieuHuong.accept("hocphi"));
        the4.addActionListener(e -> dieuHuong.accept("baocao"));

        row.add(the1);
        row.add(the2);
        row.add(the3);
        row.add(the4);
        return row;
    }

    // ================== 2 cot: Giao dich gan day | SV qua han ==================

    private JPanel buildHaiCot(Consumer<String> dieuHuong) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.add(buildCotGiaoDich(dieuHuong));
        row.add(buildCotQuaHan(dieuHuong));
        return row;
    }

    private JPanel buildCotGiaoDich(Consumer<String> dieuHuong) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel dauMuc = new JPanel(new BorderLayout());
        dauMuc.setOpaque(false);
        JLabel tieuDe = new JLabel("Giao dịch gần đây");
        tieuDe.setFont(UITheme.FONT_BOLD);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dauMuc.add(tieuDe, BorderLayout.WEST);
        JButton xemTatCa = lienKetNho("Xem tất cả →");
        xemTatCa.addActionListener(e -> dieuHuong.accept("thanhtoan"));
        dauMuc.add(xemTatCa, BorderLayout.EAST);
        card.add(dauMuc, BorderLayout.NORTH);

        giaoDichModel = new GiaoDichTableModel();
        JTable table = new JTable(giaoDichModel);
        UIUtils.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setCellRenderer(ngayRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(stripedRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(tienRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCotQuaHan(Consumer<String> dieuHuong) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel dauMuc = new JPanel(new BorderLayout());
        dauMuc.setOpaque(false);
        JLabel tieuDe = new JLabel("Sinh viên nợ quá hạn nhiều nhất");
        tieuDe.setFont(UITheme.FONT_BOLD);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dauMuc.add(tieuDe, BorderLayout.WEST);
        JButton xemTatCa = lienKetNho("Xem tất cả →");
        xemTatCa.addActionListener(e -> dieuHuong.accept("congno"));
        dauMuc.add(xemTatCa, BorderLayout.EAST);
        card.add(dauMuc, BorderLayout.NORTH);

        quaHanModel = new QuaHanTableModel();
        JTable table = new JTable(quaHanModel);
        UIUtils.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setCellRenderer(stripedRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(ngayRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(conNoRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        lblSoLuongQuaHan = new JLabel(" ");
        lblSoLuongQuaHan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSoLuongQuaHan.setForeground(UITheme.TEXT_MUTED);
        card.add(lblSoLuongQuaHan, BorderLayout.SOUTH);
        return card;
    }

    private JButton lienKetNho(String text) {
        JButton b = new JButton(text);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setForeground(UITheme.PRIMARY);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ================== Renderer dung chung ==================

    private TableCellRenderer stripedRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
    }

    private TableCellRenderer ngayRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                String text;
                if (value instanceof LocalDateTime) text = ((LocalDateTime) value).format(DMY_HM);
                else if (value instanceof LocalDate) text = ((LocalDate) value).format(DMY);
                else text = "-";
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, text, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(UITheme.TEXT_MUTED);
                }
                l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return l;
            }
        };
    }

    private TableCellRenderer tienRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                String text = value instanceof BigDecimal ? MoneyUtils.format((BigDecimal) value) : "-";
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, text, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(UITheme.FONT_BOLD);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(UITheme.TEXT_GREEN);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 10));
                return l;
            }
        };
    }

    private TableCellRenderer conNoRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                String text = value instanceof BigDecimal ? MoneyUtils.format((BigDecimal) value) : "-";
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, text, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(UITheme.FONT_BOLD);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(UITheme.DANGER);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 10));
                return l;
            }
        };
    }

    // ================== Tai du lieu ==================

    private void taiDuLieu() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                KeToanService.SoLieuHomNay soLieu = keToanService.laySoLieuHomNay();
                List<PhieuThu> giaoDich = keToanService.layGiaoDichGanDay(8);
                List<HoaDonHocPhi> quaHan = keToanService.topSinhVienQuaHan(8);
                return new Object[]{soLieu, giaoDich, quaHan};
            }

            @Override
            protected void done() {
                try {
                    Object[] ket = get();
                    KeToanService.SoLieuHomNay soLieu = (KeToanService.SoLieuHomNay) ket[0];
                    @SuppressWarnings("unchecked")
                    List<PhieuThu> giaoDich = (List<PhieuThu>) ket[1];
                    @SuppressWarnings("unchecked")
                    List<HoaDonHocPhi> quaHan = (List<HoaDonHocPhi>) ket[2];

                    capNhatGiaTriThe(0, MoneyUtils.format(soLieu.tongThuHomNay));
                    capNhatGiaTriThe(1, soLieu.soGiaoDichHomNay + " giao dịch");
                    capNhatGiaTriThe(2, MoneyUtils.format(soLieu.tongConNoToanTruong));
                    capNhatGiaTriThe(3, soLieu.soHoaDonQuaHan + " hóa đơn");

                    giaoDichModel.capNhat(giaoDich);
                    quaHanModel.capNhat(quaHan);
                    lblSoLuongQuaHan.setText("  Tổng cộng " + soLieu.soHoaDonQuaHan + " hóa đơn đang quá hạn toàn trường");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(KeToanDashboardPanel.this,
                            "Không thể tải dữ liệu bảng điều khiển.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void capNhatGiaTriThe(int index, String giaTriMoi) {
        JPanel the = (JPanel) theThongKeBox.getComponent(index);
        JLabel lbl = timNhanTheoTen(the, "giaTri");
        if (lbl != null) lbl.setText(giaTriMoi);
    }

    private JLabel timNhanTheoTen(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (name.equals(c.getName()) && c instanceof JLabel) return (JLabel) c;
            if (c instanceof Container) {
                JLabel ket = timNhanTheoTen((Container) c, name);
                if (ket != null) return ket;
            }
        }
        return null;
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }

    // ================== Table models ==================

    private static class GiaoDichTableModel extends AbstractTableModel {
        private final String[] cot = {"Ngày", "Sinh viên", "Số tiền"};
        private List<PhieuThu> ds = new ArrayList<>();

        void capNhat(List<PhieuThu> dsMoi) { this.ds = dsMoi; fireTableDataChanged(); }

        @Override public int getRowCount() { return ds.size(); }
        @Override public int getColumnCount() { return cot.length; }
        @Override public String getColumnName(int c) { return cot[c]; }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == 0) return LocalDateTime.class;
            if (c == 2) return BigDecimal.class;
            return String.class;
        }

        @Override
        public Object getValueAt(int row, int col) {
            PhieuThu pt = ds.get(row);
            switch (col) {
                case 0: return pt.getNgayNop();
                case 1: return pt.getTenSV() != null ? pt.getTenSV() : "-";
                case 2: return pt.getSoTienNop();
                default: return null;
            }
        }
    }

    private static class QuaHanTableModel extends AbstractTableModel {
        private final String[] cot = {"Sinh viên", "Hạn nộp", "Còn nợ"};
        private List<HoaDonHocPhi> ds = new ArrayList<>();

        void capNhat(List<HoaDonHocPhi> dsMoi) { this.ds = dsMoi; fireTableDataChanged(); }

        @Override public int getRowCount() { return ds.size(); }
        @Override public int getColumnCount() { return cot.length; }
        @Override public String getColumnName(int c) { return cot[c]; }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == 1) return LocalDate.class;
            if (c == 2) return BigDecimal.class;
            return String.class;
        }

        @Override
        public Object getValueAt(int row, int col) {
            HoaDonHocPhi hd = ds.get(row);
            switch (col) {
                case 0: return hd.getTenSV() != null ? hd.getTenSV() : hd.getMaSV();
                case 1: return hd.getHanThanhToan();
                case 2: return hd.tinhConNo();
                default: return null;
            }
        }
    }
}