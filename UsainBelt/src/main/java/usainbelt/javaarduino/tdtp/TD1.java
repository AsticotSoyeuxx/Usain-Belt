package usainbelt.javaarduino.tdtp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import usainbelt.javaarduino.LecteurArduino;

public class TD1 {

    // À adapter à votre BD
    private final String serveurBD = "fimi-bd-srv1.insa-lyon.fr";
    private final String portBD = "3306";
    private final String nomBD = "G223_A_BD1";
    private final String loginBD = "G223_A";
    private final String motdepasseBD = "G223_A";

    private static Connection connection = null;
    private static PreparedStatement insertMesureStatement = null;
    private static PreparedStatement selectMesuresStatement = null;
    private static PreparedStatement insertSeanceStatement = null;
    private static PreparedStatement selectLastSeanceStatement = null;
    private static PreparedStatement selectUsersStatement = null;
    private static PreparedStatement insertVitesseStatement = null;
    private static PreparedStatement selectSeanceDataStatement = null;

    public void connexionBD() throws Exception {

        try {
            //Enregistrement de la classe du driver par le driverManager
            //Class.forName("com.mysql.jdbc.Driver");
            //System.out.println("Driver trouvé...");
            //Création d'une connexion sur la base de donnée
            String urlJDBC = "jdbc:mysql://" + this.serveurBD + ":" + this.portBD + "/" + this.nomBD;
            urlJDBC += "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Europe/Paris";

            System.out.println("Connexion à " + urlJDBC);
            connection = DriverManager.getConnection(urlJDBC, this.loginBD, this.motdepasseBD);

            System.out.println("Connexion établie...");

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
            insertMesureStatement = this.connection.prepareStatement("INSERT INTO Mesure (dateMesure,freqCardiaque,latitude,longitude,nbPas,idSeance) VALUES (?,?,?,?,?,?)");
            selectMesuresStatement = this.connection.prepareStatement("SELECT * FROM Mesure WHERE idSeance=?");
            insertSeanceStatement = this.connection.prepareStatement("INSERT INTO Seance (dateDebut, idUtilisateur) VALUES (?,?)");
            selectLastSeanceStatement = this.connection.prepareStatement("SELECT * FROM Seance WHERE idUtilisateur=? ORDER BY idSeance DESC LIMIT 1");
            selectSeanceDataStatement = this.connection.prepareStatement("SELECT * FROM Seance WHERE idSeance=?");
            selectUsersStatement = this.connection.prepareStatement("SELECT * FROM Utilisateur");
            insertVitesseStatement = this.connection.prepareStatement("UPDATE Mesure SET vitesseInstant=? WHERE dateMesure=?");
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la méthode creerRequetesParametrees()");
        }
    }

    public static int ajouterMesure(Date dateMesure, int freqCardiaque, double lat, double longi, int nbPas, int idSeance) {
        try {
            insertMesureStatement.setTimestamp(1, new Timestamp(dateMesure.getTime()));
            insertMesureStatement.setInt(2, freqCardiaque);
            insertMesureStatement.setDouble(3, lat);
            insertMesureStatement.setDouble(4, longi);
            insertMesureStatement.setInt(5, nbPas);
            insertMesureStatement.setInt(6, idSeance);
            return insertMesureStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            return -1;
        }
    }

    public static int nouvelleSeance(int idUser) {
        Date today = new Date();
        try {
            insertSeanceStatement.setTimestamp(1, new Timestamp(today.getTime()));
            insertSeanceStatement.setInt(2, idUser);
            return insertSeanceStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            return -1;
        }
    }

    public static int getLastSeance(int idUser) {
        try {
            selectLastSeanceStatement.setInt(1, idUser);
            ResultSet result = selectLastSeanceStatement.executeQuery();

            while (result.next()) {
                return (result.getInt("idSeance"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            return -1;
        }
        return -1;
    }

    public static LinkedList<String> getUsers() {
        LinkedList<String> res = new LinkedList<String>();
        try {
            ResultSet result = selectUsersStatement.executeQuery();
            while (result.next()) {
                res.add(result.getString("nom"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public static int getUserId(String nom) {
        String query = "SELECT idUtilisateur FROM Utilisateur WHERE nom=\"" + nom + "\"";
        int res = -1;
        try {
            PreparedStatement req = connection.prepareStatement(query);
            ResultSet result = req.executeQuery();
            while (result.next()) {
                res = result.getInt("idUtilisateur");
            }
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return res;
    }

    public static void finSeance(int idSeance, Date date) {
        // Stocke les données suivantes :
        // Seance(dateFin)
        // Seance(distanceParcourue)
        // Seance(vitesseMoy)
        // Mesure(vitesseInstant)
        String query = "UPDATE Seance SET dateFin=?, distanceParcourue=?, vitesseMoy=? WHERE idSeance=?";
        try {
            PreparedStatement req = connection.prepareStatement(query);
            req.setTimestamp(1, new Timestamp(date.getTime()));
            req.setDouble(2, (double) TD1.distanceTotal(idSeance));
            req.setFloat(3, 0);
            req.setInt(4, idSeance);
            req.executeUpdate(); // On doit stocker la dateFin avant de calculer la vitesseMoy
            req.setDouble(3, TD1.calculVitesseMoy(idSeance));
            req.executeUpdate();

            calculVitesseInstant(idSeance);
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static float distance(double[] pointA, double[] pointB) {
        // !!! point A et point B en DEGRE DECIMALE !!!
        // Methode Pythagore cf : http://villemin.gerard.free.fr/aGeograp/Distance.htm
        // Retourne la distance en KILOMETRE
        double latA = pointA[0];
        double longA = pointA[1];
        double latB = pointB[0];
        double longB = pointB[1];
        float x = (float) ((longB - longA) * Math.cos(Math.toRadians((latA + latB) / 2)));
        float y = (float) (latB - latA);
        float z = (float) (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
        float d = (float) (1.85185 * 60 * z); // 1 Mile marine = 1.85185 km (cf wiki)
        return d;
    }

    public static float vitesse(double[] pointA, double[] pointB, Date dateA, Date dateB) {
        float deltaT = Math.abs((dateB.getTime() - dateA.getTime()) / 1000); // EN SECONDE
        float deltaTHeure = deltaT / (60 * 60); // EN HEURE
        float distance = distance(pointA, pointB);
        float res = distance / deltaTHeure;
        return res;
    }

    public static float distanceTotal(int idSeance) {
        LinkedList<double[]> listePoints = new LinkedList<double[]>();

        try {
            selectMesuresStatement.setInt(1, idSeance);
            ResultSet result = selectMesuresStatement.executeQuery();
            while (result.next()) {
                double[] points = new double[2];
                points[0] = result.getDouble("latitude");
                points[1] = result.getDouble("longitude");
                listePoints.add(points);
            }

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            return -1;
        }

        float res = 0;
        double[] pointPrecedent = {0, 0};
        for (double[] p : listePoints) {
            if (p != listePoints.get(0)) {
                res += distance(pointPrecedent, p);
            }
            pointPrecedent = p;
        }

        return res;
    }

    public static void calculVitesseInstant(int idSeance) {
        // calcule la vitesses instantanée à chaque instant pour une séance et update les mesures
        ArrayList<double[]> listePoints = new ArrayList<double[]>();
        ArrayList<Date> listeDates = new ArrayList<Date>();
        try {
            selectMesuresStatement.setInt(1, idSeance);
            ResultSet result = selectMesuresStatement.executeQuery();
            while (result.next()) {
                double[] points = new double[2];
                points[0] = result.getDouble("latitude");
                points[1] = result.getDouble("longitude");
                listePoints.add(points);
                listeDates.add(new Date(result.getTimestamp("dateMesure").getTime()));
            }

            int taille = listeDates.size(); // listeDates et listePoints ont la même taille
            for (int i = 1; i < taille; i++) {
                float vitesseInstant = vitesse(listePoints.get(i - 1), listePoints.get(i), listeDates.get(i - 1), listeDates.get(i));
                insertVitesseStatement.setFloat(1, vitesseInstant);
                insertVitesseStatement.setTimestamp(2, new Timestamp(listeDates.get(i).getTime()));
                insertVitesseStatement.executeUpdate();
            }

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static double calculVitesseMoy(int idSeance) {
        double distance = 0;
        Date debut = new Date();
        Date fin = new Date();
        try {
            selectSeanceDataStatement.setInt(1, idSeance);
            ResultSet result = selectSeanceDataStatement.executeQuery();
            while (result.next()) {
                debut = new Date(result.getTimestamp("dateDebut").getTime());
                fin = new Date(result.getTimestamp("dateFin").getTime());
                distance = result.getDouble("distanceParcourue");
            }
            double deltaT = Math.abs((fin.getTime() - debut.getTime())) / (1000); // EN SECONDE
            double deltaTHeure = deltaT / (60 * 60);
            return distance / deltaTHeure;
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            return -1;
        }
    }

    public void majCoord(int idSeance) {
        // convertis tous les coord d'une séance en degré décimale
        LinkedList<Double> listeLat = new LinkedList<Double>();
        LinkedList<Double> listeLong = new LinkedList<Double>();
        ArrayList<Date> listeDates = new ArrayList<Date>();

        String query1 = "SELECT latitude, longitude, dateMesure FROM Mesure WHERE idSeance=?";
        String query2 = "UPDATE Mesure SET latitude=?, longitude =? WHERE dateMesure=?";
        String query3 = "SELECT dateFin FROM Seance WHERE idSeance = ?";

        try {
            PreparedStatement req1 = connection.prepareStatement(query1);
            req1.setInt(1, idSeance);
            ResultSet result = req1.executeQuery();
            while (result.next()) {
                listeLat.add(result.getDouble("latitude"));
                listeLong.add(result.getDouble("longitude"));
                listeDates.add(new Date(result.getTimestamp("dateMesure").getTime()));
            }
        } catch (Exception ex) {
            System.out.println("1");
        }

        try {
            PreparedStatement req2 = connection.prepareStatement(query2);
            for (int i = 0; i < listeLong.size(); i++) {
                double newLat = LecteurArduino.decimale(listeLat.get(i));
                double newLong = LecteurArduino.decimale(listeLong.get(i));
                Timestamp date = new Timestamp(listeDates.get(i).getTime());
                req2.setDouble(1, newLat);
                req2.setDouble(2, newLong);

                req2.setTimestamp(3, date);
                req2.executeUpdate();

            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        try {
            PreparedStatement req3 = connection.prepareStatement(query3);
            req3.setInt(1, idSeance);
            ResultSet result = req3.executeQuery();
            Timestamp datefin = new Timestamp((new Date()).getTime());
            while (result.next()) {
                datefin = result.getTimestamp("dateFin");
            }
            finSeance(idSeance, new Date(datefin.getTime()));
        } catch (Exception ex) {
            System.out.println("3");
        }
        System.out.println("Maj terminée!");
    }

}
