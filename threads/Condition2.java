package nachos.threads;

import nachos.machine.*;
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

		// waitQueue = new LinkedList<Lock>();
		waitQueue = new LinkedList<KThread>();
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The
	 * current thread must hold the associated lock. The thread will
	 * automatically reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean status = Machine.interrupt().disable(); 
		
		conditionLock.release();
		waitQueue.add(KThread.currentThread());
		KThread.sleep();
		conditionLock.acquire();
		Machine.interrupt().restore(status);
		
//		Lock waiter = new Lock();
//		waitQueue.add(waiter);
//
//		conditionLock.release();
//		waiter.acquire();
//		conditionLock.acquire();
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean status = Machine.interrupt().disable(); 
		
		if (!waitQueue.isEmpty()){
			// ((Lock)waitQueue.removeFirst()).release();
			(waitQueue.removeFirst()).ready();
		}
		
		Machine.interrupt().restore(status);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		
		boolean status = Machine.interrupt().disable(); 
		
		while (!waitQueue.isEmpty()){
			wake();
		}
		Machine.interrupt().restore(status);
        }

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

   private Lock conditionLock;
	// private LinkedList<Lock> waitQueue;
   private LinkedList<KThread> waitQueue;
}

    
