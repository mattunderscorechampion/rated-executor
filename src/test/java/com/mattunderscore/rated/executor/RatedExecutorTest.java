/* Copyright © 2013 Matthew Champion
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of mattunderscore.com nor the
 names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL MATTHEW CHAMPION BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.mattunderscore.rated.executor;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.code.tempusfugit.concurrency.IntermittentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Intermittent;
import com.mattunderscore.rated.executor.RatedExecutor;

/**
 * Test for the Rated Executor
 *
 * @author Matt Champion
 */
@RunWith(IntermittentTestRunner.class)
public class RatedExecutorTest
{
    /**
     * Test tasks are executed and the values of the future are correctly set.
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    @Intermittent(repetition = 50)
    public void testExecution0() throws InterruptedException, ExecutionException, TimeoutException
    {
        RatedExecutor executor = new RatedExecutor(100, TimeUnit.MILLISECONDS);
        CountingTask task = new CountingTask();
        Future<?> future = executor.submit(task);
        TimeUnit.MILLISECONDS.sleep(10);

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.get() == null);
        assertTrue(future.get(10, TimeUnit.MILLISECONDS) == null);
        assertEquals(1, task.count);
    }

    /**
     * Test tasks submitted at the same time are rate limited.
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    @Intermittent(repetition = 50)
    public void testExecution1() throws InterruptedException, ExecutionException, TimeoutException
    {
        RatedExecutor executor = new RatedExecutor(100, TimeUnit.MILLISECONDS);
        CountingTask delayingTask = new CountingTask();
        CountingTask task = new CountingTask();

        long startTime = System.currentTimeMillis();
        executor.submit(delayingTask);
        Future<?> future = executor.submit(task);
        TimeUnit.MILLISECONDS.sleep(10);

        long endTime = System.currentTimeMillis();
        // Require little time to pass
        // TODO: This is machine specific should it be assume or removed?
        assertTrue((endTime - startTime) < 15);
        assertFalse(future.isDone());
        TimeUnit.MILLISECONDS.sleep(40);

        assertFalse(future.isDone());
        TimeUnit.MILLISECONDS.sleep(60);

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.get() == null);
        assertTrue(future.get(10, TimeUnit.MILLISECONDS) == null);
        assertEquals(1, task.count);
    }

    /**
     * Test that tasks submitted after execution of previous tasks are rate limited..
     *
     * @throws InterruptedException
     */
    @Test
    @Intermittent(repetition = 50)
    public void testExecution2() throws InterruptedException
    {
        RatedExecutor executor = new RatedExecutor(100, TimeUnit.MILLISECONDS);
        CountingTask task0 = new CountingTask();
        CountingTask task1 = new CountingTask();

        Future<?> future0 = executor.submit(task0);
        TimeUnit.MILLISECONDS.sleep(20);

        assertTrue(future0.isDone());
        Future<?> future1 = executor.submit(task1);
        TimeUnit.MILLISECONDS.sleep(20);
        assertFalse(future1.isDone());

        TimeUnit.MILLISECONDS.sleep(70);
        assertTrue(future1.isDone());
    }

    /**
     * Test that tasks are executed as soon as possible. Tasks scheduled after the last task has
     * been executed and the period has been exceeded are scheduled immediately.
     *
     * @throws InterruptedException
     */
    @Test
    @Intermittent(repetition = 50)
    public void testExecution3() throws InterruptedException
    {
        RatedExecutor executor = new RatedExecutor(100, TimeUnit.MILLISECONDS);
        CountingTask task0 = new CountingTask();
        CountingTask task1 = new CountingTask();

        Future<?> future0 = executor.submit(task0);
        TimeUnit.MILLISECONDS.sleep(5);

        assertTrue(future0.isDone());
        TimeUnit.MILLISECONDS.sleep(100);

        Future<?> future1 = executor.submit(task1);
        TimeUnit.MILLISECONDS.sleep(5);

        assertTrue(future1.isDone());
    }

    /**
     * Test that tests are repeated and the values of futures are correctly set.
     *
     * @throws InterruptedException
     */
    @Test
    @Intermittent(repetition = 50)
    public void testRepeatingExecution() throws InterruptedException
    {
        RatedExecutor executor = new RatedExecutor(100, TimeUnit.MILLISECONDS);
        CountingTask task = new CountingTask();
        Future<?> future = executor.schedule(task);
        TimeUnit.MILLISECONDS.sleep(10);

        assertEquals(1, task.count);
        TimeUnit.MILLISECONDS.sleep(110);

        assertEquals(2, task.count);
        TimeUnit.MILLISECONDS.sleep(100);

        assertEquals(3, task.count);
        assertFalse(future.isDone());
        assertFalse(future.isCancelled());
    }

    /**
     * Test submitted tasks are cancellable.
     *
     * @throws InterruptedException
     */
    @Test
    @Intermittent(repetition = 50)
    public void testCancel0() throws InterruptedException
    {
        RatedExecutor executor = new RatedExecutor(100, TimeUnit.MILLISECONDS);
        CountingTask delayingTask = new CountingTask();
        CountingTask task = new CountingTask();
        executor.submit(delayingTask);
        Future<?> future = executor.submit(task);
        future.cancel(false);
        assertTrue(future.isCancelled());
        TimeUnit.MILLISECONDS.sleep(300);

        assertTrue(future.isDone());
        assertEquals(0, task.count);
    }

    /**
     * Test repeating tasks are cancellable.
     *
     * @throws InterruptedException
     */
    @Test
    @Intermittent(repetition = 50)
    public void testCancel1() throws InterruptedException
    {
        RatedExecutor executor = new RatedExecutor(100, TimeUnit.MILLISECONDS);
        CountingTask delayingTask = new CountingTask();
        CountingTask task = new CountingTask();
        executor.submit(delayingTask);
        Future<?> future = executor.schedule(task);
        future.cancel(false);
        assertTrue(future.isCancelled());
        TimeUnit.MILLISECONDS.sleep(300);

        assertTrue(future.isDone());
        assertEquals(0, task.count);
    }

    /**
     * Test to ensure that there is not too much overhead or inaccuracy in the rate.
     *
     * @throws InterruptedException
     */
    @Test
    public void testAccuracy() throws InterruptedException
    {
        long rate = 20L;
        RatedExecutor executor = new RatedExecutor(rate, TimeUnit.MILLISECONDS);
        CountingTask task = new CountingTask();
        long start = System.currentTimeMillis();
        Future<?> future = executor.schedule(task);
        TimeUnit.MILLISECONDS.sleep(5000);
        future.cancel(false);
        long end = System.currentTimeMillis();
        long timeSpent = end - start;
        long expectedNumber = (timeSpent / rate);
        assertTrue(task.count > (expectedNumber - 5));
        assertTrue(task.count < (expectedNumber + 5));
        System.out.println("Expected: " + expectedNumber);
        System.out.println("Actual: " + task.count);
    }

    /**
     * Simple task to execute.
     *
     * @author Matt Champion
     */
    private static class CountingTask implements Runnable
    {
        private int count = 0;

        public void run()
        {
            count++;
        }
    }
}
