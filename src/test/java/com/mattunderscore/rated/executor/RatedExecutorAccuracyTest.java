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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.mattunderscore.executor.stubs.CountingTask;

@RunWith(Parameterized.class)
public final class RatedExecutorAccuracyTest
{
    private static final long RUN = 10000L;
    private static final long RATE = 10L;

    private final IRatedExecutor executor;

    public RatedExecutorAccuracyTest(final IRatedExecutor executor)
    {
        this.executor = executor;
    }

    @Parameters
    public static Collection<Object[]> data()
    {
        final Object[][] list = {
            {RatedExecutors.ratedExecutor(RATE, TimeUnit.MILLISECONDS)}, // 0
            {RatedExecutors.interruptableRatedExecutor(RATE, TimeUnit.MILLISECONDS)} // 1
        };
        return Arrays.asList(list);
    }

    /**
     * Test to ensure that there is not too much overhead or inaccuracy in the rate.
     * 
     * @throws InterruptedException
     */
    @Test
    public void testAccuracy() throws InterruptedException
    {
        final CountingTask task = new CountingTask();
        final Future<?> future = executor.schedule(task);
        final long start = System.nanoTime();
        TimeUnit.MILLISECONDS.sleep(RUN);
        final long end = System.nanoTime();
        future.cancel(false);
        final long timeSpent = end - start;
        final long expectedNumber = (millisFromNanos(timeSpent) / RATE) + 1;
        System.out.println("Expected: " + expectedNumber);
        System.out.println("Actual: " + task.count);
        assertTrue(task.count > (expectedNumber - 2));
        assertTrue(task.count < (expectedNumber + 2));
    }

    private long millisFromNanos(final long nanos)
    {
        return Math.round(nanos / 1000000.0);
    }
}
