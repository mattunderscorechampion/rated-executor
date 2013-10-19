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
import static org.mockito.Mockito.*;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import com.google.code.tempusfugit.concurrency.IntermittentTestRunner;
import com.google.code.tempusfugit.concurrency.annotations.Intermittent;

/**
 * Test suite for the SingleFuture class.
 * <P>
 * Contains unit tests. It uses stubs for the tasks and mocks the task canceller.
 * 
 * @author Matt Champion
 * @since 0.1.0
 */
@RunWith(IntermittentTestRunner.class)
public final class SingleFutureTest
{
    private static final int REPETITIONS = 50;
    private static final long TIMEOUT = 200L;
    private static final long TIMEOUT_MAX = 204L;
    private static final long M_2_N = 1000L * 1000L;

    private TaskCanceller canceller;

    @Before
    public void before()
    {
        canceller = mock(TaskCanceller.class);
    }

    // Test setting of the cancellation flag
    // This is based on the result of the TaskCanceller cancelTask method

    @Test
    public void testIsCancelled0()
    {
        final SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isCancelled());
    }

    @Test
    public void testIsCancelled1()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled2()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                false);
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled3()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled4()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                false);
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled5()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
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
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        assertFalse(future.isCancelled());
    }

    @Test
    public void testIsCancelled7()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled9()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                false);
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled10()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled11()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                false);
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled12()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
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
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isDone());
    }

    @Test
    public void testIsDone1()
    {
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        future.setResult(null);
        assertTrue(future.isDone());
    }

    @Test
    public void testIsDone2()
    {
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        TaskWrapper task = new RunnableTaskWrapper(new CountingTask(),future);
        task.execute();
        assertTrue(future.isDone());
    }

    @Test
    public void testIsDone3()
    {
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        assertFalse(future.isDone());
    }

    @Test
    public void testIsDone4()
    {
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        future.setResult(null);
        assertTrue(future.isDone());
    }

    @Test
    public void testIsDone5()
    {
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        TaskWrapper task = new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        task.execute();
        assertTrue(future.isDone());
    }

    // Test getting the result

    @Test
    public void testGet0() throws CancellationException, InterruptedException, ExecutionException
    {
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        TaskWrapper task = new RunnableTaskWrapper(new CountingTask(),future);
        task.execute();
        assertTrue(future.get() == null);
    }

    @Test(expected = CancellationException.class)
    public void testGet1() throws CancellationException, InterruptedException, ExecutionException
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        future.cancel(false);
        future.get();
    }

    @Test(expected = ExecutionException.class)
    public void testGet2() throws CancellationException, InterruptedException, ExecutionException
    {
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        TaskWrapper task = new RunnableTaskWrapper(new ExceptionTask(),future);
        task.execute();
        future.get();
    }

    @Test
    public void testGet3() throws CancellationException, InterruptedException, ExecutionException
    {
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        TaskWrapper task = new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        task.execute();
        assertEquals(Integer.valueOf(5), future.get());
    }

    @Test(expected = CancellationException.class)
    public void testGet4() throws CancellationException, InterruptedException, ExecutionException
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
        new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
        future.cancel(false);
        future.get();

    }

    @Test(expected = ExecutionException.class)
    public void testGet5() throws CancellationException, InterruptedException, ExecutionException
    {
        SingleFuture<Object> future = new SingleFuture<Object>(canceller);
        TaskWrapper task = new CallableTaskWrapper<Object>(new ExceptionCallable(),future);
        task.execute();
        future.get();
    }

    @Test(expected = TimeoutException.class)
    @Intermittent(repetition = REPETITIONS)
    public void testTimeout0() throws Throwable
    {
        long start = System.nanoTime();
        try
        {
            SingleFuture<Object> future = new SingleFuture<Object>(canceller);
            new RunnableTaskWrapper(new CountingTask(),future);
            start = System.nanoTime();
            future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            long end = System.nanoTime();
            long time = end - start;
            System.out.println("Time: " + time + " Target:" + (TIMEOUT * M_2_N) + " Max: " + (TIMEOUT_MAX * M_2_N));
            assertTrue(time < (TIMEOUT_MAX * M_2_N));
            throw t;
        }
    }

    @Test(expected = TimeoutException.class)
    @Intermittent(repetition = REPETITIONS)
    public void testTimeout1() throws Throwable
    {
        long start = System.nanoTime();
        try
        {
            SingleFuture<Integer> future = new SingleFuture<Integer>(canceller);
            new CallableTaskWrapper<Integer>(new NumberCallable(5),future);
            start = System.nanoTime();
            future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        }
        catch (Throwable t)
        {
            long end = System.nanoTime();
            long time = end - start;
            System.out.println("Time: " + time + " Target:" + (TIMEOUT * M_2_N) + " Max: " + (TIMEOUT_MAX * M_2_N));
            assertTrue(time < (TIMEOUT_MAX * M_2_N));
            throw t;
        }
    }
}
