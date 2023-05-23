import greenfoot.*;

public class Pixel extends Actor {
    
    public int n;
    
    public Pixel(int x, int y, double cRe, double cIm, double maxMagnitude, int maxIterations) {
        int i = 0;
        double zRe  = 0, zIm = 0, magnitude = 0.0;
        while (magnitude <= maxMagnitude && i < maxIterations) {
            zRe = (zRe * zRe) - (zIm * zIm) + cRe;
            zIm = 2 * zRe * zIm + cIm;
            magnitude = (zRe * zRe) + (zIm * zIm);
            i++;
        }
        this.n = i;
    }
    
    
}
