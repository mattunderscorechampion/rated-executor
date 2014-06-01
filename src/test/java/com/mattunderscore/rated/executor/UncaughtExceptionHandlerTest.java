/* Copyright © 2014 Matthew Champion
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

import com.mattunderscore.executor.stubs.ExceptionTask;
import com.mattunderscore.executor.stubs.TestException;
import com.mattunderscore.executor.stubs.TestThreadFactory;
import com.mattunderscore.executor.stubs.TestUncaughtExceptionHandler;
import com.mattunderscore.executors.ITaskWrapperFactory;
import com.mattunderscore.executors.TaskWrapperFactory;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author matt on 01/06/14.
 */
public class UncaughtExceptionHandlerTest {
    @Test
    public void handlerTest() throws InterruptedException {
        final TestUncaughtExceptionHandler handler = new TestUncaughtExceptionHandler();
        final ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(handler);
                return t;
            }
        };
        final Executor executor = RatedExecutors.simpleRatedExecutor(50, TimeUnit.MILLISECONDS, factory);
        executor.execute(new ExceptionTask());
        assertTrue("Timed out", handler.latch.await(500, TimeUnit.MILLISECONDS));
        assertTrue(handler.throwable.getClass().getSimpleName(), handler.throwable instanceof TestException);
    }
}
