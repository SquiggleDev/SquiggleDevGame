package ie.squiggledev.game;

import ie.squiggledev.engine.input.InputHandler;
import ie.squiggledev.game.gfx.SpriteSheet;
import ie.squiggledev.game.gfx.Screen;
import ie.squiggledev.game.gfx.Colour;
import ie.squiggledev.game.gfx.Font;
import ie.squiggledev.game.level.Level;
import ie.squiggledev.game.entity.Player;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

/**
 * Game class.
 *
 * Implements the game loop.
 * @author SquiggleDev.
 **/
public class Game extends Canvas implements Runnable {
    public static final int WIDTH = 160;
    public static final int HEIGHT = WIDTH / 12 * 9;
    public static final int SCALE = 3;
    public static final String NAME = "Game";
    public static final String RES = "/res/";
    public boolean running = false;
    public int tickCount = 0;
    public InputHandler input;
    public Level level;
    public Player player;
    
    private static final long serialVersionUID = 1L;
    private JFrame frame;    
    private BufferedImage image = new BufferedImage(WIDTH, 
                                                    HEIGHT, 
                                                    BufferedImage.TYPE_INT_RGB);
    private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
        .getData();
    private int[] colours = new int[6 * 6 * 6];
    private Screen screen;
    
    public Game() {
        setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        
        frame = new JFrame(NAME);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        frame.add(this, BorderLayout.CENTER);
        frame.pack();
        
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    public void init() {
        int index = 0;
        for (int r = 0; r < 6; r++) {
            for( int g = 0; g < 6; g++) {
                for (int b = 0; b < 6; b++) {
                    // shades 0 -> 5
                    int rr = (r * 255 / 5);
                    int gg = (g * 255 / 5);
                    int bb = (b * 255 / 5);
                    
                    colours[index++] = rr << 16 | gg << 8 | bb;
                    // 255 as transparent colour
                }
            }
        }
    
        this.screen = new Screen(WIDTH, HEIGHT, new SpriteSheet(RES + "sprite_sheet.png"));
        this.input = new InputHandler(this);
        this.level = new Level(RES + "level/water_test_level.png");
        this.player = new Player(level, 0, 0, input);
        level.addEntity(player);
    }
    
    public synchronized void start() {
        running = true;
        new Thread(this).start();
    }
    
    public synchronized void stop() {
        running = false;
    }
    
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000D/60D;
        
        int ticks = 0;
        int frames = 0;
        
        long lastTimer = System.currentTimeMillis();
        double delta = 0;
        
        init();
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            boolean shouldRender = true;
            
            while (delta >= 1) {
                ticks++;
                tick();
                delta -= 1;
                shouldRender = true;
            }
            
            try {
                Thread.sleep(2);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
            
            if (shouldRender) {
                frames++;
                render();
            }
            
            if (System.currentTimeMillis() - lastTimer >= 1000) {
               lastTimer += 1000;
               System.out.println(ticks + " ticks, " + frames + " frames");
               frames = 0;
               ticks = 0;
            }
        }
    }
    
    public void tick() {
        tickCount++;
        level.tick();
    }
    
    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3); // triple buffering - the higher it is
                                     // reduces tearing but requires more 
                                     // power.
            return;
        }
        
        int xOffset = player.x - (screen.width / 2);
        int yOffset = player.y - (screen.height / 2);
        
        level.renderTiles(screen, xOffset, yOffset);
        
       
        
        
        level.renderEntities(screen);
        
        for (int y = 0; y < screen.height; y++) {
            for (int x = 0; x < screen.width; x++) {
                int colourCode = screen.pixels[x + y * screen.width];
                if (colourCode < 255) {
                    pixels[x + y * WIDTH] = colours[colourCode];
                }
            }
        }
        
        Graphics g = bs.getDrawGraphics();        
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
        bs.show();
    }

    public static void main(String[] args) {
        new Game().start();
    }
}
