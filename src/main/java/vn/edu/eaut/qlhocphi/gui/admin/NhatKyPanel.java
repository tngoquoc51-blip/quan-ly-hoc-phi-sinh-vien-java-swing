package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.NhatKyHeThongService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.NhatKyHeThong;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Màn hình "Nhật ký hệ thống" - truy vết ai đã làm hành động gì, lúc nào.
 * Chỉ dành cho ADMIN. Đồng bộ giao diện với các trang quản trị khác (banner,
 * KPI, toolbar GridBagLayout, bảng có thể màu theo loại hành động).
 */
public class NhatKyPanel extends JPanel {
    private static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final NhatKyHeThongService nhatKyService = new NhatKyHeThongService();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtTimKiem;
    private JComboBox<String> cboHanhDong;
    private JLabel lblSoLuong;

    private JLabel lblTongHoatDong, lblSoNguoiDung, lblHomNay;

    public NhatKyPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildKpiRow());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildToolbar());
        add(north, BorderLayout.NORTH);

        add(buildTableCard(), BorderLayout.CENTER);

        taiDuLieu();
        AutoRefreshTimer.gan(this, 30, this::taiDuLieu);
    }

    private JPanel buildHeader() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(logoBadge(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Nhật ký hệ thống");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Truy vết mọi hành động thay đổi dữ liệu trong hệ thống");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setFont(UITheme.FONT_BOLD);
        btnLamMoi.setBackground(Color.WHITE);
        btnLamMoi.setForeground(UITheme.PRIMARY_DARK);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setBorderPainted(false);
        btnLamMoi.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLamMoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLamMoi.addActionListener(e -> taiDuLieu());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnLamMoi);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    private JComponent logoBadge() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83D\uDCDC";
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        lblTongHoatDong = new JLabel("0");
        lblSoNguoiDung = new JLabel("0");
        lblHomNay = new JLabel("0");
        row.add(thongKeCard("Tổng hoạt động (200 gần nhất)", lblTongHoatDong, UITheme.PRIMARY));
        row.add(thongKeCard("Số người dùng thực hiện", lblSoNguoiDung, UITheme.TEXT_VIOLET));
        row.add(thongKeCard("Hoạt động hôm nay", lblHomNay, UITheme.SUCCESS));
        return row;
    }

    private JPanel thongKeCard(String tieuDe, JLabel giaTri, Color mauNhan) {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel(tieuDe);
        l1.setFont(UITheme.FONT_BASE);
        l1.setForeground(UITheme.TEXT_MUTED);
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        giaTri.setFont(new Font("Segoe UI", Font.BOLD, 22));
        giaTri.setForeground(mauNhan);
        giaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l1);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(giaTri);
        return card;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        int col = 0;

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Tìm kiếm:"), gbc);

        txtTimKiem = UIUtils.textField(20);
        txtTimKiem.setToolTipText("Tìm theo người dùng, đối tượng hoặc chi tiết");
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void removeUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void changedUpdate(DocumentEvent e) { apDungBoLoc(); }
        });
        gbc.gridx = col++;
        toolbar.add(txtTimKiem, gbc);

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Hành động:"), gbc);

        cboHanhDong = new JComboBox<>(new String[]{
                "Tất cả hành động",
                "Đăng nhập",
                "Đăng xuất",
                "Thêm hóa đơn",
                "Thu tiền",
                "Sao lưu"
        });
        cboHanhDong.setFont(UITheme.FONT_BASE);
        cboHanhDong.addActionListener(e -> apDungBoLoc());
        gbc.gridx = col++;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(cboHanhDong, gbc);

        return toolbar;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        tableModel = new DefaultTableModel(
                new Object[]{"Thời gian", "Người dùng", "Hành động", "Đối tượng", "Chi tiết"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(2).setCellRenderer(hanhDongCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        lblSoLuong = new JLabel("Hiển thị 0 / 0 hoạt động");
        lblSoLuong.setFont(UITheme.FONT_BASE);
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        card.add(lblSoLuong, BorderLayout.SOUTH);

        return card;
    }

    private DefaultTableCellRenderer hanhDongCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                String text = value == null ? "" : value.toString();
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(UITheme.FONT_BOLD);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                Color bg, fg;
                switch (text) {
                    case "THÊM":
                    case "THEM":
                        bg = UITheme.TINT_GREEN; fg = UITheme.TEXT_GREEN; break;
                    case "SỬA":
                    case "SUA":
                        bg = UITheme.TINT_BLUE; fg = UITheme.TEXT_BLUE; break;
                    case "XÓA":
                    case "XOA":
                        bg = new Color(0xFC, 0xE4, 0xE4); fg = UITheme.DANGER; break;
                    case "THANH_TOÁN":
                    case "THANH_TOAN":
                        bg = UITheme.TINT_VIOLET; fg = UITheme.TEXT_VIOLET; break;
                    default:
                        bg = new Color(0xEC, 0xEE, 0xF2); fg = UITheme.TEXT_MUTED;
                }
                if (!isSelected) {
                    label.setBackground(bg);
                    label.setForeground(fg);
                }
                return label;
            }
        };
    }

    private void apDungBoLoc() {
        List<RowFilter<Object, Object>> danhSachLoc = new ArrayList<>();
        String tuKhoa = txtTimKiem.getText().trim();
        if (!tuKhoa.isEmpty()) {
            danhSachLoc.add(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa), 1, 3, 4));
        }

        String chon = (String) cboHanhDong.getSelectedItem();
        String maLoc = mapHanhDongSangMa(chon);
        if (maLoc != null) {
            danhSachLoc.add(RowFilter.regexFilter("^" + Pattern.quote(maLoc) + "$", 2));
        }

        sorter.setRowFilter(danhSachLoc.isEmpty() ? null : RowFilter.andFilter(danhSachLoc));
        capNhatSoLuongHienThi();
    }

    /** Nhãn trên combo → mã cột HanhDong trong DB. null = không lọc. */
    private static String mapHanhDongSangMa(String nhan) {
        if (nhan == null || nhan.startsWith("Tất cả")) return null;
        return switch (nhan) {
            case "Đăng nhập"    -> "DANG_NHAP";
            case "Đăng xuất"    -> "DANG_XUAT";
            case "Thêm hóa đơn" -> "THEM_HOA_DON";
            case "Thu tiền"     -> "THU_TIEN";
            case "Sao lưu"      -> "SAO_LUU";
            default             -> null;
        };
    }

    private void capNhatSoLuongHienThi() {
        lblSoLuong.setText("Hiển thị " + table.getRowCount() + " / " + tableModel.getRowCount() + " hoạt động");
    }

    private void taiDuLieu() {
        SwingWorker<List<NhatKyHeThong>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<NhatKyHeThong> doInBackground() throws Exception {
                return nhatKyService.layGanNhat();
            }

            @Override
            protected void done() {
                try {
                    List<NhatKyHeThong> list = get();
                    tableModel.setRowCount(0);
                    Map<String, Integer> theoNguoiDung = new TreeMap<>();
                    int homNay = 0;
                    java.time.LocalDate hienTai = java.time.LocalDate.now();
                    for (NhatKyHeThong nk : list) {
                        tableModel.addRow(new Object[]{
                                nk.getThoiGian() != null ? nk.getThoiGian().format(DMY_HM) : "",
                                nk.getTenDangNhap(), nk.getHanhDong(), nk.getDoiTuong(),
                                nk.getChiTiet() == null ? "" : nk.getChiTiet()
                        });
                        theoNguoiDung.merge(nk.getTenDangNhap(), 1, Integer::sum);
                        if (nk.getThoiGian() != null && nk.getThoiGian().toLocalDate().equals(hienTai)) homNay++;
                    }
                    lblTongHoatDong.setText(String.valueOf(list.size()));
                    lblSoNguoiDung.setText(String.valueOf(theoNguoiDung.size()));
                    lblHomNay.setText(String.valueOf(homNay));
                    apDungBoLoc();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(NhatKyPanel.this, "Không thể tải nhật ký hệ thống.");
                }
            }
        };
        worker.execute();
    }
}