import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class MyWorld extends World
{   
    public MyWorld()
    {    
        // Create a new world with 800x800 cells with a cell size of 1x1 pixels.
        super(800, 800, 1);
        
        // The four objects of the game are created and added to the game
        Ball ball = new Ball(getWidth()/2, getHeight()/2, 360);
        Paddle leftPaddle = Paddle.createKeyboardControlledPaddle("w", "s");
        Paddle rightPaddle = Paddle.createMouseControlledPaddle();
        Score score = new Score();
        
        addObject(ball, getWidth()/2, getHeight()/2);
        addObject(leftPaddle, 60, getHeight()/2);
        addObject(rightPaddle, getWidth() - 60, getHeight()/2);
        addObject(score, getWidth()/2, score.FONT_SIZE);
        
        Vector v1 = new Vector(1,2);
        Vector v2 = new Vector(2,1);
        
        System.out.println(v1.getPhiRadians());
        System.out.println(v1.getPhiDegrees());
        
        // Boolean shouldSkipVaribalePrint um anzuzeigen wann i nicht geprintet werden soll
        // For Loop der mit der Variable i bei 0 beginnt und jede Runde eins dazu bekommt bis 99
        // shouldSkipVariablePrint wird auf falsch gesetzt
        // Abfrage ob i % 3 gleich 0 ist, dann: Herausprinten von Fizz mit println("Fizz"), dann shouldSkipVariablePrint wird auf wahr gesetzt
        // Abfrage ob i % 5 gleich 0 ist, dann: falls shouldSkipVariablePrint wahr: Herausprinten von Buzz mit print("Buzz"); falls nicht: Herausprinten von Buzz mit println("Buzz"), dann shouldSkipVariablePrint wird auf wahr gesetzt
        // Abfrage ob shouldSkipVariablePrint falsch ist, falls ja: In neuer Zeile soll Variable geprintet werden mit: println; falls nein: mach nichts
        // boolean shouldSkipVariablePrint = false;
        // for (int i = 0; i < 100; i++) {
            // shouldSkipVariablePrint = false;
            // if (i % 3 == 0) {
                // // Fehlerhaft sollte Fizz mit print in die Konsole printen
                // System.out.println("Fizz");
                // shouldSkipVariablePrint = true;
            // }
            // if (i % 5 == 0) {
                // // Fehlerhaft sollte immer Buzz mit print in die Konsole printen
                // if (shouldSkipVariablePrint == true) {
                    // System.out.print("Buzz");
                // } else {
                    // System.out.println("Buzz");
                // }
                // shouldSkipVariablePrint = true;
            // }
            // if (shouldSkipVariablePrint == false) {
                // System.out.println(i);
            // }
            // // Fehlerhaft falls nein sollte das sein: printe eine leere Zeile mit println()
        // }
        boolean shouldSkipVariablePrint = false;
        for (int i = 0; i < 16; i++) {
            shouldSkipVariablePrint = false;
            if ((i % 3 == 0) && i != 0) {
                System.out.print("Fizz");
                shouldSkipVariablePrint = true;
            }
            if ((i % 5 == 0) && i != 0) {
                System.out.print("Buzz");
                shouldSkipVariablePrint = true;
            }
            if (shouldSkipVariablePrint == false) {
                System.out.println(i);
            } else {
                System.out.println();
            }
        }
        
       
        
       
    }

}
