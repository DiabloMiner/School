import greenfoot.*;

public class PublicActor extends Actor {
    
    public Actor getOneObjectAtOffsetP(int dx, int dy, java.lang.Class<?> cls) {
        return this.getOneObjectAtOffset(dx, dy, cls);
    }
    
    public boolean isTouchingP(java.lang.Class<?> cls) {
        return this.isTouching(cls);
    }
    
    public void removeTouchingP(java.lang.Class<?> cls) {
        this.removeTouching(cls);
    }
    
    public <A> java.util.List<A> getIntersectingObjectsP(java.lang.Class<A> cls) {
        return this.getIntersectingObjects(cls);
    }
    
    public Actor getOneIntersectingObjectP(java.lang.Class<?> cls) {
        return this.getOneIntersectingObject(cls);
    }
    
}
