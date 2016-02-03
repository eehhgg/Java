package basic;

public class Figure implements Cloneable {
    public static final int POSITION_THRESHOLD = 5;
    public static final float SCALE_THRESHOLD = 0.01f;
    public static final int ROTATION_THRESHOLD = 10;
    public static final int UNDEFINED_ID = -1;
    private String name;
    private int id, positionX, positionY;   // position of the center
    private float scaleX, scaleY, rotation;   // rotation is in [0,360)
    private boolean reflectX;
    private Diagram diagram;
    private int index;
    
    public Figure() {
        name = null;   id = UNDEFINED_ID;
        positionX = 0;   positionY = 0;   scaleX = 1f;   scaleY = 1f;
        rotation = 0f;   reflectX = false;   diagram = null;   index = -1;
    }

    public void setId(int id) {
        if (id < UNDEFINED_ID) {
            throw new IllegalArgumentException("Invalid figure id: " + id);
        }
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDiagram(Diagram d) {
        diagram = d;
    }

    public void setIndex(int i) {
        index = i;
    }

    public void setPositionX(int p) {
        positionX = p;
    }

    public void setPositionY(int p) {
        positionY = p;
    }

    public void setScaleX(float s) {
        if (s < SCALE_THRESHOLD) { scaleX = SCALE_THRESHOLD; }
        else { scaleX = s; }
    }

    public void setScaleY(float s) {
        if (s < SCALE_THRESHOLD) { scaleY = SCALE_THRESHOLD; }
        else { scaleY = s; }
    }

    public void setRotation(float r) {
        float r1 = Math.abs(r) % 360;
        if (r < 0) { r1 = 360 - r1; }
        rotation = r1;
    }

    public void setReflectX(boolean b) {
        reflectX = b;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Diagram getDiagram() {
        return diagram;
    }

    public int getIndex() {
        return index;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getRotation() {
        return rotation;
    }

    public boolean getReflectX() {
        return reflectX;
    }

    @Override
    public Object clone() {
        Object o;
        try { o = super.clone(); }
        catch (Exception e) { throw new RuntimeException("Unable to clone Figure"); }
        return o;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }
        if ( !(o instanceof Figure) ) { return false; }
        Figure f = (Figure) o;
        float rotationD = Math.abs(rotation-f.rotation);
        rotationD = Math.min(rotationD, 360-rotationD);
        return ( name.equals(f.name)
                && (Math.abs(positionX-f.positionX) <= POSITION_THRESHOLD)
                && (Math.abs(positionY-f.positionY) <= POSITION_THRESHOLD)
                && (Math.abs(scaleX-f.scaleX) <= SCALE_THRESHOLD)
                && (Math.abs(scaleY-f.scaleY) <= SCALE_THRESHOLD)
                && (rotationD <= ROTATION_THRESHOLD)
                && (reflectX == f.reflectX) );
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + this.positionX;
        hash = 53 * hash + this.positionY;
        hash = 53 * hash + Float.floatToIntBits(this.scaleX);
        hash = 53 * hash + Float.floatToIntBits(this.scaleY);
        hash = 53 * hash + Float.floatToIntBits(this.rotation);
        hash = 53 * hash + (this.reflectX ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(diagram.getLabel()).append(", figure").append(" ").append(id);
        sb.append(": ").append(name).append(", ");
        sb.append(positionX).append(":").append(positionY).append(", ");
        sb.append(scaleX).append("|").append(scaleY).append(", ");
        sb.append(rotation).append(", ").append(reflectX);
        return sb.toString();
    }
    
}
