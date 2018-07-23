import java.awt.AWTException;

public class KarateMaster {

	public static void main( String[] args) {
		try {
			System.out.println("init");
			BotKido bk = new BotKido();
			bk.run();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
