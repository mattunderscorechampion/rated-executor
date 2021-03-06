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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.mattunderscore.executor.stubs.BlockingTask;
import com.mattunderscore.executor.stubs.CountingCallable;
import com.mattunderscore.executor.stubs.CountingTask;
import com.mattunderscore.executor.stubs.ExceptionCallable;
import com.mattunderscore.executor.stubs.ExceptionTask;
import com.mattunderscore.executor.stubs.NumberCallable;
import com.mattunderscore.executor.stubs.TestException;
import com.mattunderscore.executor.stubs.TestThreadFactory;
import com.mattunderscore.executors.IRepeatingFuture;
import com.mattunderscore.executors.ITaskWrapperFactory;
import com.mattunderscore.executors.LessThanLong;
import com.mattunderscore.executors.RateMatcher;
import com.mattunderscore.executors.TestTaskWrapperFactory;

/**
 * Test suite for the Rated Executor.
 * <P>
 * Integration tests. Nothing is mocked.
 * 
 * @author Matt Champion
 * @since 0.0.1
 */
@RunWith(Parameterized.class)
public final class RatedExecutorTest
{
    private static final long RATE = 75L;
    private static final long EXTRA_MILLS = 15L;
    private static final long EXTRA_NANOS = TimeUnit.MILLISECONDS.toNanos(EXTRA_MILLS);

    private final Type type;
    private TestTaskWrapperFactory factory;
    private IRatedExecutor executor;

    /**
     * 
     * @param type
     *            The executor type
     */
    public RatedExecutorTest(final Type type)
    {
        this.type = type;
    }

    /**
     * Provide types to the JUnit runner to pass to the constructor.
     * @return The collection of types
     */
    @Parameters
    public static Collection<Object[]> data()
    {
        final Object[][] list = {
            {Type.STANDARD}, // 0
            {Type.STANDARD}, // 1
            {Type.STANDARD}, // 2
            {Type.INTERRUPTABLE}, // 3
            {Type.INTERRUPTABLE}, // 4
            {Type.INTERRUPTABLE}, // 5
            {Type.STANDARD_WITH_THREAD_FACTORY}, // 6
            {Type.STANDARD_WITH_THREAD_FACTORY}, // 7
            {Type.STANDARD_WITH_THREAD_FACTORY}, // 8
            {Type.INTERRUPTABLE_WITH_THREAD_FACTORY}, // 9
            {Type.INTERRUPTABLE_WITH_THREAD_FACTORY}, // 10
            {Type.INTERRUPTABLE_WITH_THREAD_FACTORY} // 11
        };
        return Arrays.asList(list);
    }

    /**
     * Creates an executor of the type provided to the constructor by the Paramaterized JUnit
     * runner.
     */
    @Before
    public void setUp()
    {
        factory = new TestTaskWrapperFactory();
        executor = type.getExecutor(RATE, TimeUnit.MILLISECONDS, factory);
    }

    /**
     * Test tasks are executed.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void testExecution0() throws InterruptedException
    {
        final CountingTask task = new CountingTask();
        executor.execute(task);
        factory.waitForTask(0, 0, RATE * 2);

        assertEquals(1, task.count);
    }

    /**
     * Test tasks are executed and the values of the future are correctly set.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void testExecution1() throws InterruptedException, ExecutionException, TimeoutException
    {
        final CountingTask task = new CountingTask();
        final Future<?> future = executor.submit(task);
        factory.waitForTask(0, 0, RATE * 2);

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertNull(future.get());
        assertNull(future.get(EXTRA_MILLS, TimeUnit.MILLISECONDS));
        assertEquals(1, task.count);
    }

    /**
     * Test callable tasks are executed.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void testExecution2() throws InterruptedException
    {
        final CountingCallable task = new CountingCallable();
        executor.execute(task);
        factory.waitForTask(0, 0, RATE * 2);

        assertEquals(1, task.count);
    }

    /**
     * Test tasks submitted at about the same time are rate limited.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Test
    public void testRateLimit0() throws InterruptedException, ExecutionException, TimeoutException
    {
        final CountingTask delayingTask = new CountingTask();
        final CountingTask task = new CountingTask();

        executor.execute(delayingTask);
        final Future<?> future = executor.submit(task);
        factory.waitForTask(0, 0, RATE * 2);

        assertFalse(future.isDone());
        TimeUnit.MILLISECONDS.sleep(RATE / 2);

        assertFalse(future.isDone());
        factory.getWrapper(1).getLatch(0).await();

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertNull(future.get());
        assertNull(future.get(EXTRA_MILLS, TimeUnit.MILLISECONDS));
        assertEquals(1, task.count);
    }

    /**
     * Test that tasks submitted after execution of previous tasks are rate limited..
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRateLimit1() throws InterruptedException
    {
        final CountingTask task0 = new CountingTask();
        final CountingTask task1 = new CountingTask();

        final Future<?> future0 = executor.submit(task0);
        factory.waitForTask(0, 0, RATE * 2);

        assertTrue(future0.isDone());
        final Future<?> future1 = executor.submit(task1);
        TimeUnit.MILLISECONDS.sleep(RATE / 2);

        assertFalse(future1.isDone());
        factory.waitForTask(1, 0, RATE * 2);

        assertTrue(future1.isDone());
        assumeThat(factory.timeBetween(0, 0, 1,0), new RateMatcher(RATE, TimeUnit.MILLISECONDS));
    }

    /**
     * Test that tasks are executed as soon as possible. Tasks scheduled after the last task has
     * been executed and the period has been exceeded are scheduled immediately.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testPromptness() throws InterruptedException
    {
        final CountingTask task0 = new CountingTask();
        final CountingTask task1 = new CountingTask();

        final Future<?> future0 = executor.submit(task0);
        factory.waitForTask(0, 0, RATE * 2);

        assertTrue("First task incomplete", future0.isDone());
        TimeUnit.MILLISECONDS.sleep(RATE);

        final long beforeSubmit = System.nanoTime();
        final Future<?> future1 = executor.submit(task1);
        factory.waitForTask(1, 0, RATE * 2);

        assertTrue("Second task incomplete", future1.isDone());
        assumeThat(factory.startTimestamp(1, 0) - beforeSubmit, new LessThanLong(EXTRA_NANOS));
    }

    /**
     * Test that the futures of runnable tasks return null from get.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testExecutionResult0() throws InterruptedException, ExecutionException
    {
        final CountingTask task0 = new CountingTask();

        final Future<?> future0 = executor.submit(task0);
        factory.waitForTask(0, 0, RATE * 2);

        assertTrue(future0.isDone());
        assertNull(future0.get());
    }

    /**
     * Test that callables set the result that can be retrieved from the future.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testExecutionResult1() throws InterruptedException, ExecutionException
    {
        final NumberCallable task0 = new NumberCallable(5);

        final Future<Integer> future0 = executor.submit(task0);
        factory.waitForTask(0, 0, RATE * 2);

        assertTrue(future0.isDone());
        assertEquals(Integer.valueOf(5), future0.get());
    }

    /**
     * Test that getting the result waits for the result to be set.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testExecutionResult2() throws InterruptedException, ExecutionException
    {
        final NumberCallable task0 = new NumberCallable(5);

        final Future<Integer> future0 = executor.submit(task0);
        assertEquals(Integer.valueOf(5), future0.get());
        assertTrue(future0.isDone());
    }

    /**
     * Test that getting the result throws an ExecutionException if a runnable threw an exception.
     * 
     * @throws Throwable
     */
    @Test(expected = TestException.class)
    public void testExecutionException0() throws Throwable
    {
        final ExceptionTask task0 = new ExceptionTask();

        final Future<?> future0 = executor.submit(task0);
        try
        {
            future0.get();
        }
        catch (ExecutionException e)
        {
            throw e.getCause();
        }
    }

    /**
     * Test that getting the result throws an ExecutionException if a callable threw an exception.
     * 
     * @throws Throwable
     */
    @Test(expected = TestException.class)
    public void testExecutionException1() throws Throwable
    {
        final ExceptionCallable task0 = new ExceptionCallable();

        final Future<Object> future0 = executor.submit(task0);
        try
        {
            future0.get();
        }
        catch (ExecutionException e)
        {
            throw e.getCause();
        }
    }

    /**
     * Test that tasks are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRepeatingExecution0() throws InterruptedException
    {
        final CountingTask task = new CountingTask();
        final long beforeSubmit = System.nanoTime();
        final Future<?> future = executor.schedule(task);
        factory.waitForTask(0, 0, RATE * 2);

        assumeThat(factory.startTimestamp(0, 0) - beforeSubmit, new LessThanLong(EXTRA_NANOS));
        assertEquals(1, task.count);
        factory.waitForTask(0, 1, RATE * 2);

        assumeThat(factory.timeBetween(0, 0, 0, 1), new RateMatcher(RATE, TimeUnit.MILLISECONDS));
        assertEquals(2, task.count);
        factory.waitForTask(0, 2, RATE * 2);

        assumeThat(factory.timeBetween(0, 1, 0, 2), new RateMatcher(RATE, TimeUnit.MILLISECONDS));
        assertEquals(3, task.count);
        assertFalse("Task should not be done", future.isDone());
        assertFalse("Task should not be cancelled", future.isCancelled());
        TimeUnit.MILLISECONDS.sleep(EXTRA_MILLS);

        final boolean cancelled = future.cancel(false);
        assertTrue("Cancel should succeed", cancelled);
        assertTrue("Task should be cancelled", future.isCancelled());
        assertTrue("Task should be done", future.isDone());
    }

    /**
     * Test that tasks with a limited number of repetitions repeat the correct number of times.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRepeatingExecution1() throws InterruptedException
    {
        final int repetitions = 5;
        final CountingTask task0 = new CountingTask();

        final IRepeatingFuture<?> future0 = executor.schedule(task0, repetitions);
        factory.waitForTask(0, 4, RATE * 2);

        assertEquals(repetitions, task0.count);
        assertTrue("Task should be done", future0.isDone());
        assertEquals("Completed executions", 5, future0.getCompletedExecutions());
        assertEquals("Expected executions", 5, future0.getExpectedExecutions());
    }

    /**
     * Test that tasks are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRepeatingExecution2() throws InterruptedException
    {
        final CountingTask task = new CountingTask();
        final IRepeatingFuture<?> future = executor.schedule(task, 3);
        factory.waitForTask(0, 0, RATE * 2);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 3, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());

        factory.waitForTask(0, 1, RATE * 2);
        assertEquals(2, task.count);
        assertEquals("Expected executions", 3, future.getExpectedExecutions());
        assertEquals("Completed executions", 2, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());
        assumeThat(factory.timeBetween(0, 0, 0, 1), new RateMatcher(RATE, TimeUnit.MILLISECONDS));

        factory.waitForTask(0, 2, RATE * 2);
        assertEquals(3, task.count);
        assertEquals("Expected executions", 3, future.getExpectedExecutions());
        assertEquals("Completed executions", 3, future.getCompletedExecutions());
        assertTrue("Task should have completed", future.isDone());
        assertFalse("Task should not be cancelled", future.isCancelled());
        assumeThat(factory.timeBetween(0, 1, 0, 2), new RateMatcher(RATE, TimeUnit.MILLISECONDS));
    }

    /**
     * Test that tasks are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     * @throws ExecutionException 
     * @throws IndexOutOfBoundsException 
     * @throws CancellationException 
     */
    @Test
    public void testRepeatingExecution3() throws InterruptedException, CancellationException,
        IndexOutOfBoundsException, ExecutionException
    {
        final CountingTask task = new CountingTask();
        final IRepeatingFuture<?> future = executor.schedule(task, 4);
        factory.waitForTask(0, 0, RATE * 2);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 4, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());

        assertNull(future.getResult(2));
        assertEquals(3, task.count);
    }

    /**
     * Test that tasks are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     * @throws ExecutionException 
     * @throws IndexOutOfBoundsException 
     * @throws CancellationException 
     * @throws TimeoutException 
     */
    @Test(expected = TimeoutException.class)
    public void testRepeatingExecution4() throws InterruptedException, CancellationException,
        IndexOutOfBoundsException, ExecutionException, TimeoutException
    {
        final CountingTask task = new CountingTask();
        final IRepeatingFuture<?> future = executor.schedule(task, 4);
        factory.waitForTask(0, 0, RATE * 2);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 4, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());

        future.getResult(2, 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Test that tasks are repeated and the values of futures are correctly set.
     *
     * @throws CancellationException
     * @throws IndexOutOfBoundsException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testRepeatingExecution5() throws CancellationException, IndexOutOfBoundsException,
        InterruptedException, ExecutionException
    {
        final CountingCallable task = new CountingCallable();
        final IRepeatingFuture<Integer> future = executor.schedule(task, 4);
        factory.waitForTask(0, 0, RATE * 2);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 4, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());

        assertNotNull(future.getResult(2));
        assertEquals(Integer.valueOf(3), future.getResult(2));
        assertEquals(3, task.count);
    }

    /**
     * Test submitted tasks are cancellable.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test(expected = CancellationException.class)
    public void testCancel0() throws InterruptedException, ExecutionException
    {
        final CountingTask delayingTask = new CountingTask();
        final CountingTask task = new CountingTask();
        executor.submit(delayingTask);
        final Future<?> future = executor.submit(task);
        future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());
        TimeUnit.MILLISECONDS.sleep(RATE * 3);

        assertEquals(0, task.count);
        future.get();
    }

    /**
     * Test submitted callable tasks are cancellable.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test(expected = CancellationException.class)
    public void testCancel1() throws InterruptedException, ExecutionException
    {
        final CountingTask delayingTask = new CountingTask();
        final NumberCallable task = new NumberCallable(5);

        executor.execute(delayingTask);
        final Future<?> future = executor.submit(task);
        future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());
        TimeUnit.MILLISECONDS.sleep(RATE * 3);

        future.get();
    }

    /**
     * Test repeating tasks are cancellable.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test(expected = CancellationException.class)
    public void testCancel2() throws InterruptedException, ExecutionException
    {
        final CountingTask delayingTask = new CountingTask();
        final CountingTask task = new CountingTask();
        executor.submit(delayingTask);
        final Future<?> future = executor.schedule(task);
        future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());
        TimeUnit.MILLISECONDS.sleep(RATE * 3);

        assertEquals(0, task.count);
        future.get();
    }

    /**
     * Test repeating tasks are cancellable.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test(expected = CancellationException.class)
    public void testCancel3() throws InterruptedException, ExecutionException
    {
        final CountingTask delayingTask = new CountingTask();
        final CountingTask task = new CountingTask();
        executor.submit(delayingTask);
        final IRepeatingFuture<?> future = executor.schedule(task,5);
        future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());
        TimeUnit.MILLISECONDS.sleep(RATE * 3);

        assertEquals(0, task.count);
        future.get();
    }

    /**
     * Test repeating tasks are cancellable.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test(expected = CancellationException.class)
    public void testCancel4() throws InterruptedException, ExecutionException
    {
        final CountingTask delayingTask = new CountingTask();
        final CountingTask task = new CountingTask();
        executor.submit(delayingTask);
        final IRepeatingFuture<?> future = executor.schedule(task,5);
        factory.waitForTask(1, 2, RATE * 2);
        assertFalse(future.isCancelled());
        assertFalse(future.isDone());

        TimeUnit.MILLISECONDS.sleep(EXTRA_MILLS);
        future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());
        future.get();
    }

    /**
     * Test running tasks are interrupted for executors that support this.
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test(expected = CancellationException.class)
    public void testCancel5() throws InterruptedException, ExecutionException
    {
        assumeTrue(type == Type.INTERRUPTABLE || type == Type.INTERRUPTABLE_WITH_THREAD_FACTORY);
        final CountDownLatch waitinglatch = new CountDownLatch(1);
        final CountDownLatch blockingLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final CountingTask delayingTask = new CountingTask();
        final BlockingTask task = new BlockingTask(waitinglatch, blockingLatch, completedLatch);
        executor.submit(delayingTask);
        final Future<?> future = executor.submit(task);
        waitinglatch.await();
        assertFalse(future.isCancelled());
        assertFalse(future.isDone());
        final boolean cancelled = future.cancel(true);
        completedLatch.await();
        assertTrue(cancelled);
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());
        assertTrue(task.hasRun());
        assertTrue(task.isIterrupted());
        future.get();
    }

    /**
     * Allow the setup method to create some type of {@link IRatedExecutor}.
     * 
     * @author Matt Champion
     * @since 0.1.1
     */
    private static enum Type
    {
        STANDARD
        {
            @Override
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit, final ITaskWrapperFactory wrapperFactory)
            {
                final TaskQueue queue = new TaskQueue();
                final ThreadFactory factory = new RatedExecutorThreadFactory();
                final IInternalExecutor executor = new ScheduledInternalExecutor(queue, duration, unit, factory);
                return new RatedExecutor(queue, executor, wrapperFactory);
            }
        },
        INTERRUPTABLE
        {
            @Override
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit, final ITaskWrapperFactory wrapperFactory)
            {
                final TaskQueue queue = new TaskQueue();
                final ThreadFactory factory = new RatedExecutorThreadFactory();
                final IInternalExecutor executor = new ThreadedInternalExecutor(queue, duration, unit, factory);
                return new RatedExecutor(queue, executor, wrapperFactory);
            }
        },
        STANDARD_WITH_THREAD_FACTORY
        {
            @Override
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit, final ITaskWrapperFactory wrapperFactory)
            {
                final TaskQueue queue = new TaskQueue();
                final IInternalExecutor executor = new ScheduledInternalExecutor(queue, duration, unit, new TestThreadFactory());
                return new RatedExecutor(queue, executor, wrapperFactory);
            }
        },
        INTERRUPTABLE_WITH_THREAD_FACTORY
        {
            @Override
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit, final ITaskWrapperFactory wrapperFactory)
            {
                final TaskQueue queue = new TaskQueue();
                final IInternalExecutor executor = new ThreadedInternalExecutor(queue, duration, unit, new TestThreadFactory());
                return new RatedExecutor(queue, executor, wrapperFactory);
            }
        };

        /**
         * Get an executor of the type indicated.
         * @param duration The duration of the executor rate
         * @param unit The time unit of the duration
         * @return The executor
         */
        public abstract IRatedExecutor getExecutor(long duration, TimeUnit unit, ITaskWrapperFactory wrapperFactory);
    }
}
