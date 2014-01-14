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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.mattunderscore.executor.stubs.CountingCallable;
import com.mattunderscore.executor.stubs.CountingTask;
import com.mattunderscore.executor.stubs.ExceptionCallable;
import com.mattunderscore.executor.stubs.ExceptionTask;
import com.mattunderscore.executor.stubs.NumberCallable;
import com.mattunderscore.executor.stubs.TestException;
import com.mattunderscore.executor.stubs.TestThreadFactory;
import com.mattunderscore.executors.IRepeatingFuture;

/**
 * Test suite for the Rated Executor.
 * <P>
 * Integration tests. Nothing is mocked.
 * 
 * @author Matt Champion
 * @since 0.0.1
 */
@RunWith(Parameterized.class)
public class RatedExecutorTest
{
    private static final long STD_RATE = 100L;
    private static final long STD_EXTRA = 15L;

    private final Type type;
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
        executor = type.getExecutor(STD_RATE, TimeUnit.MILLISECONDS);
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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.get() == null);
        assertTrue(future.get(STD_EXTRA, TimeUnit.MILLISECONDS) == null);
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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertFalse(future.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE / 2);

        assertFalse(future.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE / 2);

        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.get() == null);
        assertTrue(future.get(STD_EXTRA, TimeUnit.MILLISECONDS) == null);
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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertTrue(future0.isDone());
        final Future<?> future1 = executor.submit(task1);
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertFalse(future1.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

        assertTrue(future1.isDone());
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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertTrue(future0.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

        final Future<?> future1 = executor.submit(task1);
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertTrue(future1.isDone());
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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertTrue(future0.isDone());
        assertTrue(future0.get() == null);
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
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

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
     * Test that tests are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRepeatingExecution0() throws InterruptedException
    {
        final CountingTask task = new CountingTask();
        final Future<?> future = executor.schedule(task);
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertEquals(1, task.count);
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

        assertEquals(2, task.count);
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

        assertEquals(3, task.count);
        assertFalse("Task should not be done", future.isDone());
        assertFalse("Task should not be cancelled", future.isCancelled());

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
        TimeUnit.MILLISECONDS.sleep((STD_RATE + STD_EXTRA) * (repetitions + 3));

        assertEquals(repetitions, task0.count);
        assertTrue("Task should be done", future0.isDone());
        assertEquals("Completed executions", 5, future0.getCompletedExecutions());
        assertEquals("Expected executions", 5, future0.getExpectedExecutions());
    }

    /**
     * Test that tests are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testRepeatingExecution2() throws InterruptedException
    {
        final CountingTask task = new CountingTask();
        final IRepeatingFuture<?> future = executor.schedule(task, 3);
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 3, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

        assertEquals(2, task.count);
        assertEquals("Expected executions", 3, future.getExpectedExecutions());
        assertEquals("Completed executions", 2, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

        assertEquals(3, task.count);
        assertEquals("Expected executions", 3, future.getExpectedExecutions());
        assertEquals("Completed executions", 3, future.getCompletedExecutions());
        assertTrue("Task should have completed", future.isDone());
        assertFalse("Task should not be cancelled", future.isCancelled());
    }

    /**
     * Test that tests are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     * @throws ExecutionException 
     * @throws IndexOutOfBoundsException 
     * @throws CancellationException 
     */
    @Test
    public void testRepeatingExecution3() throws InterruptedException, CancellationException, IndexOutOfBoundsException, ExecutionException
    {
        final CountingTask task = new CountingTask();
        final IRepeatingFuture<?> future = executor.schedule(task, 4);
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 4, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

        assertNull(future.getResult(2));
        assertEquals(3, task.count);
    }

    /**
     * Test that tests are repeated and the values of futures are correctly set.
     * 
     * @throws InterruptedException
     * @throws ExecutionException 
     * @throws IndexOutOfBoundsException 
     * @throws CancellationException 
     * @throws TimeoutException 
     */
    @Test(expected = TimeoutException.class)
    public void testRepeatingExecution4() throws InterruptedException, CancellationException, IndexOutOfBoundsException, ExecutionException, TimeoutException
    {
        final CountingTask task = new CountingTask();
        final IRepeatingFuture<?> future = executor.schedule(task, 4);
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 4, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

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
    public void testRepeatingExecution5() throws CancellationException, IndexOutOfBoundsException, InterruptedException, ExecutionException
    {
        final CountingCallable task = new CountingCallable();
        final IRepeatingFuture<Integer> future = executor.schedule(task, 4);
        TimeUnit.MILLISECONDS.sleep(STD_EXTRA);

        assertEquals(1, task.count);
        assertEquals("Expected executions", 4, future.getExpectedExecutions());
        assertEquals("Completed executions", 1, future.getCompletedExecutions());
        assertFalse("Task should not be done", future.isDone());
        TimeUnit.MILLISECONDS.sleep(STD_RATE);

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
        TimeUnit.MILLISECONDS.sleep(STD_RATE * 3);

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
        TimeUnit.MILLISECONDS.sleep(STD_RATE * 3);

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
        TimeUnit.MILLISECONDS.sleep(STD_RATE * 3);

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
        TimeUnit.MILLISECONDS.sleep(STD_RATE * 3);

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
        TimeUnit.MILLISECONDS.sleep(STD_RATE * 3);
        assertFalse(future.isCancelled());
        assertFalse(future.isDone());
        future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(future.isDone());
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
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit)
            {
                return RatedExecutors.ratedExecutor(duration, unit);
            }
        },
        INTERRUPTABLE
        {
            @Override
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit)
            {
                return RatedExecutors.interruptableRatedExecutor(duration, unit);
            }
        },
        STANDARD_WITH_THREAD_FACTORY
        {
            @Override
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit)
            {
                return RatedExecutors.ratedExecutor(duration, unit, new TestThreadFactory());
            }
        },
        INTERRUPTABLE_WITH_THREAD_FACTORY
        {
            @Override
            public IRatedExecutor getExecutor(final long duration, final TimeUnit unit)
            {
                return RatedExecutors.interruptableRatedExecutor(duration, unit, new TestThreadFactory());
            }
        };

        /**
         * Get an executor of the type indicated.
         * @param duration The duration of the executor rate
         * @param unit The time unit of the duration
         * @return The executor
         */
        public abstract IRatedExecutor getExecutor(final long duration, final TimeUnit unit);
    }
}
