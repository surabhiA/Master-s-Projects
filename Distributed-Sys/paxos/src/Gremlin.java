import java.util.Random;

/* 
 *  This class introduces random failures by randomly stopping the thread.
 */
public class Gremlin  extends Thread {
	public static class Lookout {
		public volatile boolean safe;
	}
	
	Thread victim;
	private volatile Lookout lookout;
	private final static int uptimePercent = 90; //percent chance that a thread wont be killed by this gremlin
	private final static int maxWaitTime = 2; // maximum time gremlin waits before acting
	
	String victimRole;
	
	Gremlin(Thread victim, Lookout lookout, String victimRole) {
		this.victim = victim;
		this.lookout = lookout;
		this.victimRole = victimRole;
	}
	
	public static void GremlinStart(Thread thread, Lookout lookout, String victimRole) {
		lookout.safe = true;
		Gremlin gremlin = new Gremlin(thread, lookout, victimRole);
		gremlin.start();
	}
	
	public static void GremlinEnd(Lookout lookout) {
		lookout.safe = false;
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		Random rand = new Random();
		try {
			Thread.sleep(rand.nextInt(maxWaitTime), rand.nextInt(1000));
		} catch (InterruptedException e) {
		}
		if(victim.isAlive() && lookout.safe) {
			if (rand.nextInt(100) > uptimePercent) {
				System.out.println("Gremlin: Killed " + victimRole);
				victim.stop();
				return;
			}
		}
	}
}

