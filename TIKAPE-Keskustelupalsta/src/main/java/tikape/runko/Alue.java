package tikape.runko;

public class Alue {

    int id;
    String nimi;
    int lkm;
    String kello;
    String pvm;
    
    public Alue(int id, String nimi, int lkm, String kello, String pvm) {
        this.id = id;
        this.nimi = nimi;
        this.lkm = lkm;
        this.kello = kello;
        this.pvm = pvm;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getNimi() {
        return this.nimi;
    }
    
    public int getLkm() {
        return lkm;
    }
    
    public String getKello() {
        return kello;
    }
    
    public String getPvm() {
        return this.pvm;
    }
    
}
