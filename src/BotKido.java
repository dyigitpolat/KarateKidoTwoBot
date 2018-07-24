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
	private ArrayList<Color> dangerousMaterials;
	private Color indicator;
	Instant press;
	Instant tick;
	Instant change;
	
	public BotKido() throws AWTException {
		robo = new Robot();
		state = 0;
		dangerousMaterials = new ArrayList<Color>();
		Color normalWoodLeft;
		Color normalWoodMid;
		Color normalWoodRight;
		Color lightWoodLeft;
		Color lightWoodMid;
		Color lightWoodRight;
		Color darkWoodLeft;
		Color darkWoodMid;
		Color darkWoodRight;
		Color stoneLeft;
		Color stoneMid;
		Color stoneMid2;
		Color stoneRight;
		
		normalWoodLeft = new Color(165, 95, 51);
		normalWoodMid = new Color(179, 144, 119);
		normalWoodRight = new Color(209, 137, 93);
		lightWoodLeft = new Color(135, 99, 82);
		lightWoodMid = new Color(128, 113, 129);
		lightWoodRight = new Color(169, 150, 170);
		stoneLeft = new Color(93, 101, 125);
		stoneMid = new Color(105, 114, 142);
		stoneMid2 = new Color(72, 80, 103);
		
		dangerousMaterials.add(normalWoodLeft);
		dangerousMaterials.add(normalWoodMid);
		dangerousMaterials.add(normalWoodRight);
		dangerousMaterials.add(lightWoodLeft);
		dangerousMaterials.add(lightWoodMid);
		dangerousMaterials.add(lightWoodRight);
		dangerousMaterials.add(stoneLeft);
		dangerousMaterials.add(stoneMid);
		dangerousMaterials.add(stoneMid2);
		
		indicator = new Color(44, 55, 64);
		period = 80;
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
	
	private double getHue( Color c) {
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0] * 360;
	}
	
	private double getSaturation( Color c) {
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[1] * 100;
	}
	
	private double getBrightness( Color c) {
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2] * 100;
	}
	
	private boolean isSame(Color a, Color b) {
		double dist = 0;
		dist += (a.getRed() - b.getRed())*(a.getRed() - b.getRed());
		dist += (a.getGreen() - b.getGreen())*(a.getGreen() - b.getGreen());
		dist += (a.getBlue() - b.getBlue())*(a.getBlue() - b.getBlue());
		dist = Math.sqrt(dist);
		
		dist += Math.abs(getHue(a) - getHue(b)) * 5;
		dist += Math.abs(getSaturation(a) - getSaturation(b));
		if( dist < 10) {
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
		int x1 = 0;
		int x2 = 0;
		int x3 = 0;
		int x4 = 0;
		int x5 = 0;
		if( state == 0) {
			x1 = 929;
			x2 = 890;
			x3 = 900;
			x4 = 920;
			x5 = 865;
		}
		if( state == 1) {
			x1 = 990;
			x2 = 1030;
			x3 = 1020;
			x4 = 1000;
			x5 = 1055;
		}
		
		for( int i = -50; i < 10; i++) {
			Color cur1 = getPixelColor(x1, y + i);
			Color cur2 = getPixelColor(x2, y + i);
			Color cur3 = getPixelColor(x3, y + i);
			Color cur4 = getPixelColor(x4, y + i);
			Color cur5 = getPixelColor(x5, y + i);
			if( (i>-20 && getBrightness(cur1) < 32) || isDangerousMaterial(cur2) ||
				isDangerousMaterial(cur3) || isDangerousMaterial(cur4) ||
				isDangerousMaterial(cur5)) {
				//System.out.println("danger");
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
