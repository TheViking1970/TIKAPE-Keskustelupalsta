package tikape.runko;

public class Sivu {

    int sivu;
    String sivuStr;
    int tila;
    
    public Sivu(int sivu, String sivuStr, int tila) {
        this.sivu = sivu;
        this.sivuStr = sivuStr;
        this.tila = tila;
    }
    
    public int getSivu(){
        return sivu;
    }
    
    public String getSivuStr() {
        return sivuStr;
    }
    
    public int getTila() {
        return tila;
    }
    
}
