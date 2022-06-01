package usainbelt.affichage;

import usainbelt.javaarduino.tdtp.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import static usainbelt.affichage.Main.lissageExponentiel;

public class Bdd {

    // À adapter à votre BD
    private final String serveurBD = "fimi-bd-srv1.insa-lyon.fr";
    private final String portBD = "3306";
    private final String nomBD = "G223_A_BD1";
    private final String loginBD = "G223_A";
    private final String motdepasseBD = "G223_A";

    private static Connection connection = null;
    private static PreparedStatement selectUserDataStatement = null;
    private static PreparedStatement userExistsStatement = null;
    private static PreparedStatement selectLastSeanceStatement = null;
    private static PreparedStatement selectSeanceDataStatement = null;
    private static PreparedStatement selectVitesseMaxMinStatement = null;
    private static PreparedStatement selectVitessesInstant = null;
    private static PreparedStatement selectDistantTotSeptDeniers = null;
    private static PreparedStatement selectVitesseMoySeptDeniers = null;

    public void connexionBD() throws Exception {

        try {
            String urlJDBC = "jdbc:mysql://" + this.serveurBD + ":" + this.portBD + "/" + this.nomBD;
            urlJDBC += "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Europe/Paris";

            //System.out.println("Connexion à " + urlJDBC);
            connection = DriverManager.getConnection(urlJDBC, this.loginBD, this.motdepasseBD);

            //System.out.println("Connexion établie...");
            // Requête de test pour lister les tables existantes dans les BDs MySQL
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT table_schema, table_name"
                    + " FROM information_schema.tables"
                    + " WHERE table_schema NOT LIKE '%_schema' AND table_schema != 'mysql'"
                    + " ORDER BY table_schema, table_name");
            ResultSet result = statement.executeQuery();

            //System.out.println("Liste des tables:");
            while (result.next()) {
                //System.out.println("- " + result.getString("table_schema") + "." + result.getString("table_name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la méthode connexionBD()");
        }

    }

    public void fermetureConnexionBD() throws Exception {

        try {
            if (this.connection != null) {

                this.connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la méthode fermetureConnexionBD()");
        }

    }

    public void creerRequetesParametrees() throws Exception {
        try {
            selectUserDataStatement = connection.prepareStatement("SELECT * FROM Utilisateur WHERE idUtilisateur =?");
            userExistsStatement = connection.prepareStatement("SELECT EXISTS(SELECT * from Utilisateur WHERE idUtilisateur = ?) AS reponse");
            selectLastSeanceStatement = connection.prepareStatement("SELECT * FROM Seance WHERE idUtilisateur=? ORDER BY dateDebut DESC LIMIT ?");
            selectSeanceDataStatement = connection.prepareStatement("SELECT * FROM Seance WHERE idSeance =?");
            selectVitesseMaxMinStatement = connection.prepareStatement("SELECT * FROM (SELECT MAX(vitesseInstant) AS MAX FROM Mesure WHERE idSeance=?) AS MAX, (SELECT MIN(vitesseInstant) AS MIN FROM Mesure WHERE idSeance=?) AS MIN");
            selectVitessesInstant = connection.prepareStatement("SELECT dateMesure, vitesseInstant FROM Mesure WHERE idSeance = ?");
            selectVitesseMoySeptDeniers = connection.prepareStatement("SELECT AVG(vitesseMoy) moy FROM (SELECT vitesseMoy FROM Seance WHERE idUtilisateur=? AND vitesseMoy IS NOT NULL ORDER BY dateDebut DESC LIMIT 7) as res");
            selectDistantTotSeptDeniers = connection.prepareStatement("SELECT SUM(distanceParcourue) sum FROM (SELECT distanceParcourue FROM Seance WHERE idUtilisateur=? AND distanceParcourue IS NOT NULL ORDER BY dateDebut DESC LIMIT 7) AS res");
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la méthode creerRequetesParametrees()");
        }
    }

    public String[] getUserInfo(int idUser) {
        // Renvoie un tableau avec (nom, age, poids, taille, genre, idCeinture)
        String[] res = new String[6];
        try {
            selectUserDataStatement.setInt(1, idUser);
            ResultSet result = selectUserDataStatement.executeQuery();
            while (result.next()) {
                res[0] = result.getString("nom");
                res[1] = Integer.toString(result.getInt("age"));
                res[2] = Integer.toString(result.getInt("poids"));
                res[3] = Integer.toString(result.getInt("taille"));
                int genre = 0;
                if (result.getBoolean("genre")) {
                    genre = 1;
                }
                res[4] = Integer.toString(genre);
                res[5] = Integer.toString(result.getInt("idCeinture"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(new JFrame(), "L'utilisateur n'est pas inscrit !", "Erreur d'authenfication",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public boolean userExists(int idUser) {
        int res = 0;
        try {
            userExistsStatement.setInt(1, idUser);
            ResultSet result = userExistsStatement.executeQuery();
            while (result.next()) {
                res = result.getInt("reponse");
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        if (res == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static int getLastSeance(int idUser) {
        int res = -1;
        try {
            selectLastSeanceStatement.setInt(1, idUser);
            selectLastSeanceStatement.setInt(2, 1);
            ResultSet result = selectLastSeanceStatement.executeQuery();

            while (result.next()) {
                res = (result.getInt("idSeance"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public String[] getSeanceInfo(int idSeance) {
        // Renvoie un tableau avec (duree(h), idUtilisateur, vitesseMoy, distanceParcourue)
        String[] res = new String[4];
        try {
            selectSeanceDataStatement.setInt(1, idSeance);
            ResultSet result = selectSeanceDataStatement.executeQuery();
            while (result.next()) {
                Date debut = new Date(result.getTimestamp("dateDebut").getTime());
                Date fin = debut;
                if (result.getTimestamp("dateFin") != null) { // On vérifie si la séance est terminée
                    fin = new Date(result.getTimestamp("dateFin").getTime());
                }
                double dureeMin = (fin.getTime() / (1000 * 60)) - (debut.getTime() / (1000 * 60));
                double dureeHeure = dureeMin / 60;
                res[0] = Double.toString(dureeHeure);
                res[1] = Integer.toString(result.getInt("idUtilisateur"));
                res[2] = Double.toString(Main.arrondir(result.getDouble("vitesseMoy"), 1));
                res[3] = Double.toString(Main.arrondir(result.getDouble("distanceParcourue"), 1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public double[] getVitessesMaxMin(int idSeance) {
        // Renvoie un tableau avec la vitesse max et min d'une séance
        double[] res = new double[2];
        try {
            selectVitesseMaxMinStatement.setInt(1, idSeance);
            selectVitesseMaxMinStatement.setInt(2, idSeance);
            ResultSet result = selectVitesseMaxMinStatement.executeQuery();
            while (result.next()) {
                res[1] = result.getDouble("MAX");
                res[0] = result.getDouble("MIN");
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public LinkedList<int[]> vitessesSeance(int idSeance, double alpha) {
        LinkedList<int[]> valeurs = new LinkedList<int[]>();
        int secondeInitiale = 0;
        int[] valeur = new int[2];
        Date dateMesure;
        try {
            selectVitessesInstant.setInt(1, idSeance);
            ResultSet result = selectVitessesInstant.executeQuery();
            if (result.next()) { // Première mesure
                dateMesure = new Date(result.getTimestamp("dateMesure").getTime());
                secondeInitiale = (int) (dateMesure.getTime() / 1000);
                valeur[0] = 0;
                valeur[1] = (int) result.getDouble("vitesseInstant");

                valeurs.add(valeur);
            }
            while (result.next()) {
                dateMesure = new Date(result.getTimestamp("dateMesure").getTime());
                valeur = new int[2];
                valeur[0] = (int) ((dateMesure.getTime() / 1000) - secondeInitiale);
                valeur[1] = (int) result.getDouble("vitesseInstant");
                valeurs.add(valeur);
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        if (valeurs.isEmpty()) {
            int[] v = {0, 0};
            valeurs.add(v);
        }
        // lissage
        LinkedList<Double> vitessesLisses = new LinkedList<Double>();
        for (int[] v : valeurs) {
            vitessesLisses.add((double) v[1]);
        }
        vitessesLisses = lissageExponentiel(vitessesLisses, alpha);
        for (int i = 0; i < valeurs.size(); i++) {
            double nvVitesse = vitessesLisses.get(i);
            int[] v = new int[2];
            v[0] = valeurs.get(i)[0];
            v[1] = (int) nvVitesse;
            valeurs.set(i, v);
        }
        return valeurs;
    }

    public LinkedList<String> septDernieresDates(int idUser) {
        LinkedList<String> res = new LinkedList<String>();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");
        Date dateSeance;
        try {
            selectLastSeanceStatement.setInt(1, idUser);
            selectLastSeanceStatement.setInt(2, 7);
            ResultSet result = selectLastSeanceStatement.executeQuery();
            while (result.next()) {
                dateSeance = new Date(result.getTimestamp("dateDebut").getTime());
                res.add(dateFormat.format(dateSeance));
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        Collections.reverse(res);
        return res;
    }

    public LinkedList<Integer> septDernieresDistances(int idUser) {
        LinkedList<Integer> res = new LinkedList<Integer>();
        try {
            selectLastSeanceStatement.setInt(1, idUser);
            selectLastSeanceStatement.setInt(2, 7);
            ResultSet result = selectLastSeanceStatement.executeQuery();
            while (result.next()) {
                res.add((int) result.getDouble("distanceParcourue"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }

        // la derniere valeur est le max de la liste
        int max = res.get(0);
        for (int dist : res) {
            if (dist > max) {
                max = dist;
            }
        }
        res.add(max);
        Collections.reverse(res); // Le max de la liste est le premier élément
        return res;
    }

    public String distanceTotalSeptDerniers(int idUser) {
        String res = "";
        try {
            selectDistantTotSeptDeniers.setInt(1, idUser);
            ResultSet result = selectDistantTotSeptDeniers.executeQuery();
            while (result.next()) {
                res = Double.toString(Main.arrondir(result.getDouble("sum"), 1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public String vitesseMoySeptDerniers(int idUser) {
        String res = "";
        try {
            selectVitesseMoySeptDeniers.setInt(1, idUser);
            ResultSet result = selectVitesseMoySeptDeniers.executeQuery();
            while (result.next()) {
                Double vitesseMoy = result.getDouble("moy"); // en km/h
                vitesseMoy = vitesseMoy / 60; // en km/min
                vitesseMoy = 1 / vitesseMoy; // en min/km = allure
                res = Double.toString(Main.arrondir(vitesseMoy, 1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public String getCalories(int idUser, Double duree, int vitesseMoy) {
        double kcal = 0;
        // durée en HEURE, vitesseMoy en kmh
        int genre = Integer.parseInt(getUserInfo(idUser)[4]);
        int age = Integer.parseInt(getUserInfo(idUser)[1]);
        int taille = Integer.parseInt(getUserInfo(idUser)[3]); // en cm
        int poids = Integer.parseInt(getUserInfo(idUser)[2]); // en kg
        if (genre == 0) { // garçon
            kcal = (13.397 * poids + 4.799 * taille - 5.677 * age + 88.362) * (0.0486 * vitesseMoy + 0.093) * (duree / 24);
        }
        if (genre == 1) {// fille
            kcal = (9.247 * poids + 3.098 * taille - 4.330 * age + 447.593) * (0.0486 * vitesseMoy + 0.093) * (duree / 24);
        }
        return Double.toString(Main.arrondir(kcal, 1));
    }

    public int[] getBpmMaxMin(int idSeance) {
        String query = "SELECT * FROM (SELECT MAX(freqCardiaque) AS MAX FROM Mesure WHERE idSeance=?) AS MAX, (SELECT MIN(freqCardiaque) AS MIN FROM Mesure WHERE idSeance=?) AS MIN";
        int[] res = new int[2];
        try {
            PreparedStatement bpmMaxMinStatement = connection.prepareStatement(query);
            bpmMaxMinStatement.setInt(1, idSeance);
            bpmMaxMinStatement.setInt(2, idSeance);
            ResultSet rs = bpmMaxMinStatement.executeQuery();
            while (rs.next()) {
                res[0] = rs.getInt("MAX");
                res[1] = rs.getInt("MIN");
            }
        } catch (Exception err) {
            System.out.println(err);
        }
        return res;
    }

    public LinkedList<int[]> getBpmSeance(int idSeance) {
        String query = "SELECT freqCardiaque, dateMesure FROM Mesure WHERE idSeance = ?";
        LinkedList<int[]> res = new LinkedList<int[]>();
        Date dateMesure;
        int secondeInitiale = 0;
        int[] valeur = {0, 0};
        try {
            PreparedStatement bpmMaxMinStatement = connection.prepareStatement(query);
            bpmMaxMinStatement.setInt(1, idSeance);
            ResultSet rs = bpmMaxMinStatement.executeQuery();
            if (rs.next()) { // Première mesure
                dateMesure = new Date(rs.getTimestamp("dateMesure").getTime());
                secondeInitiale = (int) (dateMesure.getTime() / 1000);
                valeur[0] = 0;
                valeur[1] = (int) rs.getInt("freqCardiaque");
                res.add(valeur);
            }
            while (rs.next()) {
                dateMesure = new Date(rs.getTimestamp("dateMesure").getTime());
                valeur = new int[2];
                valeur[0] = (int) ((dateMesure.getTime() / 1000) - secondeInitiale);
                valeur[1] = (int) rs.getInt("freqCardiaque");
                res.add(valeur);
            }
        } catch (Exception err) {
            System.out.println(err);
        }
        return res;
    }

    public LinkedList<Integer> getSevenLastAllures(int idUser) {
        // Allures en min/km
        LinkedList<Integer> res = new LinkedList<Integer>();
        LinkedList<Integer> distances = septDernieresDistances(idUser);
        LinkedList<Integer> durees = new LinkedList<Integer>(); // en secondes
        int duree = 0;
        try {
            selectLastSeanceStatement.setInt(1, idUser);
            selectLastSeanceStatement.setInt(2, 7);
            ResultSet rs = selectLastSeanceStatement.executeQuery();
            while (rs.next()) {
                Date debut = new Date(rs.getTimestamp("dateDebut").getTime());
                Date fin = debut;
                if (rs.getTimestamp("dateFin") != null) {
                    fin = new Date(rs.getTimestamp("dateFin").getTime());
                }
                duree = (int) (fin.getTime() - debut.getTime()) / 1000; // EN SECONDE 
                durees.add(duree);
            }
        } catch (SQLException err) {
            System.out.println(err);
        }
        for (int i = 0; i < durees.size(); i++) {
            int allure = 0;
            if (distances.get(i) > 0.0001) {
                allure = (int) (((double) durees.get(i) / ((double) distances.get(i)) * 60.0)); // EN MIN/KM
            }
            res.add(allure);
        }
        return res;
    }

    public int getPas(int idSeance) {
        String query = "SELECT nbPas FROM Mesure WHERE idSeance = ? ORDER BY dateMesure DESC LIMIT 1";
        int res = 0;
        try {
            PreparedStatement nbPasStatement = connection.prepareStatement(query);
            nbPasStatement.setInt(1, idSeance);
            ResultSet rs = nbPasStatement.executeQuery();
            while (rs.next()) {
                res = rs.getInt("nbPas");
            }
        } catch (Exception err) {
            System.out.println(err);
        }
        return res;
    }

    public LinkedList<Course> courses(int idUser, int mode) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm");
        LinkedList<Course> res = new LinkedList<Course>();
        String query = "";
        switch (mode) {
            case 1:
                query = "SELECT dateDebut, dateFin, distanceParcourue FROM Seance WHERE idUtilisateur = ? AND dateFin IS NOT NULL ORDER BY TIMESTAMPDIFF(SECOND, dateDebut,dateFin) DESC LIMIT 6";
                break;
            case 2:
                query = "SELECT dateDebut, dateFin, distanceParcourue FROM Seance WHERE idUtilisateur = ? AND dateFin IS NOT NULL ORDER BY (TIMESTAMPDIFF(SECOND, dateDebut,dateFin)/distanceParcourue) DESC LIMIT 6";
                break;
            case 3:
                query = "SELECT dateDebut, dateFin, distanceParcourue FROM Seance WHERE idUtilisateur = ? AND dateFin IS NOT NULL ORDER BY distanceParcourue DESC LIMIT 6";
                break;
        }
        try {
            PreparedStatement coursesStatement = connection.prepareStatement(query);
            coursesStatement.setInt(1, idUser);
            ResultSet rs = coursesStatement.executeQuery();
            while (rs.next()) {
                Date debut = new Date(rs.getTimestamp("dateDebut").getTime());
                Date fin = debut;
                if (rs.getTimestamp("dateFin") != null) {
                    fin = new Date(rs.getTimestamp("dateFin").getTime());
                }
                int duree = (int) (fin.getTime() - debut.getTime()) / 1000;
                res.add(new Course(dateFormat.format(debut), (float) rs.getDouble("distanceParcourue"), duree));
            }
        } catch (Exception err) {
            System.out.println(err);
        }
        return res;
    }

    public static void filtrage(int idSeance) {
        // filtrage vitesse à 20km/h
        String query = "SELECT vitesseInstant, dateMesure FROM Mesure WHERE idSeance = ?";
        ArrayList<Float> vitesses = new ArrayList<Float>();
        ArrayList<Timestamp> date = new ArrayList<Timestamp>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                vitesses.add(rs.getFloat("vitesseInstant"));
                date.add(rs.getTimestamp("dateMesure"));
            }
        } catch (Exception err) {
            System.out.println(err);
        }
        for (int i = 1; i < vitesses.size(); i++) {
            if (vitesses.get(i) >= 20) {
                vitesses.set(i, vitesses.get(i - 1));
            }
        }
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE Mesure SET vitesseInstant = ? WHERE dateMesure = ?");
            for (int i = 0; i < vitesses.size(); i++) {
                ps.setFloat(1, vitesses.get(i));
                ps.setTimestamp(2, date.get(i));
                ps.executeUpdate();
            }

        } catch (Exception err) {
            System.out.println(err);
        }
    }

    public LinkedList<Utilisateur> utilisateurs(int mode) {
        LinkedList<Utilisateur> res = new LinkedList<Utilisateur>();
        String query = "";
        switch (mode) {
            case 1:
                query = "SELECT nom, SUM(distanceParcourue) distance, SUM(TIMESTAMPDIFF(second,dateDebut,dateFin)) duree\n"
                        + "FROM Utilisateur u, Seance s WHERE u.idUtilisateur = s.idUtilisateur AND dateFin IS NOT NULL \n"
                        + "GROUP BY(u.idUtilisateur)\n"
                        + "ORDER BY duree DESC";
                break;
            case 2:
                query = "SELECT nom, SUM(distanceParcourue) distance, SUM(TIMESTAMPDIFF(second,dateDebut,dateFin)) duree\n"
                        + "FROM Utilisateur u, Seance s WHERE u.idUtilisateur = s.idUtilisateur AND dateFin IS NOT NULL \n"
                        + "GROUP BY(u.idUtilisateur)\n"
                        + "ORDER BY (SUM(TIMESTAMPDIFF(second,dateDebut,dateFin))/SUM(distanceParcourue)) DESC";
                break;
            case 3:
                query = "SELECT nom, SUM(distanceParcourue) distance, SUM(TIMESTAMPDIFF(second,dateDebut,dateFin)) duree\n"
                        + "FROM Utilisateur u, Seance s WHERE u.idUtilisateur = s.idUtilisateur AND dateFin IS NOT NULL \n"
                        + "GROUP BY(u.idUtilisateur)\n"
                        + "ORDER BY distance DESC";
                break;
        }
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nom = rs.getString("nom");
                float distance = Float.parseFloat(Double.toString(rs.getDouble("distance")));
                int duree = rs.getInt("duree");
                res.add(new Utilisateur(nom, distance, duree));
            }
        } catch (Exception err) {
            System.out.println(err);
        }
        return res;
    }

    public LinkedList[] getLatLong(int idSeance) {
        String query = "SELECT latitude,longitude FROM Mesure WHERE idSeance = ?";
        LinkedList[] res = new LinkedList[2];
        LinkedList<Double> lat = new LinkedList<Double>();
        LinkedList<Double> longi = new LinkedList<Double>();
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, idSeance);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getDouble("latitude") > 40 && rs.getDouble("longitude") > 3) {
                    lat.add(rs.getDouble("latitude"));
                    longi.add(rs.getDouble("longitude"));
                }

            }
        } catch (Exception err) {
            System.out.println(err);
        }
        res[0] = lat;
        res[1] = longi;
        return res;
    }

}
