package tikape.runko;

public class Data {

    int alue;
    int ketju;
    String alueNimi;
    String ketjuNimi;
    
    public Data() {}
    
    public void setAlue(int alue) {
        this.alue = alue;
    }
    
    public int getAlue() {
        return alue;
    }

    public void setKetju(int ketju) {
        this.ketju = ketju;
    }
    
    public int getKetju() {
        return ketju;
    }
    
    public void setAlueNimi(String nimi) {
        this.alueNimi = nimi;
    }

    public String getAlueNimi() {
        return this.alueNimi;
    }

    public void setKetjuNimi(String nimi) {
        this.ketjuNimi = nimi;
    }

    public String getKetjuNimi() {
        return this.ketjuNimi;
    }

}
