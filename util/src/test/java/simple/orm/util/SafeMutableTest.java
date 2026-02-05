package simple.orm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for SafeMutable class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SafeMutableTest {

    @Test
    public void testNoCocurrency() {
        SafeMutable<String> mutable = SafeMutable.of("test string");
        assertThat(mutable.get()).isEqualTo("test string");
        mutable.set("new string");
        assertThat(mutable.get()).isEqualTo("new string");
        assertThat(mutable.apply(s -> s + " 2")).isEqualTo("new string 2");
        assertThat(mutable.get()).isEqualTo("new string 2");
    }

    @Test
    public void testConcurrencyApplySet() throws Exception {
        // prepare
        SafeMutable<String> mutable = SafeMutable.of("test string");
        Semaphore semaphore1 = new Semaphore(1); // to block inside "apply"
        Semaphore semaphore2 = new Semaphore(1); // to let second thread call "set" after "apply" is already called
        Thread t1 = new Thread(() -> {
            mutable.apply(s -> {
                try {
                    semaphore1.acquire(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return s + " 1";
            });
        });
        Thread t2 = new Thread(() -> {
            try {
                semaphore2.acquire(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mutable.set("test string 2");
        });
        // run
        semaphore1.acquire(1);
        semaphore2.acquire(1);
        t1.start();
        Thread.sleep(100);
        t2.start();
        Thread.sleep(100);
        assertThat(t1.isAlive()).isTrue(); // both threads are expected to be blocked
        assertThat(t2.isAlive()).isTrue(); // both threads are expected to be blocked
        assertThat(mutable.get()).isEqualTo("test string"); // accessible value not changed
        semaphore2.release();
        Thread.sleep(100);
        assertThat(t1.isAlive()).isTrue(); // both threads are expected to be still blocked
        assertThat(t2.isAlive()).isTrue(); // both threads are expected to be still blocked
        assertThat(mutable.get()).isEqualTo("test string"); // accessible value not changed
        semaphore1.release();
        Thread.sleep(100);
        assertThat(t1.isAlive()).isFalse(); // both threads are expected to finish
        assertThat(t2.isAlive()).isFalse(); // both threads are expected to finish
        t2.join();
        t1.join();
        // assert final value
        assertThat(mutable.get()).isEqualTo("test string 2");
    }

    @Test
    public void testConcurrencyApplyApply() throws Exception {
        // prepare
        SafeMutable<String> mutable = SafeMutable.of("test string");
        Semaphore semaphore = new Semaphore(1); // to block inside first "apply"
        Thread t1 = new Thread(() ->
                mutable.apply(s -> {
                    try {
                        semaphore.acquire(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return s + " 11";
                }));
        Thread t2 = new Thread(() -> mutable.apply(s -> s + " 3"));
        // run
        semaphore.acquire(1);
        t1.start();
        Thread.sleep(100);
        t2.start();
        Thread.sleep(100);
        assertThat(t1.isAlive()).isTrue(); // both threads are expected to be blocked
        assertThat(t2.isAlive()).isTrue(); // both threads are expected to be blocked
        assertThat(mutable.get()).isEqualTo("test string"); // accessible value not changed
        semaphore.release();
        Thread.sleep(100);
        assertThat(t1.isAlive()).isFalse(); // both threads are expected to finish
        assertThat(t2.isAlive()).isFalse(); // both threads are expected to finish
        t2.join();
        t1.join();
        // assert final value
        assertThat(mutable.get()).isEqualTo("test string 11 3");
    }

    @Test
    public void testNullPointers() {
        assertThatCode(() -> SafeMutable.<String>of(null)).isInstanceOf(NullPointerException.class);
        assertThatCode(() -> new SafeMutable<String>(null)).isInstanceOf(NullPointerException.class);
        SafeMutable<String> mutable = SafeMutable.of("some string");
        assertThatCode(() -> mutable.set(null)).isInstanceOf(NullPointerException.class);
        assertThatCode(() -> mutable.apply(s -> null)).isInstanceOf(NullPointerException.class);
    }

}
