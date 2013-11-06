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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.mattunderscore.executor.stubs.CountingCallable;
import com.mattunderscore.executor.stubs.CountingTask;
import com.mattunderscore.executor.stubs.TestThreadFactory;
import com.mattunderscore.executors.IUniversalExecutor;

/**
 * Test suite for the Simple Rated Executor.
 * <P>
 * Integration tests. Nothing is mocked.
 *
 * @author Matt Champion
 * @since 0.1.1
 */
@RunWith(Parameterized.class)
public class SimpleRatedExecutorTest
{
    private static final long RATE = 100L;
    private static final long EXTRA = 15L;

    private final Type type;
    private IUniversalExecutor executor;

    /**
     * 
     * @param type
     *            The executor type
     */
    public SimpleRatedExecutorTest(final Type type)
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
            {Type.SIMPLE}, // 0
            {Type.SIMPLE}, // 1
            {Type.SIMPLE}, // 2
            {Type.SIMPLE_WITH_THREAD_FACTORY}, // 3
            {Type.SIMPLE_WITH_THREAD_FACTORY}, // 4
            {Type.SIMPLE_WITH_THREAD_FACTORY}, // 5
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
        executor = type.getExecutor(RATE, TimeUnit.MILLISECONDS);
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
        TimeUnit.MILLISECONDS.sleep(EXTRA);

        assertEquals(1, task.count);
    }

    @Test
    public void testExecution1() throws InterruptedException
    {
        final CountingCallable task = new CountingCallable();
        executor.execute(task);
        TimeUnit.MILLISECONDS.sleep(EXTRA);

        assertEquals(1, task.count);
    }

    @Test
    public void testExecution2() throws InterruptedException
    {
        final CountingTask task = new CountingTask();
        executor.execute(task);
        TimeUnit.MILLISECONDS.sleep(EXTRA);
        assertEquals(1, task.count);
        executor.execute(task);
        TimeUnit.MILLISECONDS.sleep(EXTRA);
        assertEquals(1, task.count);
        TimeUnit.MILLISECONDS.sleep(RATE);
        assertEquals(2, task.count);
    }

    @Test
    public void testExecution3() throws InterruptedException
    {
        final CountingCallable task = new CountingCallable();
        executor.execute(task);
        TimeUnit.MILLISECONDS.sleep(EXTRA);

        assertEquals(1, task.count);
        executor.execute(task);
        TimeUnit.MILLISECONDS.sleep(EXTRA);
        assertEquals(1, task.count);
        TimeUnit.MILLISECONDS.sleep(RATE);
        assertEquals(2, task.count);
    }

    private static enum Type
    {
        SIMPLE
        {
            public IUniversalExecutor getExecutor(final long duration, final TimeUnit unit)
            {
                return RatedExecutors.simpleRatedExecutor(duration, unit, new TestThreadFactory());
            }
        },
        SIMPLE_WITH_THREAD_FACTORY
        {
            public IUniversalExecutor getExecutor(final long duration, final TimeUnit unit)
            {
                return RatedExecutors.simpleRatedExecutor(duration, unit, new TestThreadFactory());
            }
        };

        public abstract IUniversalExecutor getExecutor(final long duration, final TimeUnit unit);
    }
}
