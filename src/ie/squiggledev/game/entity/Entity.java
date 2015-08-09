package ie.squiggledev.game.entity;

import ie.squiggledev.game.gfx.Screen;
import ie.squiggledev.game.level.Level;

public abstract class Entity {
    public int x;
    public int y;
    protected Level level;
    
    public Entity(Level level) {
        this.init(level);
    }
    
    public final void init(Level level) {
        this.level = level;
    }
    
    public abstract void tick();
    
    public abstract void render(Screen screen);
}