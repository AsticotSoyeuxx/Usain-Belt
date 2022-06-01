package usainbelt.affichage;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.io.File;
import java.util.LinkedList;

public class JMaps extends JPanel {

    // Les variables utilisées
    // Image statique de la carte
    public BufferedImage carte;
    // Paramètres image
    private final int ZOOM = 15;
    private final int WIDTH = 700;
    private final int HEIGHT = 530;
    // Génération lien image statique
    private final String START = "https://maps.geoapify.com/v1/staticmap?style=osm-carto";
    private final String API_KEY = "f02d87f54d4c417db2f6eca959996a0a";
    private String urlString;
    // Les coordonnées GPS
    public double centreLat;
    public double centreLon;
    public int[] deltaLat;
    public int[] deltaLon;
    public LinkedList<Double> lat = new LinkedList<Double>();
    public LinkedList<Double> lon = new LinkedList<Double>();

    public JMaps(LinkedList<Double> latt, LinkedList<Double> longi) {
        this.deltaLat = new int[latt.size()];
        this.deltaLon = new int[longi.size()];
        this.centreLat = latt.get(0);
        this.centreLon = longi.get(0);
        // Création de l'url de l'image
        urlString = START + "&width=" + WIDTH + "&height=" + HEIGHT + "&center=lonlat:" + centreLon + "," + centreLat + "&zoom=" + ZOOM + "&apiKey=" + API_KEY;
        // Récupere l'image d'après le lien
        try {
            URL url = new URL(urlString);
            BufferedImage img = ImageIO.read(url);
            File file = new File(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/carte.jpg");
            ImageIO.write(img, "jpg", file);
            carte = ImageIO.read(file);
        } catch (IOException io) {
            System.out.println("Pas de connexion à Internet.");
        }
        // Remplissage
        for (int i = 0; i < deltaLat.length; i++) {
            lat.add(latt.get(i));
            lon.add(longi.get(i));
            lat = Main.lissageExponentiel(lat, 0.1);
            lon = Main.lissageExponentiel(lon, 0.1);
            deltaLat[i] = (HEIGHT / 2) - (int) ((lat.get(i) - centreLat) * (67 / 0.001));
            deltaLon[i] = (WIDTH / 2) + (int) ((lon.get(i) - centreLon) * (47 / 0.001));
        }

    }

    @Override
    protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        Graphics2D g = (Graphics2D) gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(carte, 0, 0, null);
        g.setColor(Color.MAGENTA);
        g.fillOval((int) WIDTH / 2, (int) HEIGHT / 2, 10, 10);
        g.setColor(Color.RED);
        g.drawPolyline(deltaLon, deltaLat, deltaLon.length);
        g.setColor(Color.BLUE);
        g.fillOval((WIDTH / 2) + (int) ((lon.get(lon.size() - 1) - centreLon) * (47 / 0.001)), (HEIGHT / 2) - (int) ((lat.get(lat.size() - 1) - centreLat) * (67 / 0.001)), 10, 10);
    }

}
