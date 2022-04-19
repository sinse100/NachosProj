package nachos.threads;

import nachos.machine.*;
import java.util.Vector;
import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
        CV_WaitThread_List = new Vector();  // init conditional waiting set!!
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	conditionLock.release();
 
        ////////////////////////////////////////////////////////////
        boolean intStatus = Machine.interrupt().disable(); // to provide atomicity, must disable interrupt
        CV_WaitThread_List.add((KThread)KThread.currentThread()); 
        // add thread(caller of sleep) to Conditional Waiting set
        KThread.currentThread().sleep();
        Machine.interrupt().restore(intStatus);
        //////////////////////////////////////////////////////////////

	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

        boolean intStatus = Machine.interrupt().disable(); // to provide atomicity, must disable interrupt
        if(CV_WaitThread_List.size() !=0) {                // FCFS Scheduling, ready most longedt waiting thread
            ((KThread)CV_WaitThread_List.firstElement()).ready(); 
            CV_WaitThread_List.removeElementAt(0);
        }
         Machine.interrupt().restore(intStatus);           // enable interrupt again!!   
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

         while(CV_WaitThread_List.size()!=0)            // wake all threads in conditional waiting set!!!
            wake();
    }

    private Lock conditionLock;      // lock
    private Vector CV_WaitThread_List = null;   // conditional waiting set!!!


    ///////////////////// This Class is for Testing ////////////////////////////
 
    private static class InterlockTest {        
        private static Lock lock;              // locker (Mutex)
        private static Condition2 cv;          // condition variable

        private static class Interlocker implements Runnable {
           public void run() {
              lock.acquire();                // get mutex of it(lock mutex is for sync of run method!!)
              for(int i=0; i<10;i++) {         
                  System.out.println(KThread.currentThread().getName());  // print itself name(thread name)
                  cv.wake();                     // wake up thread in conditional waiting set
                  cv.sleep();                    // release mutex lock and get into conditional waiting set
              }
              lock.release();                // release mutex for run method!!
           }
        }

        public InterlockTest() {
            lock = new Lock();             // lock and condition variable init
            cv = new Condition2(lock);     

            KThread ping = new KThread(new Interlocker()); // InterLocker Testing Routine1 (call it ping)
            ping.setName("ping");
            KThread pong = new KThread(new Interlocker()); // InterLocker Testing Routine1 (call it ping)
            pong.setName("pong");

            ping.fork();
            pong.fork();
            
            ping.join();                                  // wait for ping to finish
                                                          // when ping terminated, pong is sleeping...
                                                          // so, pong.join() makes whole system blocked!! 
        }
   }
     
   public static void selfTest() { new InterlockTest();}      // Testing InterLock!!

   public static void cvTest5() {
      final Lock lock = new Lock();
      final Condition2 empty = new Condition2(lock);
      final LinkedList<Integer> list = new LinkedList<>();

      KThread consumer = new KThread(new Runnable() {
           public void run() {
              lock.acquire();
              while(list.isEmpty()){empty.sleep();}
              Lib.assertTrue(list.size()==5, "List should have 5 values.");
              while(!list.isEmpty()) {
                  KThread.currentThread().yield();
                  System.out.println("Removed " + list.removeFirst());
              }
              lock.release();
           }
         });

      KThread producer = new KThread(new Runnable() {
           public void run() {
              lock.acquire();
              for(int i=0; i<5;i++){
                 list.add(i);
                 System.out.println("Added " + i);
                 KThread.currentThread().yield();
              }
              empty.wake();
              lock.release();
           }
         });

     consumer.setName("Consumer");
     producer.setName("Producer");
     consumer.fork();
     producer.fork();
     consumer.join();
     producer.join();
   }
}
