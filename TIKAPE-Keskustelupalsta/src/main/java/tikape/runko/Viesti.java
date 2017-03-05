package tikape.runko;

public class Viesti {

    int id;
    String nimi;
    String viesti;
    String kello;
    String pvm;
    
    public Viesti(int id, String nimi, String viesti, String kello, String pvm) {
        this.id = id;
        this.nimi = nimi;
        this.viesti = viesti;
        this.kello = kello;
        this.pvm = pvm;
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getNimi() {
        return this.nimi;
    }
    
    public String getViesti() {
        return this.viesti;
    }
    
    public String getKello() {
        return this.kello;
    }

    public String getPvm() {
        return this.pvm;
    }
    
}
