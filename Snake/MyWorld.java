import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public class MyWorld extends World {
    
    public static final int snakeStartingRotation = 270;
    public static final int size = 20;
    
    private Snake snake;

    public MyWorld() {    
        // Create a new world with 20x20 cells with a cell size of 40x40 pixels.
        super(size, size, 40);
        
        // Add snake parts
        List<SnakePart> snakeParts = new ArrayList<>();
        for (int i = 1; i <= 1; i++) {
            SnakePart snakePart = new SnakePart(snakeStartingRotation);
            this.addObject(snakePart, (getWidth() / 2) + (int) Math.cos(Math.toRadians(snakeStartingRotation)) * i, (getHeight() / 2) + (int) -Math.sin(Math.toRadians(snakeStartingRotation)) * i);
            snakeParts.add(snakePart);
        }
        
        // Add snake
        snake = new Snake(snakeParts, snakeStartingRotation);
        this.addObject(snake, getWidth() / 2, getHeight() / 2);
        
        // Add apples
        for (int i = 1; i <= (int) Math.round(Math.random() * 3 + 1); i++) {
            Apple apple = new Apple();
            // double x = Math.random() * (0.5 * size - 2) + 2, y = Math.random() * (0.5 * size - 2) + 2, sign = (int) Math.pow(-1, Math.round(Math.random() * 1));
            // this.addObject(apple, (getWidth() / 2) + (int) Math.round(sign * x), (getHeight() / 2) + (int) Math.round(sign * y));
            int x = (int) Math.round(Math.random() * (size - 1)), y = (int) Math.round(Math.random() * (size - 1));
            if (this.getObjectsAt(x,y, SnakePart.class).isEmpty() && this.getObjectsAt(x, y, Snake.class).isEmpty()) {
                this.addObject(apple, x, y);
            } else {
                i--;
            }
        }
    }
    
    public void act() {
        // Poll key inputs
        snake.pollInputs();
        
        // Perform collision detection and response
        if (snake.getOneObjectAtOffsetP(Apple.class) != null) {
            this.removeObject(snake.getOneObjectAtOffsetP(Apple.class));
            addApple();
            
            List<SnakePart> parts = snake.getParts();
            SnakePart lastPart = parts.get(parts.size() - 1);
            SnakePart newPart = new SnakePart(lastPart.rotation);
            this.addObject(newPart, lastPart.getX() + (int) Math.cos(Math.toRadians(lastPart.rotation)), lastPart.getY() + (int) -Math.sin(Math.toRadians(lastPart.rotation)));
            parts.add(newPart);
        }
        if (snake.getOneObjectAtOffsetP(SnakePart.class) != null || snake.isLeavingEdge()) {
            endGame();
        }
        
        // Move snake
        snake.move();
    }
    
    public void addApple() {
         Apple apple = new Apple();
         // double x = Math.random() * (0.5 * size - 2) + 2, y = Math.random() * (0.5 * size - 2) + 2, sign = (int) Math.pow(-1, Math.round(Math.random() * 1));
         // world.addObject(apple, (world.getWidth() / 2) + (int) Math.round(sign * x), (world.getHeight() / 2) + (int) Math.round(sign * y));
         int x = (int) Math.round(Math.random() * (size - 1)), y = (int) Math.round(Math.random() * (size - 1));
         if (this.getObjectsAt(x,y, SnakePart.class).isEmpty() && this.getObjectsAt(x, y, Snake.class).isEmpty()) {
             this.addObject(apple, x, y);
         } else {
             addApple();
         }
    }
    
    public void endGame() {
       // End simulation and show "Game Over" text
       GameOver endText = new GameOver();
       this.addObject(endText, this.getWidth() / 2, this.getHeight() / 2);
       this.removeObjects(this.getObjects(SnakePart.class));
       this.removeObjects(this.getObjects(Snake.class));
       this.removeObjects(this.getObjects(Apple.class));
       Greenfoot.stop();
    }
}
