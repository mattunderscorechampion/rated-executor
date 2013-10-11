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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

/**
 * Test suite for the RatedSingleFuture class.
 * <P>
 * Contains unit tests. It uses stubs for the tasks and mocks the executor.
 * 
 * @author Matt Champion
 * @since 0.1.0
 */
public final class RatedSingleFutureTest
{
    private RatedExecutor executor = mock(RatedExecutor.class);

    @Before
    public void before()
    {
        executor = mock(RatedExecutor.class);
    }

    // Test setting of the cancellation flag
    // This is based on the result of the Executor cancelTask method

    @Test
    public void testIsCancelled0()
    {
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        assertFalse(future.isCancelled());
    }

    @Test
    public void testIsCancelled1()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled2()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                false);
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled3()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled4()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                false);
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled5()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
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
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        assertFalse(future.isCancelled());
    }

    @Test
    public void testIsCancelled7()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled9()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                false);
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(false);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled10()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled);
    }

    @Test
    public void testIsCancelled11()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                false);
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        assertFalse(future.isCancelled());
        boolean cancelled = future.cancel(true);
        assertFalse(future.isCancelled());
        assertFalse(cancelled);
    }

    @Test
    public void testIsCancelled12()
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(true))).thenReturn(
                true);
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        assertFalse(future.isCancelled());
        boolean cancelled0 = future.cancel(true);
        assertTrue(future.isCancelled());
        assertTrue(cancelled0);
        boolean cancelled1 = future.cancel(true);
        assertTrue(future.isCancelled());
        assertFalse(cancelled1);
    }

    @Test
    public void testIsDone0()
    {
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        assertFalse(future.isDone());
    }

    @Test
    public void testIsDone1()
    {
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        future.setResult(null);
        assertTrue(future.isDone());
    }

    @Test
    public void testIsDone2()
    {
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        future.execute();
        assertTrue(future.isDone());
    }

    @Test
    public void testIsDone3()
    {
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        assertFalse(future.isDone());
    }

    @Test
    public void testIsDone4()
    {
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        future.setResult(null);
        assertTrue(future.isDone());
    }

    @Test
    public void testIsDone5()
    {
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        future.execute();
        assertTrue(future.isDone());
    }

    @Test
    public void testGet0() throws CancellationException, InterruptedException, ExecutionException
    {
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        future.execute();
        assertTrue(future.get() == null);
    }

    @Test(expected = CancellationException.class)
    public void testGet1() throws CancellationException, InterruptedException, ExecutionException
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new CountingTask());
        future.cancel(false);
        future.get();

    }

    @Test(expected = ExecutionException.class)
    public void testGet2() throws CancellationException, InterruptedException, ExecutionException
    {
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new ExceptionTask());
        future.execute();
        future.get();
    }

    @Test
    public void testGet3() throws CancellationException, InterruptedException, ExecutionException
    {
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        future.execute();
        assertEquals(Integer.valueOf(5), future.get());
    }

    @Test(expected = CancellationException.class)
    public void testGet4() throws CancellationException, InterruptedException, ExecutionException
    {
        when(executor.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        RatedSingleFuture<Integer> future = new RatedSingleFuture<Integer>(executor,
                new NumberCallable(5));
        future.cancel(false);
        future.get();

    }

    @Test(expected = ExecutionException.class)
    public void testGet5() throws CancellationException, InterruptedException, ExecutionException
    {
        RatedSingleFuture<Object> future = new RatedSingleFuture<Object>(executor,
                new ExceptionCallable());
        future.execute();
        future.get();
    }
}
