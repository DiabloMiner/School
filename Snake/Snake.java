import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public class Snake extends PublicActor {
    
    private List<SnakePart> parts;
    private int rotation;
    
    public Snake(List<SnakePart> parts) {
        this.parts = parts;
        rotation = 0;
        this.setRotation(rotation);
    }
    
    public Snake(List<SnakePart> parts, int rotation) {
        this.parts = parts;
        this.rotation = rotation;
        this.setRotation(rotation);
    }
    
    public void pollInputs() {
        // Poll key inputs
        if (Greenfoot.isKeyDown("up") || Greenfoot.isKeyDown("w")) {
            rotation = 270;
        }
        if (Greenfoot.isKeyDown("down") || Greenfoot.isKeyDown("s")) {
            rotation = 90;
        }
        if (Greenfoot.isKeyDown("left") || Greenfoot.isKeyDown("a")) {
            rotation = 180;
        }
        if (Greenfoot.isKeyDown("right") || Greenfoot.isKeyDown("d")) {
            rotation = 0;
        }
        this.setRotation(rotation);
    }
    
    public void move() {
        // TODO: Sometimes apples just appear in snakes & some snake parts are spawned incorrectly in some scenarios & restrict movement
        // TODO: Add grid
        
        // Move snake and update part rotations
        if (true) {
            // Move snake
            move(1);
            for (SnakePart part : parts) {
                part.move(1);
            }
        
            // Update part rotations
            for (int i = (parts.size() - 1); i >= 0; i--) {
                if (i == 0) {
                    parts.get(i).setRotation(this.rotation);
                } else {
                    parts.get(i).setRotation(parts.get(i - 1).getRotation());
                }
            }
        }
    }
    
    public Actor getOneObjectAtOffsetP(java.lang.Class<?> cls) {
        return this.getOneObjectAtOffsetP((int) Math.cos(Math.toRadians(rotation)), (int) Math.sin(Math.toRadians(rotation)), cls);
    }

    public boolean isLeavingEdge() {
        if (getX() <= 0) {
            return rotation == 180;
        } else if (getX() >= MyWorld.size) {
            return rotation == 0;
        } else if (getY() <= 0) {
            return rotation == 270;
        } else if (getY() >= MyWorld.size) {
            return rotation == 90;
        }
        return false;
    }
    
    public List<SnakePart> getParts() {
        return parts;
    }

}
