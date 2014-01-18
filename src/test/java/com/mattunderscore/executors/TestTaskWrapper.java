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

package com.mattunderscore.executors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Implements a task wrapper that counts down on a latch after execution.
 * @author Matt Champion
 * @since 0.1.2
 */
public final class TestTaskWrapper<V> extends TaskWrapper<V>
{
    private final List<CountDownLatch> latches = Collections.synchronizedList(new ArrayList<CountDownLatch>());
    private final List<Long> startTimestamps = Collections.synchronizedList(new ArrayList<Long>());
    private CountDownLatch currentLatch;

    public TestTaskWrapper(final Callable<V> task, final ITaskResultProcessor<V> processor)
    {
        super(task, processor);
        nextLatch();
    }

    @Override
    public void execute()
    {
        startTimestamps.add(System.nanoTime());
        super.execute();
        synchronized (this)
        {
            final CountDownLatch latch = currentLatch;
            nextLatch();
            latch.countDown();
        }
    }

    /**
     * Get the current latch.
     * @return The latch.
     */
    public CountDownLatch getLatch(final int i)
    {
        return latches.get(i);
    }

    /**
     * Get starting timestamp.
     * @param i The stamp to get.
     * @return
     */
    public long getStartTimestamp(final int i)
    {
        return startTimestamps.get(i);
    }

    private void nextLatch()
    {
        final CountDownLatch latch = new CountDownLatch(1);
        latches.add(latch);
        currentLatch = latch;
    }
}
