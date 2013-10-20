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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.mattunderscore.rated.executor.stubs.CountingTask;
import com.mattunderscore.rated.executor.stubs.ExceptionTask;

/**
 * Test suite for the {@link UnboundedFuture} class.
 * <P>
 * Contains unit tests. It uses stubs for the tasks and mocks the executor.
 * 
 * @author Matt Champion
 * @since 0.1.0
 */
public class UnboundedFutureTest
{
    private static final int REPETITIONS = 5;

    private TaskCanceller canceller;

    @Before
    public void before() throws Exception
    {
        canceller = mock(TaskCanceller.class);
    }

    // Test setting of the cancellation flag
    // This is based on the result of the TaskCanceller cancelTask method

    @Test
    public void testIsCancelled0()
    {
        final UnboundedFuture future = new UnboundedFuture(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isCancelled());
    }

    @Test
    public void testIsCancelled1()
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        final UnboundedFuture future = new UnboundedFuture(canceller);
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
        final UnboundedFuture future = new UnboundedFuture(canceller);
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
        final UnboundedFuture future = new UnboundedFuture(canceller);
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
        final RepeatingFuture<Object> future = new RepeatingFuture<Object>(canceller,REPETITIONS);
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
        final UnboundedFuture future = new UnboundedFuture(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
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
        final UnboundedFuture future = new UnboundedFuture(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        assertFalse(future.isDone());
    }

    @Test
    public void testIsDone1()
    {
        final UnboundedFuture future = new UnboundedFuture(canceller);
        final TaskWrapper task = new RunnableTaskWrapper(new CountingTask(),future);
        for (int i = 0; i < REPETITIONS; i++)
        {
            assertFalse(future.isDone());
            task.execute();
        }
        assertFalse(future.isDone());
    }

    // Test getting the result

    @Test
    public void testGet0() throws CancellationException, InterruptedException, ExecutionException
    {
        final UnboundedFuture future = new UnboundedFuture(canceller);
        final TaskWrapper task = new RunnableTaskWrapper(new CountingTask(),future);
        task.execute();
        assertTrue(future.get() == null);
    }

    @Test(expected = CancellationException.class)
    public void testGet1() throws CancellationException, InterruptedException, ExecutionException
    {
        when(canceller.cancelTask(Matchers.any(TaskWrapper.class), Matchers.eq(false))).thenReturn(
                true);
        final UnboundedFuture future = new UnboundedFuture(canceller);
        new RunnableTaskWrapper(new CountingTask(),future);
        future.cancel(false);
        future.get();
    }

    @Test(expected = ExecutionException.class)
    public void testGet2() throws CancellationException, InterruptedException, ExecutionException
    {
        final UnboundedFuture future = new UnboundedFuture(canceller);
        final TaskWrapper task = new RunnableTaskWrapper(new ExceptionTask(),future);
        task.execute();
        future.get();
    }
}
