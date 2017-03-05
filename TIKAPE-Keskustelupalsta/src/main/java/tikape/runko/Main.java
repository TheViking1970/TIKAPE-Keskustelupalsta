package tikape.runko;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Date;
import spark.ModelAndView;
import static spark.Spark.*;
import spark.template.thymeleaf.ThymeleafTemplateEngine;
import tikape.runko.database.Database;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import spark.Spark;

public class Main {

    static boolean sqlite;
    
    public static void main(String[] args) throws Exception {
        
        if(System.getenv("PORT") != null) {
            port(Integer.valueOf(System.getenv("PORT")));
        }
        
        sqlite = true;
        
        String jdbcOsoite = "jdbc:sqlite:keskustelupalsta.db";
        if(System.getenv("JDBC_DATABASE_URL")!= null) {
            // Heroku tietokanta
            jdbcOsoite = System.getenv("JDBC_DATABASE_URL");
            sqlite = false;
        }
        
        Database database = new Database(jdbcOsoite);
        database.init();
        Connection connection = database.getConnection();
        
        Spark.staticFileLocation("/public");
        
        // INDEX - Näytä ALUEET
        get("/", (req, res) -> {
            HashMap map = new HashMap<>();
            
            String sql = "SELECT id,nimi,lkm,pvm FROM Alueet";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            
            List<Alue> alueet = new ArrayList<>();
            Date d = new Date();
            while(result.next()) {
                String[] splits = getFormattedDate(result.getInt("pvm"));
                alueet.add(new Alue(
                        result.getInt("id"),
                        result.getString("nimi"),
                        result.getInt("lkm"),
                        splits[0],
                        splits[1]
                ));
            }
            
            map.put("alueet", alueet);

            return new ModelAndView(map, "index");
        }, new ThymeleafTemplateEngine());
        
        // ALUE - Näytä alueen KETJUT
        get("/alue/:id", (req, res) -> {
            HashMap map = new HashMap<>();
            
            int alue = Integer.parseInt(req.params(":id"));
            
            String sql = "SELECT id,otsikko,lkm,pvm FROM Ketjut WHERE alue=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, alue);
            ResultSet result = statement.executeQuery();

            List<Ketju> ketjut = new ArrayList<>();
            Date d = new Date();
            while (result.next()) {
                String[] splits = getFormattedDate(result.getInt("pvm"));
                ketjut.add(new Ketju(
                        result.getInt("id"),
                        result.getString("otsikko"),
                        result.getInt("lkm"),
                        splits[0],
                        splits[1]
                ));
            }
            
            sql = "SELECT nimi FROM Alueet WHERE id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, alue);
            result = statement.executeQuery();
            result.next();
            String nimi = result.getString("nimi");
            
            List<Data> data = new ArrayList<>();
            Data tmpData = new Data();
            tmpData.setAlue(alue);
            tmpData.setAlueNimi(nimi);
            
            map.put("ketjut", ketjut);
            map.put("data", tmpData);

            return new ModelAndView(map, "alue");
        }, new ThymeleafTemplateEngine());

        // UUSIALUE(post) - lisää uusi ALUE
        post("/uusialue", (req, res) -> {
            String nimi = req.queryParams("nimi");
            int error = 0;
            String sql = "SELECT id FROM Alueet WHERE nimi=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nimi);
            ResultSet result = statement.executeQuery();
            if(result.getFetchSize()==0) {
                System.out.println("Timestamp: "+getTimestamp());
                sql = "INSERT INTO Alueet (nimi,lkm,pvm) VALUES (?,0,?)";
                statement = connection.prepareStatement(sql);
                statement.setString(1, nimi);
                statement.setInt(2, getTimestamp());
                int insertedRows = statement.executeUpdate();
                if(insertedRows!=1) {
                    error = 2;
                }
            }
            else {
                error = 1;
            }
            if(error>0) {
                res.redirect("/error");
            }
            res.redirect("/");
            return "ok";
        });

        // KETJU - Näytä ketju
        get("/ketju/:alue/:ketju/:sivu", (req, res) -> {
            HashMap map = new HashMap<>();

            int perPage = 5;
            
            int alue = Integer.parseInt(req.params(":alue"));
            int ketju = Integer.parseInt(req.params(":ketju"));
            int sivu = Integer.parseInt(req.params(":sivu"));

            String sql = "SELECT id,nimi,viesti,pvm FROM Viestit WHERE ketju=? ORDER BY pvm DESC LIMIT ?,?";
            if(!sqlite) {
                sql = "SELECT id,nimi,viesti,pvm FROM Viestit WHERE ketju=? ORDER BY pvm DESC LIMIT ? OFFSET ?";
            }
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, ketju);
            if(sqlite) {
                statement.setInt(2, sivu*perPage);
                statement.setInt(3, perPage);
            }
            else {
                statement.setInt(2, perPage);
                statement.setInt(3, sivu*perPage);
            }
            System.out.println(statement.toString());
            ResultSet result = statement.executeQuery();

            List<Viesti> viestit = new ArrayList<>();
            Date d = new Date();
            while (result.next()) {
                String[] splits = getFormattedDate(result.getInt("pvm"));
                viestit.add(new Viesti(
                        result.getInt("id"),
                        result.getString("nimi"),
                        result.getString("viesti"),
                        splits[0],
                        splits[1]
                ));
            }

            sql = "SELECT nimi FROM Alueet WHERE id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, alue);
            result = statement.executeQuery();
            result.next();
            String alueNimi = result.getString("nimi");
            sql = "SELECT otsikko FROM Ketjut WHERE id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, ketju);
            result = statement.executeQuery();
            result.next();
            String ketjuNimi = result.getString("otsikko");

            List<Data> data = new ArrayList<>();
            Data tmpData = new Data();
            tmpData.setAlue(alue);
            tmpData.setKetju(ketju);
            tmpData.setAlueNimi(alueNimi);
            tmpData.setKetjuNimi(ketjuNimi);
            
            sql = "SELECT count(id) AS lkm FROM Viestit WHERE ketju=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, ketju);
            result = statement.executeQuery();
            result.next();
            int sivuLkm = result.getInt("lkm");
            
            List<Sivu> sivut = new ArrayList<>();
            for(int i=0; i<sivuLkm; i+=perPage) {
                int tmpSivu = i/perPage;
                sivut.add( new Sivu(tmpSivu, tmpSivu+1+"", tmpSivu==sivu?1:0));
            }
            
            map.put("viestit", viestit);
            map.put("data", tmpData);
            map.put("sivut", sivut);

            return new ModelAndView(map, "ketju");
        }, new ThymeleafTemplateEngine());


        // UUSIKETJU(post) - Lisää uusi ketju + viesti
        post("/uusiketju", (req, res) -> {
            String nimi = req.queryParams("nimi");
            String otsikko = req.queryParams("otsikko");
            String viesti = req.queryParams("viesti");
            int alue = Integer.parseInt(req.queryParams("alue"));
            int pvm = getTimestamp();
            int error = 0;
            System.out.println("Timestamp: "+getTimestamp());
            String sql = "INSERT INTO Ketjut (otsikko,lkm,pvm,alue) VALUES (?,1,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, otsikko);
            statement.setInt(2, pvm);
            statement.setInt(3, alue);
            int insertedRows = statement.executeUpdate();

            sql = "SELECT id FROM Ketjut WHERE pvm=? LIMIT 1";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, pvm);
            ResultSet result = statement.executeQuery();
            result.next();
            int ketju = result.getInt("id");
            
            if(insertedRows!=1) {
                error = 2;
            }
            if(error>0) {
                res.redirect("/error");
            }

            error = 0;
            System.out.println("Timestamp: " + getTimestamp());
            
            sql = "INSERT INTO Viestit (nimi,viesti,ketju,alue,pvm) VALUES (?,?,?,?,?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, nimi);
            statement.setString(2, viesti);
            statement.setInt(3, ketju);
            statement.setInt(4, alue);
            statement.setInt(5, pvm);
            insertedRows = statement.executeUpdate();
            if (insertedRows != 1) {
                error = 2;
            }
            if (error > 0) {
                res.redirect("/error");
            }
            sql = "UPDATE Alueet SET lkm = (lkm+1), pvm=? WHERE id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, pvm);
            statement.setInt(2, alue);
            int updatedRows = statement.executeUpdate();
            
            System.out.println(result.toString());
            
            res.redirect("/alue/"+alue);
            return "ok";
        });
        
        // UUSIVIESTI(post) - Lisää uusi viesti
        post("/uusiviesti", (req, res) -> {
            String nimi = req.queryParams("nimi");
            String viesti = req.queryParams("viesti");
            int alue = Integer.parseInt(req.queryParams("alue"));
            int ketju = Integer.parseInt(req.queryParams("ketju"));
            int pvm = getTimestamp();
            int error = 0;
            System.out.println("Timestamp: " + getTimestamp());
            String sql = "INSERT INTO Viestit (nimi,viesti,ketju,alue,pvm) VALUES (?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nimi);
            statement.setString(2, viesti);
            statement.setInt(3, ketju);
            statement.setInt(4, alue);
            statement.setInt(5, pvm);
            System.out.println(statement.toString());
            int insertedRows = statement.executeUpdate();
            ResultSet result = statement.getGeneratedKeys();
            if (insertedRows != 1) {
                error = 2;
            }
            if (error > 0) {
                res.redirect("/error");
            }

            sql = "UPDATE Ketjut SET lkm = (lkm+1), pvm=? WHERE id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, pvm);
            statement.setInt(2, ketju);
            int updatedRows = statement.executeUpdate();

            sql = "UPDATE Alueet SET lkm = (lkm+1), pvm=? WHERE id=?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, pvm);
            statement.setInt(2, alue);
            updatedRows = statement.executeUpdate();

            res.redirect("/ketju/" + alue + "/" + ketju + "/0");
            return "ok";
        });

}
    
    static int getTimestamp() {
        return (int) (System.currentTimeMillis()/1000L);
    }
    
    static String[] getFormattedDate(int unixtime) {
        return (new SimpleDateFormat("HH:mm dd.MM.yyyy").format(new Date(unixtime*1000L))).split(" ");
    }
}
