import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class BotKido {
	
	private BufferedImage screen;
	private Robot robo;
	private int state;
	private int period;
	private Color wood1;
	private Color wood2;
	private Color wood3;
	private Color wood4;
	private Color wood5;
	private Color wood6;
	private Color stone1;
	private Color stone2;
	private Color stone3;
	private ArrayList<Color> dangerousMaterials;
	private Color indicator;
	Instant press;
	Instant tick;
	Instant change;
	
	public BotKido() throws AWTException {
		robo = new Robot();
		state = 0;
		dangerousMaterials = new ArrayList<Color>();
		
		wood1 = new Color(165, 95, 51);
		wood2 = new Color(179, 144, 119);
		wood3 = new Color(209, 137, 93);
		wood4 = new Color(135, 99, 82);
		wood5 = new Color(128, 113, 129);
		wood6 = new Color(169, 150, 170);
		stone1 = new Color(93, 101, 125);
		stone2 = new Color(105, 114, 142);
		stone3 = new Color(72, 80, 103);
		
		dangerousMaterials.add(wood1);
		dangerousMaterials.add(wood2);
		dangerousMaterials.add(wood3);
		dangerousMaterials.add(wood4);
		dangerousMaterials.add(wood5);
		dangerousMaterials.add(wood6);
		dangerousMaterials.add(stone1);
		dangerousMaterials.add(stone2);
		dangerousMaterials.add(stone3);
		
		indicator = new Color(44, 55, 64);
		period = 70;
		press = Instant.now();
		tick = Instant.now();
		change = Instant.now();
	}
	
	public void run() {
		System.out.println("run");
		while( true) {
			try {
				loop();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean isSame(Color a, Color b) {
		double dist = 0;
		dist += (a.getRed() - b.getRed())*(a.getRed() - b.getRed());
		dist += (a.getGreen() - b.getGreen())*(a.getGreen() - b.getGreen());
		dist += (a.getBlue() - b.getBlue())*(a.getBlue() - b.getBlue());
		dist = Math.sqrt(dist);
		if( dist < 4) {
			return true;
		}
		return false;
	}
	
	private Color getPixelColor(int x, int y) {
		int rgb = screen.getRGB(x, y);
		return new Color(rgb);
	}
	
	private boolean isValid() {
		Color v = getPixelColor(1000, 133);
		if( isSame(v, indicator)) {
			if( state == 2)
				state = 0;
			return true;
		}
		return false;
	}
	
	
	private boolean isDangerousMaterial( Color c) {
		for( Color d : dangerousMaterials) {
			if( isSame(d, c)) return true;
		}
		return false;
	}

	private boolean isDangerous() {
		int y = 640;
		int x = 0;
		if( state == 0) {
			x = 900;
		}
		if( state == 1) {
			x = 1020;
		}
		
		for( int i = -50; i < 10; i++) {
			Color cur = getPixelColor(x, y + i);
			if( isDangerousMaterial(cur)) {
				System.out.println("danger");
				return true;
			}
		}
		
		return false;
	}
	
	private void reportState() {
		if( Duration.between(tick, Instant.now()).toMillis() > 1000) {
			System.out.println("state: " + state);
			tick = Instant.now();
		}
	}
	
	private void performAction() throws InterruptedException {
		if( state == 1) {
			if( Duration.between(press, Instant.now()).toMillis() > period) {
				robo.keyPress( KeyEvent.VK_RIGHT);
				TimeUnit.MILLISECONDS.sleep(20);
				robo.keyRelease( KeyEvent.VK_RIGHT);
				System.out.println("right");
				press = Instant.now();
			}
		} else if ( state == 0){
			if( Duration.between(press, Instant.now()).toMillis() > period) {
				robo.keyPress( KeyEvent.VK_LEFT);
				TimeUnit.MILLISECONDS.sleep(20);
				robo.keyRelease( KeyEvent.VK_LEFT);
				System.out.println("left");
				press = Instant.now();
			}
		} else {
			robo.keyRelease( KeyEvent.VK_RIGHT);
			robo.keyRelease( KeyEvent.VK_LEFT);
		}
	}
	
	private void loop() throws InterruptedException {
		screen = robo.createScreenCapture(new Rectangle(1920,1080));
		if( !isValid()) state = 2;
		
		if( state == 0) {
			if( isDangerous() && Duration.between(change, Instant.now()).toMillis() > period*1.5) {
				state = 1;
				change = Instant.now();
			}
		} else if (state == 1) {
			if( isDangerous() && Duration.between(change, Instant.now()).toMillis() > period*1.5) {
				state = 0;
				change = Instant.now();
			}
		} 
		
		if( state != 2)
			performAction();
		
		reportState();
	}
}
