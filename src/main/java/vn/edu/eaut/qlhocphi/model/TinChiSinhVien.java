package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

/** Tổng hợp tín chỉ & điểm của một sinh viên. */
public class TinChiSinhVien {
    private String maSV;
    private int tinChiTichLuy;
    private int tinChiBiRut;
    private int tinChiDangKy;
    private int tinChiDaHoc;
    private int tongTcChuongTrinh = 183;
    private double diemTB10;
    private double diemTB4;
    private double diemTBTichLuy10;
    private double diemTBTichLuy4;
    private LocalDateTime capNhatLuc;
    private String hoTen;
    private Integer namNhapHoc;
    private Integer namThu;
    private String trangThaiHoc;

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }
    public int getTinChiTichLuy() { return tinChiTichLuy; }
    public void setTinChiTichLuy(int tinChiTichLuy) { this.tinChiTichLuy = tinChiTichLuy; }
    public int getTinChiBiRut() { return tinChiBiRut; }
    public void setTinChiBiRut(int tinChiBiRut) { this.tinChiBiRut = tinChiBiRut; }
    public int getTinChiDangKy() { return tinChiDangKy; }
    public void setTinChiDangKy(int tinChiDangKy) { this.tinChiDangKy = tinChiDangKy; }
    public int getTinChiDaHoc() { return tinChiDaHoc; }
    public void setTinChiDaHoc(int tinChiDaHoc) { this.tinChiDaHoc = tinChiDaHoc; }
    public int getTongTcChuongTrinh() { return tongTcChuongTrinh; }
    public void setTongTcChuongTrinh(int tongTcChuongTrinh) { this.tongTcChuongTrinh = tongTcChuongTrinh; }
    public double getDiemTB10() { return diemTB10; }
    public void setDiemTB10(double diemTB10) { this.diemTB10 = diemTB10; }
    public double getDiemTB4() { return diemTB4; }
    public void setDiemTB4(double diemTB4) { this.diemTB4 = diemTB4; }
    public double getDiemTBTichLuy10() { return diemTBTichLuy10; }
    public void setDiemTBTichLuy10(double diemTBTichLuy10) { this.diemTBTichLuy10 = diemTBTichLuy10; }
    public double getDiemTBTichLuy4() { return diemTBTichLuy4; }
    public void setDiemTBTichLuy4(double diemTBTichLuy4) { this.diemTBTichLuy4 = diemTBTichLuy4; }
    public LocalDateTime getCapNhatLuc() { return capNhatLuc; }
    public void setCapNhatLuc(LocalDateTime capNhatLuc) { this.capNhatLuc = capNhatLuc; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public Integer getNamNhapHoc() { return namNhapHoc; }
    public void setNamNhapHoc(Integer namNhapHoc) { this.namNhapHoc = namNhapHoc; }
    public Integer getNamThu() { return namThu; }
    public void setNamThu(Integer namThu) { this.namThu = namThu; }
    public String getTrangThaiHoc() { return trangThaiHoc; }
    public void setTrangThaiHoc(String trangThaiHoc) { this.trangThaiHoc = trangThaiHoc; }

    public int getTinChiConHieuLuc() {
        return Math.max(0, tinChiTichLuy);
    }

    public int getTinChiConThieu() {
        return Math.max(0, tongTcChuongTrinh - tinChiTichLuy);
    }
}