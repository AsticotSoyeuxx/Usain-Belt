package usainbelt.javaarduino;

import usainbelt.javaarduino.tdtp.TD1;
import usainbelt.javaarduino.util.Console;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Date;

public class Main {

    public static TD1 td1 = new TD1();
    public static final Console console = new Console();
    public static void main(String[] args) {
        
        try {
            td1.connexionBD();
            td1.creerRequetesParametrees();
            //td1.majCoord(14);
            LinkedList<String> utilisateurs = TD1.getUsers();
            boolean estConnecte = false;
            while (!estConnecte) {
                String line = console.readLine("Nom d'utilisateur > ");
                if (line.length() != 0) {
                    console.log("CLAVIER >> " + line);
                    estConnecte = utilisateurs.contains(line);
                    if (estConnecte) {
                        int utilisateur = TD1.getUserId(line);
                        td1.nouvelleSeance(utilisateur);
                        int idSeance = TD1.getLastSeance(utilisateur);
                        
                        console.log("------- NOUVELLE SEANCE -------");
                        LecteurArduino TA = new LecteurArduino(idSeance); // écrire "stop" dans la console pour aller à la ligne suivante
                        td1.finSeance(idSeance, new Date());
                        console.log("------- FIN SEANCE -------");

                    } else {
                        console.log("Utilisateur inexistant !");
                    }
                }
            }

            td1.fermetureConnexionBD();
        } catch (IOException ex) {
            console.log(ex);
        } catch (Exception err) {
            System.out.println(err);
        }

    }

}
