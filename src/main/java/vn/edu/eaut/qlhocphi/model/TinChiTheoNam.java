package vn.edu.eaut.qlhocphi.model;

/** Tín chỉ + điểm TB theo từng năm học. */
public class TinChiTheoNam {
    private int id;
    private String maSV;
    private int namHoc;
    private int tinChiDat;
    private int tinChiRut;
    private String ghiChu;
    private double diemTB10;
    private double diemTB4;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }
    public int getNamHoc() { return namHoc; }
    public void setNamHoc(int namHoc) { this.namHoc = namHoc; }
    public int getTinChiDat() { return tinChiDat; }
    public void setTinChiDat(int tinChiDat) { this.tinChiDat = tinChiDat; }
    public int getTinChiRut() { return tinChiRut; }
    public void setTinChiRut(int tinChiRut) { this.tinChiRut = tinChiRut; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public double getDiemTB10() { return diemTB10; }
    public void setDiemTB10(double diemTB10) { this.diemTB10 = diemTB10; }
    public double getDiemTB4() { return diemTB4; }
    public void setDiemTB4(double diemTB4) { this.diemTB4 = diemTB4; }
}