package usainbelt.javaarduino;

import usainbelt.javaarduino.tdtp.TD1;
import usainbelt.javaarduino.usb.ArduinoManager;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class LecteurArduino {

    public static int numSeance;
    public static int bpm;
    public static int pas;
    public static double longi;
    public static double lat;
    public static int indiceLigne = -1;
    public static int decalage;
    public static Calendar cal = Calendar.getInstance();

    public LecteurArduino(int idSeance) {
        numSeance = idSeance;
        Main.console.log("TOUS les Ports COM Virtuels:");
        for (String port : ArduinoManager.listVirtualComPorts()) {
            Main.console.log(" - " + port);
        }
        Main.console.log("----");

        // Recherche d'un port disponible (avec une liste d'exceptions si besoin)
        String myPort = ArduinoManager.searchVirtualComPort("COM0", "/dev/tty.usbserial-FTUS8LMO", "<autre-exception>");

        Main.console.log("CONNEXION au port " + myPort);

        ArduinoManager arduino = new ArduinoManager(myPort) {
            @Override
            protected void onData(String line) {
                lirePaquet(line);
            }
        };

        try {
            Main.console.log("DÉMARRAGE de la connexion arduino");
            arduino.start();
            // Boucle d'ecriture sur l'arduino (execution concurrente au thread)
            boolean exit = false;
            while (!exit) {
                String line = Main.console.readLine("Envoyer une ligne (ou 'stop') > ");
                if (line.length() != 0) {
                    Main.console.log("CLAVIER >> " + line);
                    exit = line.equalsIgnoreCase("stop");
                    if (!exit) {
                        arduino.write(line);
                    }
                }
            }
            Main.console.log("ARRÊT de la connexion");
            arduino.stop();

        } catch (IOException ex) {
            Main.console.log(ex);
        }

    }

    public static void lirePaquet(String ligne) {

        switch (indiceLigne) {
            case 0:
                bpm = Integer.parseInt(ligne);
                indiceLigne++;
                break;
            case 1:
                pas = Integer.parseInt(ligne);
                indiceLigne++;
                break;
            case 2:
                lat = Double.parseDouble(ligne) / 1000000;
                indiceLigne++;
                break;
            case 3:
                longi = Double.parseDouble(ligne) / 1000000;
                indiceLigne = -1;
                Calendar newCal = Calendar.getInstance(); // on crée un nouveau calendrier pour ne pas modifier le time de l'ancien
                newCal.setTime(cal.getTime());
                newCal.add(Calendar.SECOND, decalage); //on applique le décalage au nouveau calendrier
                TD1.ajouterMesure(newCal.getTime(), bpm, lat, longi, pas, numSeance);
                Main.console.log("Nouvelle mesure le : " + newCal.getTime());
                break;
            default:
                if (ligne.equals("DEBUT")) { // Début d'un fragment
                    indiceLigne = 0;
                    decalage++;
                }
                if (ligne.equals("PAQUET")) { // Début d'un paquet
                    decalage = -4;
                    cal.setTime(new Date());
                }
                break;
        }
    }

    public static double decimale(double coord) {
        // Convertir une coordonnée en degré minute/sec vers des degré décimal
        double coordA = usainbelt.affichage.Main.arrondir(coord, 5);
        double deg = Double.parseDouble(Double.toString(coordA).split("\\.")[0]);
        String nbMinuteSeconde = Double.toString(coordA).split("\\.")[1];
        if (Double.parseDouble(nbMinuteSeconde) < 0.000001) {
            nbMinuteSeconde = "0";
        }
        int taille = nbMinuteSeconde.length();
        switch (taille) {
            case 4:
                nbMinuteSeconde = nbMinuteSeconde + 0;
                break;
            case 3:
                nbMinuteSeconde = nbMinuteSeconde + 0 + 0;
                break;
            case 2:
                nbMinuteSeconde = nbMinuteSeconde + 0 + 0 + 0;
                break;
            case 1:
                nbMinuteSeconde = nbMinuteSeconde + 0 + 0 + 0 + 0;
                break;
        }
        System.out.println(nbMinuteSeconde);
        String min = Character.toString(nbMinuteSeconde.charAt(0)) + Character.toString(nbMinuteSeconde.charAt(1));
        String sec = Character.toString(nbMinuteSeconde.charAt(2)) + Character.toString(nbMinuteSeconde.charAt(3)) + "." + Character.toString(nbMinuteSeconde.charAt(4));

        double decimale = (Double.parseDouble(min) / 60) + (Double.parseDouble(sec) / 3600);
        double res = deg + decimale;
        return res;
    }

}
