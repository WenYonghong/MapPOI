package poi.map.com.mappoi.Model;

/**
 * Created by Administrator on 2018/3/25.
 */

public class PoiModel {

    private String addressName;
    private String city;
    private String province;
    private String cityCode;
    private String provinceCode;
    private String snippet;
    private double LocationX;
    private double LocationY;
    private boolean checked;

    public PoiModel(String addressName,String city, String province, String cityCode, String provinceCode, String snippet, double locationX, double locationY, boolean checked) {
        this.addressName = addressName;
        this.city = city;
        this.province = province;
        this.cityCode = cityCode;
        this.provinceCode = provinceCode;
        this.snippet = snippet;
        this.LocationX = locationX;
        this.LocationY = locationY;
        this.checked = checked;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public PoiModel() {
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public double getLocationX() {
        return LocationX;
    }

    public void setLocationX(double locationX) {
        LocationX = locationX;
    }

    public double getLocationY() {
        return LocationY;
    }

    public void setLocationY(double locationY) {
        LocationY = locationY;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
