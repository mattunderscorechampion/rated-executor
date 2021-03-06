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

package com.mattunderscore.executor.stubs;

import java.util.concurrent.CountDownLatch;

/**
 * Added a task that waits and blocks at certain key points.
 *
 * @author Matt Champion
 * @since 0.1.2
 */
public final class BlockingTask implements Runnable
{
    private final CountDownLatch blockingLatch;
    private final CountDownLatch waitingLatch;
    private final CountDownLatch completedLatch;
    private volatile boolean ran;
    private volatile boolean interrupted;

    /**
     * Constructor for BlockingTask.
     *
     * @param blockingLatch
     *            The latch is released when the task has begun execution.
     * @param waitingLatch
     *            This latch is waited on inside the task and must be counted down on outside of it.
     * @param completedLatch
     *            This latch is released when the task has finished (in a finally block).
     */
    public BlockingTask(final CountDownLatch blockingLatch, final CountDownLatch waitingLatch,
            final CountDownLatch completedLatch)
    {
        this.blockingLatch = blockingLatch;
        this.waitingLatch = waitingLatch;
        this.completedLatch = completedLatch;
        ran = false;
        interrupted = false;
    }

    @Override
    public void run()
    {
        try
        {
            ran = true;
            blockingLatch.countDown();
            waitingLatch.await();
        }
        catch (final InterruptedException e)
        {
            interrupted = true;
        }
        finally
        {
            completedLatch.countDown();
        }
    }

    /**
     * Used to test if the task started running. Set to true in the first statement of the task.
     * @return true if the task started running.
     */
    public boolean hasRun()
    {
        return ran;
    }

    /**
     * Used to test if the task was interrupted while waiting. Set to true in a catch block for
     * interrupted exceptions.
     * @return true if interrupted.
     */
    public boolean isIterrupted()
    {
        return interrupted;
    }
}
