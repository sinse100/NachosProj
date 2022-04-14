package nachos.threads;

import nachos.machine.*;

//----------------------------
import java.util.Vector;
//----------------------------

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */

public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */

    private Vector WaitQ = null;            // for waiting Queue for asserted Thread
                                            // we gonna store SelfWaitThread in this vector...

    public class SelfWaitThread {                 // thread which itself calls 'waitUntil(long)'
        private KThread waitThread = null;        // thread in waiting State
        private long waitTime = 0;                // waiting time

        public SelfWaitThread(KThread thread, long time) {
               waitTime = time;
                waitThread = thread;
        }
       
        public KThread getSelfWaitThread() {return waitThread;}  // return him self
        public long getSelfWaitTime() {return waitTime;}         // return set waiting time
   
    } 
    
    public Alarm() {
        WaitQ = new Vector();
        Machine.timer().setInterruptHandler(new Runnable() {
                public void run() { timerInterrupt(); }
            });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
  
        long now = Machine.timer().getTime();    // get Current Time
        boolean intStatus = Machine.interrupt().disable(); // need to disable Interrupt
           
        int i=0;

        while(WaitQ.size() > i){           // we gonna check all thread in waiting set!!
            SelfWaitThread tmp;
            tmp = (SelfWaitThread)WaitQ.elementAt(i);
            
            if(tmp.getSelfWaitTime()<now){ // if waiting time has passed
               tmp.getSelfWaitThread().ready(); // put the thread into ready queue
               WaitQ.removeElementAt(i);        // remove that thread from waiting set
               if(i != 0)i--;
            }
            i++;                                // check next thread in ready queue
        }
        Machine.interrupt().restore(intStatus); // enable interrupt again!!
        KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param   x       the minimum number of clock ticks to wait.
     *
     * @see     nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
        long wakeTime = Machine.timer().getTime() + x;     // get due of waiting
        boolean intStatus = Machine.interrupt().disable(); // need to disable interrupt
       
        SelfWaitThread tmp = new SelfWaitThread(KThread.currentThread(),wakeTime);
        WaitQ.add((SelfWaitThread)tmp);  // put this thread(called waitUntil) to waiting set
        tmp.getSelfWaitThread().sleep(); // put this thread into waiting set until wakeTime
        Machine.interrupt().restore(intStatus);  // enable interrupt again!!
        
    }
   public static void alarmTest1() {                 // testing alarm
       int durations[] = {1000,10*1000, 100*1000};   // testing duration
       long t0,t1;
 
       for(int d : durations){
          t0 = Machine.timer().getTime();             
          ThreadedKernel.alarm.waitUntil(d);
          t1 = Machine.timer().getTime();
          System.out.println("alarmTest1: waited for "+(t1-t0)+"ticks");  
       }
  }
 
  public static void selfTest() {
      alarmTest1();
  }   
}
