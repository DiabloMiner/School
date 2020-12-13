public class Vector  
{
    double x,y;
    
    public static Vector add(Vector v1, Vector v2)
    {
        Vector result = new Vector(v1.x+v2.x,v1.y+v2.y);
        return result;
    }
    
    public static Vector sub(Vector v1, Vector v2)
    {
        Vector result = new Vector(v1.x-v2.x,v1.y-v2.y);
        return result;
    }
    
    public static Vector mult(Vector v,double d)
    {
        v.x*=d;
        v.y*=d;
        return v;
    }
    
    public static double dot(Vector v1, Vector v2)
    {
        double result = v1.x*v2.x + v1.y+v2.y;
        return result;
    }
    
    public static double getRadius(Vector v) {
        return (Math.sqrt(Math.pow(v.x,2) + Math.pow(v.y,2)));
    }
    
    public static double getPhiRadians(Vector v) {
        return Math.atan(v.y/v.x);
    }
    
    public static double getPhiDegrees(Vector v) {
        return Math.toDegrees(Math.atan(v.y/v.x));
    }

    public Vector()
    {
        x=0;
        y=0;
    }
    
    public Vector(double x_new, double y_new)
    {
        x=x_new;
        y=y_new;
    }
    
    public void set(double x_new, double y_new)
    {
        x=x_new;
        y=y_new;
    }
    
    public Vector add(Vector v)
    {
        x+=v.x;
        y+=v.y;
        return this;
    }
    
    public Vector sub(Vector v)
    {
        x-=v.x;
        y-=v.y;
        return this;
    }
    
    public Vector mult(double d)
    {
        x*=d;
        y*=d;
        return this;
    }
    
    public double dot(Vector v)
    {
        return (this.x*v.x+this.y*v.y);
    }
    
    public double getRadius() {
        return (Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
    }
    
    public double getPhiRadians() {
        return Math.atan(y/x);
    }
    
    public double getPhiDegrees() {
        return Math.toDegrees(Math.atan(y/x));
    }
  
}
