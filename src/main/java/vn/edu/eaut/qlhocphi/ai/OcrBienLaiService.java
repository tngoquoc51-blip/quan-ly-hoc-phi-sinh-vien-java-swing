package vn.edu.eaut.qlhocphi.ai;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR bien lai/hoa don hoc phi bang giay: ke toan chup anh (hoac tai anh len),
 * AI (Gemini Vision - dung lai ChatbotClient da co san cho Chatbot) "doc" anh va
 * tra ve cac truong du lieu duoi dang JSON, roi duoc parse thanh KetQuaOcrBienLai
 * de dien san vao form "Ghi nhan thanh toan", giup ke toan gan nhu khong phai go tay.
 *
 * QUAN TRONG: day chi la GOI Y tu AI - GUI luon hien thi lai cho ke toan xem/sua
 * truoc khi ghi nhan, khong bao gio tu dong luu thang vao CSDL.
 */
public class OcrBienLaiService {

    private static final String SYSTEM_PROMPT =
            "Ban la mot cong cu OCR chuyen doc bien lai/hoa don hoc phi tieng Viet chup bang dien thoai. " +
                    "Chi tra loi DUY NHAT 1 doi tuong JSON hop le, KHONG kem giai thich, KHONG dung markdown code fence. " +
                    "Cac khoa bat buoc co trong JSON (dung null neu khong doc ro duoc, KHONG bia so lieu): " +
                    "maHoaDon (so nguyen neu tren bien lai co in 'Ma hoa don' hoac 'So HD', khong co thi null), " +
                    "maSV (chuoi ma sinh vien neu co in tren bien lai, thuong dang chu+so vi du 20230101), " +
                    "hoTen (ho ten sinh vien/nguoi nop tien), " +
                    "soTien (so nguyen, don vi VND, KHONG dau cham/phay, vi du bien lai ghi '2.500.000 d' thi tra ve 2500000), " +
                    "ngay (dang dd/MM/yyyy neu doc duoc ngay tren bien lai, khong co thi null), " +
                    "hinhThuc ('TIEN_MAT' hoac 'CHUYEN_KHOAN' neu doan duoc tu noi dung bien lai, khong ro thi null), " +
                    "ghiChu (noi dung/dien giai khac tren bien lai neu co, khong co thi null).";

    /** Doc 1 anh chup bien lai va tra ve cac truong da trich xuat. Nem loi neu goi AI that bai (mat mang, chua cau hinh key...). */
    public KetQuaOcrBienLai docBienLai(BufferedImage anh) throws Exception {
        String base64 = maHoaAnh(anh);
        String phanHoi = vn.edu.eaut.qlhocphi.ai.ChatbotClient.goiVoiAnh(
                SYSTEM_PROMPT,
                "Day la anh chup 1 bien lai/hoa don hoc phi. Hay doc va tra ve JSON theo dung dinh dang da huong dan.",
                base64, "image/jpeg");
        return phanTichJson(phanHoi);
    }

    private String maHoaAnh(BufferedImage anh) throws Exception {
        // JPEG không hỗ trợ alpha (PNG trong suốt) → chuyển RGB + nền trắng
        BufferedImage rgb = anh;
        if (anh.getType() != BufferedImage.TYPE_INT_RGB) {
            rgb = new BufferedImage(anh.getWidth(), anh.getHeight(), BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = rgb.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, anh.getWidth(), anh.getHeight());
            g.drawImage(anh, 0, 0, null);
            g.dispose();
        }
        // Thu nhỏ nếu ảnh quá lớn (tránh Gemini 400)
        int maxCanh = 1600;
        if (rgb.getWidth() > maxCanh || rgb.getHeight() > maxCanh) {
            double scale = Math.min(
                    (double) maxCanh / rgb.getWidth(),
                    (double) maxCanh / rgb.getHeight());
            int w = Math.max(1, (int) (rgb.getWidth() * scale));
            int h = Math.max(1, (int) (rgb.getHeight() * scale));
            BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(rgb, 0, 0, w, h, null);
            g2.dispose();
            rgb = scaled;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(rgb, "jpg", baos) || baos.size() == 0) {
            throw new IllegalStateException(
                    "Không ghi được ảnh JPEG từ biên lai. Thử file JPG/PNG khác, rõ nét hơn.");
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /** Parse JSON tra ve tu Gemini thanh KetQuaOcrBienLai. Tu viet (khong dung thu vien JSON ngoai),
     *  cung phong cach voi ChatbotClient.trichXuatText() da co san trong du an. */
    private KetQuaOcrBienLai phanTichJson(String raw) {
        KetQuaOcrBienLai kq = new KetQuaOcrBienLai();
        if (raw == null || raw.isBlank()) {
            kq.setDocDuoc(false);
            kq.setCanhBao("AI khong tra ve du lieu, vui long nhap tay hoac thu lai anh ro net hon.");
            return kq;
        }
        // Go bo markdown code fence neu AI lo dinh kem (```json ... ```)
        String json = raw.trim();
        json = json.replaceAll("(?s)^```json", "").replaceAll("(?s)^```", "").replaceAll("(?s)```$", "").trim();

        try {
            String maHD = trichChuoiHoacSo(json, "maHoaDon");
            if (maHD != null) {
                try { kq.setMaHoaDon(Integer.parseInt(maHD.replaceAll("[^0-9]", ""))); }
                catch (NumberFormatException ignored) { /* giu null */ }
            }

            kq.setMaSV(trichChuoi(json, "maSV"));
            kq.setHoTen(trichChuoi(json, "hoTen"));

            String soTienStr = trichChuoiHoacSo(json, "soTien");
            if (soTienStr != null) {
                String soSach = soTienStr.replaceAll("[^0-9]", "");
                if (!soSach.isBlank()) {
                    try { kq.setSoTien(new BigDecimal(soSach)); }
                    catch (NumberFormatException ignored) { /* giu null */ }
                }
            }

            kq.setNgay(trichChuoi(json, "ngay"));
            String hinhThuc = trichChuoi(json, "hinhThuc");
            if ("TIEN_MAT".equalsIgnoreCase(hinhThuc) || "CHUYEN_KHOAN".equalsIgnoreCase(hinhThuc)) {
                kq.setHinhThuc(hinhThuc.toUpperCase());
            }
            kq.setGhiChu(trichChuoi(json, "ghiChu"));

            if (kq.getMaHoaDon() == null && kq.getSoTien() == null && kq.getMaSV() == null) {
                kq.setDocDuoc(false);
                kq.setCanhBao("Khong doc duoc thong tin nao ro rang tu anh nay. Vui long chup lai anh ro net hon hoac nhap tay.");
            } else if (kq.getMaHoaDon() == null) {
                kq.setCanhBao("Khong doc duoc So hoa don tren bien lai - vui long tra cuu theo Ma SV o tab Sinh vien, hoac nhap tay so hoa don.");
            }
        } catch (Exception ex) {
            kq.setDocDuoc(false);
            kq.setCanhBao("Loi doc du lieu AI tra ve: " + ex.getMessage());
        }
        return kq;
    }

    /** Trich gia tri dang chuoi cua 1 khoa JSON ("key": "value"). Tra ve null neu khoa khong co hoac gia tri la null. */
    private String trichChuoi(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            String v = m.group(1).replace("\\\"", "\"").replace("\\\\", "\\").trim();
            return v.isBlank() ? null : v;
        }
        return null;
    }

    /** Trich gia tri co the la chuoi ("123") HOAC so tho (123) HOAC null cua 1 khoa JSON. */
    private String trichChuoiHoacSo(String json, String key) {
        String vChuoi = trichChuoi(json, key);
        if (vChuoi != null) return vChuoi;
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9][0-9.,]*)");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }
}
