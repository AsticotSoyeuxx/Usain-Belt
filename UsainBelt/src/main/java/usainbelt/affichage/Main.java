package usainbelt.affichage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Main {

    public static Bdd bdd = new Bdd();
    public static int idUser = 1;
    public static int idLastSeance;
    public static MainWindow main;

    public static void main(String[] args) throws IOException {
        try {
            bdd.connexionBD();
            bdd.creerRequetesParametrees();
        } catch (Exception err) {
            System.out.println(err);
        }

        main = new MainWindow();
        update();
    }

    public static void setNewUser(int idNewUser) {
        if (bdd.userExists(idNewUser)) {
            idUser = idNewUser;
            update();
        } else {
            JOptionPane.showMessageDialog(new JFrame(), "L'utilisateur n'est pas inscrit !", "Erreur d'authenfication",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static double arrondir(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static LinkedList<Double> lissageExponentiel(LinkedList<Double> liste, double alpha) {
        LinkedList<Double> nouvelleListe = new LinkedList<Double>();
        boolean premier = true;
        double valeurPrecedente = 0;
        for (double valeur : liste) {
            if (premier) {
                nouvelleListe.add(valeur);
                premier = false;
            } else {
                nouvelleListe.add(valeur * alpha + valeurPrecedente * (1 - alpha));
            }
            valeurPrecedente = valeur;
        }
        return nouvelleListe;
    }

    public static void update() {
        idLastSeance = bdd.getLastSeance(idUser);

        // Bandeau en haut
        main.boutonNom.setText(bdd.getUserInfo(idUser)[0]);

        // Fenetre 1
        /// Graph vitesse instant
        main.fenetre1.panel1.remove(main.fenetre1.graph1);
        main.fenetre1.graph1 = new JGraph("VITESSE", Color.BLUE, (int) bdd.getVitessesMaxMin(idLastSeance)[0], (int) bdd.getVitessesMaxMin(idLastSeance)[1]);
        main.fenetre1.graph1.valeurs = bdd.vitessesSeance(idLastSeance, 0.7);
        main.fenetre1.graph1.setPreferredSize(new Dimension(980, 100));
        main.fenetre1.graph1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        main.fenetre1.panel1.add(main.fenetre1.graph1, BorderLayout.WEST);
        main.fenetre1.panel1.validate();
        main.fenetre1.panel1.repaint();

        /// histogramme distances
        main.fenetre1.panel2.remove(main.fenetre1.graph2);
        LinkedList<Integer> distances = bdd.septDernieresDistances(idUser);
        int distanceMax = distances.removeFirst();
        main.fenetre1.graph2 = new JHistogram("DISTANCE PARCOURUE DURANT LES 7 DERNIERES SEANCES", Color.RED, distanceMax);
        main.fenetre1.graph2.valeurs = distances;
        main.fenetre1.graph2.textes = bdd.septDernieresDates(idUser);
        main.fenetre1.graph2.setPreferredSize(new Dimension(980, 100));
        main.fenetre1.graph2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        main.fenetre1.panel2.add(main.fenetre1.graph2, BorderLayout.WEST);
        main.fenetre1.panel2.validate();
        main.fenetre1.panel2.repaint();
        
        /// textes
        main.fenetre1.distanceValeurs1.setText(bdd.getSeanceInfo(idLastSeance)[2] + " km");
        main.fenetre1.vitesseValeurs1.setText(bdd.getSeanceInfo(idLastSeance)[3] + " km/h");
        main.fenetre1.caloriesValeurs1.setText(bdd.getCalories(idUser, Double.parseDouble(bdd.getSeanceInfo(idLastSeance)[0]), (int) Double.parseDouble(bdd.getSeanceInfo(idLastSeance)[2])) + " kcal");
        main.fenetre1.distanceValeurs2.setText(bdd.distanceTotalSeptDerniers(idUser) + " km");
        main.fenetre1.vitesseValeurs2.setText(bdd.vitesseMoySeptDerniers(idUser) + " min/km");

        // Fenetre 3
        /// graphBPM
        main.fenetre3.panelCentre.remove(main.fenetre3.graphBPM);
        int[] bpmMaxMin = bdd.getBpmMaxMin(idLastSeance);
        int FC = 220 - Integer.parseInt(bdd.getUserInfo(idUser)[1]);
        main.fenetre3.graphBPM = new JGraphECG("Fréquence cardiaque", bpmMaxMin[1], bpmMaxMin[0], FC);
        LinkedList<int[]> bpm3 = bdd.getBpmSeance(idLastSeance);
        main.fenetre3.graphBPM.valeurs = bpm3;
        main.fenetre3.graphBPM.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        main.fenetre3.panelCentre.add(main.fenetre3.graphBPM);
        main.fenetre3.panelCentre.validate();
        main.fenetre3.panelCentre.repaint();
        
        /// histogramme
        main.fenetre3.panelCentre.remove(main.fenetre3.histo);
        LinkedList<Integer> allures = bdd.getSevenLastAllures(idUser);
        main.fenetre3.histo = new JHistogram("Allures (min/km)", Color.ORANGE, Collections.max(allures));
        main.fenetre3.histo.valeurs = allures;
        main.fenetre3.histo.textes = bdd.septDernieresDates(idUser);
        main.fenetre3.histo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        main.fenetre3.panelCentre.add(main.fenetre3.histo);
        main.fenetre3.panelCentre.validate();
        main.fenetre3.panelCentre.repaint();

        /// textes
        main.fenetre3.BPMMaxValeurs.setText(Integer.toString(bpmMaxMin[0]));
        main.fenetre3.BPMMinValeurs.setText(Integer.toString(bpmMaxMin[1]));
        int bpmMoy = 0;
        for (int[] bpm : bpm3) {
            bpmMoy += bpm[1];
        }
        bpmMoy = (bpmMoy) / bpm3.size();
        main.fenetre3.BPMMoyenValeurs.setText(Integer.toString(bpmMoy));
        double allureMoy = 1 / (Double.parseDouble(bdd.getSeanceInfo(idLastSeance)[2]) * 60);
        main.fenetre3.allureValeurs.setText(Double.toString(arrondir(allureMoy, 2)) + " min/km");
        int nbPas = bdd.getPas(idLastSeance);
        main.fenetre3.pasValeurs.setText(Integer.toString(nbPas) + " pas");
        int dureeMin = (int) (Double.parseDouble(bdd.getSeanceInfo(idLastSeance)[0]) * 60);
        int cadence = (int) ((double) nbPas / (double) dureeMin);
        main.fenetre3.cadenceValeurs.setText(Integer.toString(cadence) + " pas/min");
        int foulee = (int) (Double.parseDouble(bdd.getSeanceInfo(idLastSeance)[3]) * 1000 / (double) nbPas);
        main.fenetre3.fouleeValeurs.setText(Integer.toString(foulee) + " mètres");

        // Fenetre 4
        main.fenetre4.courses = Main.bdd.courses(Main.idUser, main.fenetre4.mode);
        main.fenetre4.afficher();

        // Fenetre 5
        main.fenetre5.utilisateurs = Main.bdd.utilisateurs(main.fenetre5.mode);
        main.fenetre5.afficher();
        
        // Fenetre 2
        main.fenetre2.updateFenetre2();
    }
}
