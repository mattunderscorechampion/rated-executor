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

import java.util.concurrent.Callable;

import org.junit.Test;

import com.mattunderscore.executor.stubs.CountingTask;

public class RunnableWrapperTest
{
    @Test
    public void testExecution() throws Exception
    {
        final CountingTask runnable = new CountingTask();
        final Callable<Void> callable = new RunnableWrapper(runnable);
        Void result = callable.call();
        assertNull(result);
        assertEquals(1, runnable.count);
    }

    @Test
    public void testEquals0()
    {
        final CountingTask runnable0 = new CountingTask();
        final Callable<Void> callable0 = new RunnableWrapper(runnable0);
        final CountingTask runnable1 = new CountingTask();
        final Callable<Void> callable1 = new RunnableWrapper(runnable1);
        assertNotEquals(callable0, callable1);
    }

    @Test
    public void testEquals1()
    {
        final CountingTask runnable = new CountingTask();
        final Callable<Void> callable = new RunnableWrapper(runnable);
        assertEquals(callable, callable);
        assertEquals(callable.hashCode(),callable.hashCode());
        assertEquals(callable.hashCode(),runnable.hashCode());
    }
}
