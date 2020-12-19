package com.diablominer.opengl.main;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyLogicalEngine extends LogicalEngine {

    private AtomicBoolean shouldRun = new AtomicBoolean();

    public MyLogicalEngine(boolean shouldRun) {
        this.shouldRun.set(shouldRun);
    }

    @Override
    public void run() {
        while (shouldRun.get()) {
            updateAllGameObjects();

            if (!shouldRun.get()) {
                break;
            }
            try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void setShouldRun(boolean bool) {
        shouldRun.set(bool);
    }

    public boolean getShouldRun() {
        return shouldRun.get();
    }
}
