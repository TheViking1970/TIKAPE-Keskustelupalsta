package tikape.runko;

public class Ketju {

    int id;
    String otsikko;
    int lkm;
    String kello;
    String pvm;
    
    public Ketju(int id, String otsikko, int lkm, String kello, String pvm) {
        this.id = id;
        this.otsikko = otsikko;
        this.lkm = lkm;
        this.kello = kello;
        this.pvm = pvm;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getOtsikko() {
        return this.otsikko;
    }
    
    public int getLkm() {
        return lkm;
    }
    
    public String getKello() {
        return this.kello;
    }
    
    public String getPvm() {
        return this.pvm;
    }
    
}
