package usainbelt.affichage;
public class Course {
    private String date;
    private float distance;
    private int duree;

    public Course(String da, float di, int du) {
        date = da;
        distance = di;
        duree = du;
    }

    public String getDate() {
        return date;
    }

    public float getDistance() {
        return distance;
    }
    
    public int getDuree() {
        return duree;
    }

}