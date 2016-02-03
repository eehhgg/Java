package rule;

import basic.Figure;

public class TransformationParameters implements Cloneable {
    private int dx, dy;
    private float sx, sy, r;
    private boolean rX;

    public TransformationParameters() {
        setValues(0,0, 1f,1f, 0f, false);
    }

    public void setValues(int dx, int dy, float sx, float sy, float r, boolean reflectX) {
        if (sx < Figure.SCALE_THRESHOLD) { sx = Figure.SCALE_THRESHOLD; }
        if (sy < Figure.SCALE_THRESHOLD) { sy = Figure.SCALE_THRESHOLD; }
        this.dx = dx;   this.dy = dy;   this.sx = sx;   this.sy = sy;
        this.r = r;   this.rX = reflectX;
    }

    public void transform(Figure f) {
        f.setPositionX(f.getPositionX() + dx);   f.setPositionY(f.getPositionY() + dy);
        f.setScaleX(f.getScaleX() * sx);   f.setScaleY(f.getScaleY() * sy);
        f.setRotation(f.getRotation() + r);
        if (rX) { f.setReflectX( !f.getReflectX() ); }
    }

    public void transform(TransformationParameters p) {
        p.dx += dx;   p.dy += dy;   p.sx *= sx;   p.sy *= sy;   p.r += r;
        if (p.sx < Figure.SCALE_THRESHOLD) { p.sx = Figure.SCALE_THRESHOLD; }
        if (p.sy < Figure.SCALE_THRESHOLD) { p.sy = Figure.SCALE_THRESHOLD; }
        if (rX) { p.rX = !p.rX; }
    }

    @Override
    public Object clone() {
        TransformationParameters p;
        try { p = (TransformationParameters) super.clone(); }
        catch (Exception e) { throw new RuntimeException("Unable to clone Transformation"); }
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }
        if ( !(o instanceof TransformationParameters) ) { return false; }
        TransformationParameters p = (TransformationParameters) o;
        float rotationD = Math.abs(r-p.r);
        rotationD = Math.min(rotationD, 360-rotationD);
        return ( (Math.abs(dx-p.dx) <= Figure.POSITION_THRESHOLD)
                && (Math.abs(dy-p.dy) <= Figure.POSITION_THRESHOLD)
                && (Math.abs(sx-p.sx) <= Figure.SCALE_THRESHOLD)
                && (Math.abs(sy-p.sy) <= Figure.SCALE_THRESHOLD)
                && (rotationD <= Figure.ROTATION_THRESHOLD)
                && (rX == p.rX) );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.dx;
        hash = 83 * hash + this.dy;
        hash = 83 * hash + Float.floatToIntBits(this.sx);
        hash = 83 * hash + Float.floatToIntBits(this.sy);
        hash = 83 * hash + Float.floatToIntBits(this.r);
        hash = 83 * hash + (this.rX ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("transform (").append(dx).append(":").append(dy).append(", ");
        sb.append(sx).append("|").append(sy).append(", ");
        sb.append(r).append(", ").append(rX).append(")");
        return sb.toString();
    }
    
}
