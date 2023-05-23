public class Vector  
{
    double x,y;

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
    
    public Vector mul(double d)
    {
        x*=d;
        y*=d;
        return this;
    }
    
    public double dot(Vector v)
    {
        return (this.x*v.x+this.y*v.y);
    }
    
    public double length() {
        return (Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
    }
  
}
