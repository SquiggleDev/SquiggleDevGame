package ie.squiggledev.game.level;

import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

import ie.squiggledev.game.gfx.Screen;
import ie.squiggledev.game.level.tile.Tile;
import ie.squiggledev.game.entity.Entity;



public class Level {
    private byte[] tiles;
    public int width;
    public int height;
    public List<Entity> entities = new ArrayList<Entity>();
    private String imagePath;
    private BufferedImage image;
    
    public Level(String imagePath) {
        if (imagePath != null) {
            this.imagePath = imagePath;
            this.loadLevelFromFile();
        } else {
            this.width = 64;
            this.height = 54;
            this.tiles = new byte[this.width * this.height];
            this.generateLevel();
        }
    }
    
    private void loadLevelFromFile() {
        try {
            this.image = ImageIO.read(Level.class.getResource(this.imagePath));
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.tiles = new byte[this.width * this.height];
            this.loadTiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadTiles() {
        int[] tileColours = this.image.getRGB(0, 0, this.width, this.height, null, 0, this.width);
        
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                tileCheck: for (Tile t : Tile.tiles) {
                    if (t != null && t.getLevelColour() == tileColours[x + y * this.width]) {
                        this.tiles[x + y * this.width] = t.getId();
                        break tileCheck;
                    }
                }
            }
        }
        
    }
    
    private void saveLevelToFile() {
        try {
            ImageIO.write(image, "png", new File(Level.class.getResource(this.imagePath).getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void alterTile(int x, int y, Tile newTile) {
        this.tiles[x + y * width] = newTile.getId();
        image.setRGB(x, y, newTile.getLevelColour());
    }
    
    public void generateLevel() {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                if (x * y % 10 < 7) {
                    this.tiles[x + y * this.width] = Tile.GRASS.getId();
                } else {
                    this.tiles[x + y * this.width] = Tile.STONE.getId();
                }
            }
        }
    }
    
    public void tick() {
        for(Entity e : entities) {
            e.tick();
        }
        
        for (Tile t : Tile.tiles) {
            if (t == null) {
                break;
            } else {
                t.tick();
            }
        }
    }
    
    public void renderTiles(Screen screen, int xOffset, int yOffset) {
        if (xOffset < 0) {
            xOffset = 0;
        }
        if (xOffset > ((this.width << 3) - screen.width)) {
            xOffset = ((this.width << 3) - screen.width);
        }
        if (yOffset < 0) {
            yOffset = 0;
        }
        if (yOffset > ((this.height << 3) - screen.height)) {
            yOffset = ((this.height << 3) - screen.height);
        }
        
        screen.setOffset(xOffset, yOffset);
        
        for (int y = (yOffset >> 3); y < (yOffset + screen.height >> 3) + 1; y++) {
            for (int x = (xOffset >> 3); x < (xOffset + screen.width >> 3) + 1; x++) {
                getTile(x, y).render(screen, this, x << 3, y << 3);
            }
        }
    }
    
    public void renderEntities(Screen screen) {
        for(Entity e : entities) {
            e.render(screen);
        }
    }
    
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
            return Tile.VOID;
        } else {
            return Tile.tiles[tiles[x + y * width]];
        }
        
    }
    
    public void addEntity(Entity entity) {
        this.entities.add(entity);
    }
}