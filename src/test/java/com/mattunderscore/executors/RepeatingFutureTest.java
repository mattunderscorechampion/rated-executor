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

package com.mattunderscore.executors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.mattunderscore.executor.stubs.CountingCallable;
import com.mattunderscore.executor.stubs.CountingTask;
import com.mattunderscore.executor.stubs.ExceptionCallable;
import com.mattunderscore.executor.stubs.ExceptionTask;
import com.mattunderscore.executor.stubs.NumberCallable;
import com.mattunderscore.executors.RepeatingFuture;
import com.mattunderscore.executors.ITaskCanceller;
import com.mattunderscore.executors.ITaskWrapper;

/**
 * Test suite for the {@link RepeatingFuture} class.
 * <P>
 * Contains unit tests. It uses stubs for the tasks and mocks the executor.
 * 
 * @author Matt Champion
 * @since 0.1.0
 */
public final class RepeatingFutureTest
{
    private static final int REPETITIONS = 5;

    private ITaskCanceller canceller;

    @Before
    public void before() throws Exception
    {
        canceller = mock(ITaskCanceller.class);
    }

    // Test setting of the cancellation flag
    // This is based on the result of the TaskCanceller cancelTask method

    @Test
    public void testIsCancelled0()
    {
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        assertFalse(future.isCancelled());
    }

    @Test
    public void testIsCancelled1()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(false)))
                .thenReturn(true);
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled2()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(false)))
                .thenReturn(false);
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled3()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled4()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(true))).thenReturn(
                false);
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled5()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        assertFalse(future.isCancelled());
        boolean cancelled0 = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled0);
        boolean cancelled1 = future.cancel(true);
        assertTrue(future.isCancelled());
        assertFalse(cancelled1);
    }

    @Test
    public void testIsCancelled6()
    {
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        assertFalse(future.isCancelled());
    }

    @Test
    public void testIsCancelled7()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(false)))
                .thenReturn(true);
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled9()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(false)))
                .thenReturn(false);
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled10()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled11()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(true))).thenReturn(
                false);
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled12()
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        assertFalse(future.isCancelled());
        boolean cancelled0 = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled0);
        boolean cancelled1 = future.cancel(true);
        assertTrue(future.isCancelled());
        assertFalse(cancelled1);
    }

    // Test setting the done flag

    @Test
    public void testIsDone0()
    {
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        assertFalse(future.isDone());
    }

    @Test
    public void testIsDone1()
    {
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        final ITaskWrapper wrapper = new TaskWrapper<Void>(taskwrapper, processor);
        for (int i = 0; i < REPETITIONS; i++)
        {
            assertFalse(future.isDone());
            wrapper.execute();
        }
        assertTrue(future.isDone());
    }

    @Test
    public void testIsDone2()
    {
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        assertFalse(future.isDone());
    }

    @Test
    public void testIsDone3()
    {
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        final ITaskWrapper wrapper = new TaskWrapper<Integer>(new NumberCallable(5), processor);
        for (int i = 0; i < REPETITIONS; i++)
        {
            assertFalse(future.isDone());
            wrapper.execute();
        }
        assertTrue(future.isDone());
    }

    // Test getting the result

    @Test
    public void testGet0() throws CancellationException, InterruptedException, ExecutionException
    {
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        final ITaskWrapper wrapper = new TaskWrapper<Void>(taskwrapper, processor);
        wrapper.execute();
        assertTrue(future.get() == null);
        assertTrue(future.getResult(0) == null);
    }

    @Test(expected = CancellationException.class)
    public void testGet1() throws CancellationException, InterruptedException, ExecutionException
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(false)))
                .thenReturn(true);
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new CountingTask());
        new TaskWrapper<Void>(taskwrapper, processor);
        future.cancel(false);
        future.get();
    }

    @Test(expected = ExecutionException.class)
    public void testGet2() throws CancellationException, InterruptedException, ExecutionException
    {
        final RepeatingFuture<Void> future = new RepeatingFuture<Void>(canceller, REPETITIONS);
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(new ExceptionTask());
        final ITaskWrapper wrapper = new TaskWrapper<Void>(taskwrapper, processor);
        wrapper.execute();
        future.get();
    }

    @Test
    public void testGet3() throws CancellationException, InterruptedException, ExecutionException
    {
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        final ITaskWrapper wrapper = new TaskWrapper<Integer>(new NumberCallable(5), processor);
        wrapper.execute();
        assertEquals(Integer.valueOf(5), future.get());
        assertEquals(Integer.valueOf(5), future.getResult(0));
    }

    @Test(expected = CancellationException.class)
    public void testGet4() throws CancellationException, InterruptedException, ExecutionException
    {
        when(canceller.cancelTask(Matchers.any(ITaskWrapper.class), Matchers.eq(false)))
                .thenReturn(true);
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        new TaskWrapper<Integer>(new NumberCallable(5), processor);
        future.cancel(false);
        future.get();
    }

    @Test(expected = ExecutionException.class)
    public void testGet5() throws CancellationException, InterruptedException, ExecutionException
    {
        final RepeatingFuture<Object> future = new RepeatingFuture<Object>(canceller, REPETITIONS);
        final FutureSetResult<Object> processor = new FutureSetResult<Object>(future);
        final ITaskWrapper wrapper = new TaskWrapper<Object>(new ExceptionCallable(), processor);
        wrapper.execute();
        future.get();
    }

    @Test
    public void testGet6() throws CancellationException, InterruptedException, ExecutionException
    {
        final RepeatingFuture<Integer> future = new RepeatingFuture<Integer>(canceller, REPETITIONS);
        final FutureSetResult<Integer> processor = new FutureSetResult<Integer>(future);
        final ITaskWrapper wrapper = new TaskWrapper<Integer>(new CountingCallable(), processor);
        wrapper.execute();
        assertEquals(Integer.valueOf(1), future.get());
        assertEquals(Integer.valueOf(1), future.getResult(0));
        wrapper.execute();
        assertEquals(Integer.valueOf(2), future.get());
        assertEquals(Integer.valueOf(2), future.getResult(1));
        wrapper.execute();
        assertEquals(Integer.valueOf(3), future.get());
        assertEquals(Integer.valueOf(3), future.getResult(2));
    }
}
