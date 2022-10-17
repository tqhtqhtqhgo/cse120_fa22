package nachos.threads;
import nachos.machine.*;
import java.util.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {


	private PriorityQueue<KThreadTimeKV> threads = new PriorityQueue<KThreadTimeKV>();
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}


	private class KThreadTimeKV implements Comparable<KThreadTimeKV> {
		private Long key;
		private KThread value;
		public KThreadTimeKV(Long time, KThread val) {
			this.key = time;
			this.value = val;
		}

		/*
		 * min heap comparator
		 */
		public int compareTo(KThreadTimeKV otherThread) {
			if(this.key > otherThread.key) { return -1; }
			else if(this.key < otherThread.key) { return 1; }
			else { return 0; }
		}
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		long currTime = Machine.timer().getTime();
		boolean disabled = Machine.interrupt().disable();
		while(!threads.isEmpty() && (threads.peek().key >= currTime)) {
			threads.poll().value.ready();
		}
		KThread.currentThread().yield();
		Machine.interrupt().restore(disabled);
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		if(x <= 0) { return; }
		long wakeTime = Machine.timer().getTime() + x;
		KThreadTimeKV KVPair = new KThreadTimeKV(wakeTime, KThread.currentThread());
		boolean disabled = Machine.interrupt().disable();
		if(wakeTime > Machine.timer().getTime()) {
			threads.add(KVPair);
			KThread.currentThread().sleep();
		}
		Machine.interrupt().restore(disabled);
	}

    /**
	 * Cancel any timer set by <i>thread</i>, effectively waking
	 * up the thread immediately (placing it in the scheduler
	 * ready set) and returning true.  If <i>thread</i> has no
	 * timer set, return false.
	 * 
	 * <p>
	 * @param thread the thread whose timer should be cancelled.
	 */
    public boolean cancel(KThread thread) {
		for(java.util.Iterator i = threads.iterator();i.hasNext();){
			KThreadTimeKV timeCompare = (KThreadTimeKV) i.next(); // Get each comparable thread from list to check
			if(timeCompare.value==thread){ // If the thread has timer set remove and ready
				i.remove();
				timeCompare.value.ready();
				return true;
			}
		}
		return false;
	}
	public static void alarmTest1() {
		int durations[] = {1000, 10*1000, 100*1000};
		long t0, t1;

		for (int d : durations) {
			t0 = Machine.timer().getTime();
			ThreadedKernel.alarm.waitUntil (d);
			t1 = Machine.timer().getTime();
			System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		}
	}
	
	public static void selfTest() {
		alarmTest1();

		// Invoke your other test methods here ...
	}
}
