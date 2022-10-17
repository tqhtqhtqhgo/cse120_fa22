package nachos.threads;
import java.util.*;

public class QueueThread extends ThreadQueue {
  Queue<KThread> q;
  public QueueThread() {
    this.q = new LinkedList<KThread>();
  }
  
  public void waitForAccess(KThread kt) {
    return;
  }

  public KThread nextThread() {
    return null;
  }

  public void acquire(KThread thread) {
    return;
  }

  public void print() {
    return;
  }
}
