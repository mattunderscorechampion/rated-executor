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
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.mattunderscore.executor.stubs.CountingTask;
import com.mattunderscore.executor.stubs.ExceptionTask;
import com.mattunderscore.executor.stubs.TestException;
import com.mattunderscore.executors.ISettableFuture;

public final class RunnableTaskWrapperTest
{
    private ISettableFuture<Void> future;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
    {
        future = mock(ISettableFuture.class);
    }

    @Test
    public void testCountingTask()
    {
        final CountingTask task = new CountingTask();
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(task);
        final ITaskWrapper wrapper = new TaskWrapper<Void>(taskwrapper, processor);
        assertEquals(task.count, 0);
        wrapper.execute();
        assertEquals(task.count, 1);
        verify(future, times(1)).setResult(null);
        wrapper.execute();
        assertEquals(task.count, 2);
        verify(future, times(2)).setResult(null);
    }

    @Test
    public void testExceptionTask()
    {
        final ExceptionTask task = new ExceptionTask();
        final FutureSetResult<Void> processor = new FutureSetResult<Void>(future);
        final RunnableWrapper taskwrapper = new RunnableWrapper(task);
        final ITaskWrapper wrapper = new TaskWrapper<Void>(taskwrapper, processor);
        wrapper.execute();
        ArgumentCaptor<Throwable> captor = ArgumentCaptor
                .forClass(Throwable.class);
        verify(future, times(1)).setException(captor.capture());
        assertEquals(TestException.class,captor.getValue().getClass());
    }
}
