package usainbelt.affichage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class Fenetre4 extends JPanel implements ActionListener {

    private JButton boutonTri = new JButton("distance");
    public byte mode = 3;
    public LinkedList<Course> courses = new LinkedList<Course>();
    private JPanel panelLignes;
    private JLabel[] lignes;
    
    public Fenetre4() {
        
        // On importe les courses et on les met dans une LinkedList
        courses.add(new Course("20/05",(float)4.5,756));
        courses.add(new Course("18/05",(float)7.2,33));
        courses.add(new Course("18/05",(float)3.2,48));
        courses.add(new Course("18/05",(float)1.2,143));
        courses.add(new Course("18/05",(float)8.2,0));
        courses.add(new Course("18/05",(float)3.2,44));

        // Panel principal qui contient les deux panels
        JPanel conteneur = new JPanel();
        conteneur.setPreferredSize(new Dimension(MainWindow.LARGEUR, 585));
        add(conteneur);



        // Barre de tri
        JPanel panelTri = new JPanel();
        panelTri.setLayout(new FlowLayout(FlowLayout.CENTER,3,3));
        panelTri.setPreferredSize(new Dimension(MainWindow.LARGEUR,40));
        conteneur.add(panelTri);
        JLabel labelTri = new JLabel("Trier selon : ");
        labelTri.setForeground(Color.DARK_GRAY);
        labelTri.setFont(new Font("Open Sans", Font.BOLD, 20));
        panelTri.add(labelTri); 
        panelTri.add(boutonTri);
        boutonTri.setPreferredSize(new Dimension(110,30));
        boutonTri.setFont(new Font("Open Sans", Font.BOLD, 20));
        boutonTri.setBackground(MainWindow.COULEUR_FOND);
        boutonTri.setForeground(Color.DARK_GRAY);
        boutonTri.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2), BorderFactory.createEmptyBorder(0,0,0,0)));
        boutonTri.addActionListener(this);
        

        
        // Lignes
        panelLignes = new JPanel();
        panelLignes.setLayout(new FlowLayout(FlowLayout.CENTER,0,20));
        panelLignes.setPreferredSize(new Dimension(MainWindow.LARGEUR, 585));
        conteneur.add(panelLignes);
        afficher();
    }

    public void afficher() {
        panelLignes.removeAll();
        int taille = courses.size();
        lignes = new JLabel[taille];
        int k = 0;
        String s ="";
        for (Course c : courses) {
            s = "Course du "+c.getDate()+" : "+c.getDistance()+" km | "+formatTemps((float)c.getDuree())+" | "+formatTemps((float)c.getDuree()/c.getDistance())+" min/km";
            lignes[k] = new JLabel(s, SwingConstants.CENTER);
            panelLignes.add(lignes[k]);
            lignes[k].setFont(new Font("Open Sans", Font.BOLD, 30));
            JPanel separateur = new JPanel();
            separateur.setPreferredSize(new Dimension(700,5));
            separateur.setBackground(Color.DARK_GRAY);
            panelLignes.add(separateur);
            k++;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mode = (byte)(mode%3+1);
        switch (mode) {
            case 1:
                boutonTri.setText("durée");
                break;
            case 2:
                boutonTri.setText("allure");
                break;
            case 3:
                boutonTri.setText("distance");
                break;
        }
        courses.clear();
        courses = Main.bdd.courses(Main.idUser, mode);
        afficher();
    }

    public String formatTemps(float f) {
        String s = "";
        if ((int)f/60 < 10) {
            s += "0";
        }
        s += (int)f/60+":";
        if ((int)f%60 < 10) {
            s += "0";
        }   
        s += (int)f%60;     
        return s;
    }

}
