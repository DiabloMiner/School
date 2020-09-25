import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public abstract class Paddle extends Actor
{
    public static final int WIDTH = 60;
    public static final int HEIGHT = 20;
    
    public enum controlScheme {
        
    }
    
    public Paddle() {
        draw();
        setRotation(90);
    }
    
    public abstract void act();
    
    public void handleIntersections() {
        if (intersects(getWorld().getObjects(Ball.class).get(0))) {
            getWorld().getObjects(Ball.class).get(0).changeDirectionX();
            getWorld().getObjects(Ball.class).get(0).changeDirectionY();
        }
    }
    
    
    public void draw() {
        GreenfootImage image = new GreenfootImage(WIDTH, HEIGHT);
        image.drawRect(0, 0, WIDTH, HEIGHT);
        image.setColor(Color.CYAN);
        image.fill();
        setImage(image);
    }
    
    public static Paddle createKeyboardControlledPaddle(String upKey, String downKey) {
        return new Paddle() {
            public void act(){   
                handleIntersections();
                if (Greenfoot.isKeyDown(upKey)) {
                    move(-10);
                }
                if (Greenfoot.isKeyDown(downKey)) {
                    move(10);
                }
            }  
        };
    }
    
    public static Paddle createMouseControlledPaddle() {
        return new Paddle() {
            public void act() {   
                handleIntersections();
                if (Greenfoot.getMouseInfo() != null) {
                    if (Greenfoot.getMouseInfo().getY() <= getY()) {
                        move(-10);
                    }
                    if (Greenfoot.getMouseInfo().getY() >= getY()) {
                        move(10);
                    }
                }
            }
        };
    }
}
