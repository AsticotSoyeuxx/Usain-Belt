package usainbelt.affichage;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class MainWindow extends JFrame implements ActionListener {

    static final int LARGEUR = 1280;
    static final int HAUTEUR = 720;
    public JPanel conteneur = new JPanel();
    private JPanel fenetre;
    public Timer timer;

    // Icônes pour les boutons
    private final Icon ICONE_1_OFF = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone1off.png");
    private final Icon ICONE_2_OFF = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone2off.png");
    private final Icon ICONE_3_OFF = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone3off.png");
    private final Icon ICONE_4_OFF = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone4off.png");
    private final Icon ICONE_5_OFF = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone5off.png");
    private final Icon ICONE_QUITTER_OFF = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/iconeQuitteroff.png");
    private final Icon ICONE_1_ON = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone1on.png");
    private final Icon ICONE_2_ON = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone2on.png");
    private final Icon ICONE_3_ON = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone3on.png");
    private final Icon ICONE_4_ON = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone4on.png");
    private final Icon ICONE_5_ON = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/icone5on.png");
    private final Icon ICONE_QUITTER_ON = new ImageIcon(System.getProperty("user.dir") + System.getProperty("file.separator")+ "UsainBelt/assets/iconeQuitteron.png");

    // Boutons de la barre du haut
    private JButton bouton1 = new JButton(ICONE_1_ON);
    private JButton bouton2 = new JButton(ICONE_2_OFF);
    private JButton bouton3 = new JButton(ICONE_3_OFF);
    private JButton bouton4 = new JButton(ICONE_4_OFF);
    private JButton bouton5 = new JButton(ICONE_5_OFF);
    private JButton boutonQuitter = new JButton(ICONE_QUITTER_OFF);
    public JButton boutonNom = new JButton(Main.bdd.getUserInfo(Main.idUser)[0]);

    // Couleurs de l'IHM
    static final Color COULEUR_FOND = new Color(255, 171, 45);
    static final Color COULEUR_BORDS = new Color(255, 171, 45);

    // fenetres
    public Fenetre1 fenetre1;
    public Fenetre2 fenetre2;
    public Fenetre3 fenetre3;
    public Fenetre4 fenetre4;
    public Fenetre5 fenetre5;

    public MainWindow() throws IOException {

        // La fenêtre utilise par défaut le style de l'OS de l'utilisateur
        timer = new Timer(2000, this);
        timer.start();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            fenetre1 = new Fenetre1();
            fenetre2 = new Fenetre2();
            fenetre3 = new Fenetre3();
            fenetre4 = new Fenetre4();
            fenetre5 = new Fenetre5();
        } catch (Exception err) {
            System.out.println(err);
        }

        // Création de l'interface graphique
        this.setSize(LARGEUR, HAUTEUR);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setTitle("Usain Belt");
        conteneur.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        add(conteneur);

        // Panel contenant les boutons
        JPanel panelBoutons = new JPanel();
        panelBoutons.setLayout(new FlowLayout(FlowLayout.LEADING, 3, 3));
        conteneur.add(panelBoutons);
        panelBoutons.setBounds(0, 0, LARGEUR, 96);
        panelBoutons.setBackground(COULEUR_FOND);
        panelBoutons.add(bouton1);
        panelBoutons.add(bouton2);
        panelBoutons.add(bouton3);
        panelBoutons.add(bouton4);
        panelBoutons.add(bouton5);
        Dimension dim = new Dimension(170, 90);
        bouton1.setPreferredSize(dim);
        bouton2.setPreferredSize(dim);
        bouton3.setPreferredSize(dim);
        bouton4.setPreferredSize(dim);
        bouton5.setPreferredSize(dim);
        bouton1.setBackground(COULEUR_FOND);
        bouton2.setBackground(COULEUR_FOND);
        bouton3.setBackground(COULEUR_FOND);
        bouton4.setBackground(COULEUR_FOND);
        bouton5.setBackground(COULEUR_FOND);
        Border bord = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COULEUR_BORDS, 2), BorderFactory.createEmptyBorder(0, 0, 0, 0));
        bouton1.setBorder(bord);
        bouton2.setBorder(bord);
        bouton3.setBorder(bord);
        bouton4.setBorder(bord);
        bouton5.setBorder(bord);
        bouton1.addActionListener(this);
        bouton2.addActionListener(this);
        bouton3.addActionListener(this);
        bouton4.addActionListener(this);
        bouton5.addActionListener(this);

        // Panel pour le nom de l'utilisateur
        JPanel panelNom = new JPanel();
        panelNom.setLayout(new GridLayout(2, 1));
        panelNom.setBackground(COULEUR_FOND);
        panelBoutons.add(panelNom);

        // Label du nom de l'utilisateur
        JLabel labelUtilisateur = new JLabel("        Utilisateur :       ", SwingConstants.CENTER);
        labelUtilisateur.setFont(new Font("Open Sans", Font.BOLD, 26));
        labelUtilisateur.setForeground(Color.BLACK);
        panelNom.add(labelUtilisateur);

        // Bouton du nom de l'utilisateur
        panelNom.add(boutonNom);
        boutonNom.setBackground(COULEUR_FOND);
        boutonNom.setForeground(Color.BLACK);
        boutonNom.setBorder(bord);
        boutonNom.addActionListener(this);
        boutonNom.setFont(new Font("Open Sans", Font.BOLD, 34));

        // Bouton quitter
        panelBoutons.add(boutonQuitter);
        boutonQuitter.setPreferredSize(new Dimension(121, 90));
        boutonQuitter.setBackground(COULEUR_FOND);
        boutonQuitter.setBorder(bord);
        boutonQuitter.addActionListener(this);

        // Ligne pour délimiter les panels
        JPanel ligne = new JPanel();
        conteneur.add(ligne);
        ligne.setPreferredSize(new Dimension(LARGEUR, 3));
        ligne.setBackground(Color.DARK_GRAY);

        // Fenêtre par défaut
        fenetre = fenetre1;
        conteneur.add(fenetre);
        fenetre.setPreferredSize(new Dimension(LARGEUR, 585));

        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            //Main.update();
        }

        conteneur.remove(fenetre);
        if (e.getSource() == bouton1) {
            iconesOff();
            bouton1.setIcon(ICONE_1_ON);
            fenetre = fenetre1;
        }
        if (e.getSource() == bouton2) {
            iconesOff();
            bouton2.setIcon(ICONE_2_ON);
            fenetre = fenetre2;
        }
        if (e.getSource() == bouton3) {
            iconesOff();
            bouton3.setIcon(ICONE_3_ON);
            fenetre = fenetre3;
        }
        if (e.getSource() == bouton4) {
            iconesOff();
            bouton4.setIcon(ICONE_4_ON);
            fenetre = fenetre4;
        }
        if (e.getSource() == bouton5) {
            iconesOff();
            bouton5.setIcon(ICONE_5_ON);
            fenetre = fenetre5;
        }
        if (e.getSource() == boutonNom) {
            boutonNom.setForeground(Color.WHITE);
            JTextArea zoneTexte = new JTextArea(1, 10);
            zoneTexte.setFont(new Font("Open Sans", Font.BOLD, 22));
            JOptionPane.showMessageDialog(this, "Veuillez entrer votre ID utilisateur.");
            switch (JOptionPane.showConfirmDialog(null, new JScrollPane(zoneTexte))) {
                case JOptionPane.OK_OPTION:
                    Main.setNewUser(Integer.parseInt(zoneTexte.getText()));
                    break;
            }
            boutonNom.setForeground(Color.BLACK);
        }
        if (e.getSource() == boutonQuitter) {
            System.exit(0);
        }
        conteneur.add(fenetre);
        conteneur.validate();
        conteneur.repaint();

    }

    public void iconesOff() {
        bouton1.setIcon(ICONE_1_OFF);
        bouton2.setIcon(ICONE_2_OFF);
        bouton3.setIcon(ICONE_3_OFF);
        bouton4.setIcon(ICONE_4_OFF);
        bouton5.setIcon(ICONE_5_OFF);
    }

}
