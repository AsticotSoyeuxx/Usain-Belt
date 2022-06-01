package usainbelt.affichage;
import java.awt.*;

public class JGraphECG extends JGraph {
    final static Color COULEUR_MODERE = Color.BLUE;
    final static Color COULEUR_AEROBIE = new Color(0,150,0);
    final static Color COULEUR_ANAEROBIE = Color.MAGENTA;
    final static Color COULEUR_VO2_MAX = Color.RED;
    final float FCmax;
    
    public JGraphECG(String titre, int valeurMin, int valeurMax, int FC) {
        super(titre, COULEUR_MODERE, valeurMin, valeurMax);
        FCmax = (float) FC;
    }

    @Override
    public void setCouleur(Graphics2D g, int v) {
        if (v > 0.9*FCmax) {
            g.setColor(COULEUR_VO2_MAX);
        } 
        else if (v > 0.8*FCmax) {
            g.setColor(COULEUR_VO2_MAX);
        } 
        else if (v > 0.65*FCmax) {
            g.setColor(COULEUR_AEROBIE);
        } else {
            g.setColor(COULEUR_MODERE);
        }
    }

}
