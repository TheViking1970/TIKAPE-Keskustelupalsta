package tikape.runko.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private String databaseAddress;

    public Database(String databaseAddress) throws ClassNotFoundException {
        this.databaseAddress = databaseAddress;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseAddress);
    }

    public void init() {
        List<String> lauseet = null;
        
        if(this.databaseAddress.contains("postgres")) {
            lauseet = postgreLauseet();
        }
        else {
            lauseet = sqliteLauseet();
        }

        // "try with resources" sulkee resurssin automaattisesti lopuksi
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();

            // suoritetaan komennot
            for (String lause : lauseet) {
                System.out.println("Running command >> " + lause);
                st.executeUpdate(lause);
            }

        } catch (Throwable t) {
            // jos tietokantataulu on jo olemassa, ei komentoja suoriteta
            System.out.println("Error >> " + t.getMessage());
        }
    }

    private List<String> sqliteLauseet() {
        ArrayList<String> lista = new ArrayList<>();
        lista.add("CREATE TABLE Alueet(id INTEGER PRIMARY KEY AUTOINCREMENT, nimi VARCHAR(255), lkm INTEGER, pvm INTEGER;");
        lista.add("CREATE TABLE Ketjut(id INTEGER PRIMARY KEY AUTOINCREMENT, otsikko VARCHAR(100), lkm INTEGER, pvm INTEGER, alue INTEGER, FOREIGN KEY(alue) REFERENCES Alueet(id));");
        lista.add("CREATE TABLE Viestit(id INTEGER PRIMARY KEY AUTOINCREMENT, nimi VARCHAR(100), viesti VARCHAR(4000), ketju INTEGER, alue INTEGER, pvm INTEGER, FOREIGN KEY(ketju) REFERENCES Ketjut(id), FOREIGN KEY (alue) REFERENCES Alueet(id));");
        return lista;
    }

    private List<String> postgreLauseet() {
        ArrayList<String> lista = new ArrayList<>();
        lista.add("CREATE TABLE Alueet(id SERIAL PRIMARY KEY, nimi VARCHAR(255), lkm INTEGER, pvm INTEGER);");
        lista.add("CREATE TABLE Ketjut(id SERIAL PRIMARY KEY, otsikko VARCHAR(100), lkm INTEGER, pvm INTEGER, alue INTEGER, FOREIGN KEY(alue) REFERENCES Alueet(id));");
        lista.add("CREATE TABLE Viestit(id SERIAL PRIMARY KEY, nimi VARCHAR(100), viesti VARCHAR(4000), ketju INTEGER, alue INTEGER, pvm INTEGER, FOREIGN KEY(ketju) REFERENCES Ketjut(id), FOREIGN KEY (alue) REFERENCES Alueet(id));");
        return lista;
    }
}
