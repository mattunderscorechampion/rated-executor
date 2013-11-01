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

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mattunderscore.executors.ITaskWrapper;

/**
 * The task queue for a rated executor.
 * <P>
 * Tracks the order of the execution of tasks and the currently executing task.
 * 
 * @author Matt Champion
 * @since 0.1.1
 */
/* package */final class TaskQueue
{
    private final Queue<ITaskWrapper> taskQueue = new LinkedBlockingQueue<ITaskWrapper>();
    private volatile ITaskWrapper currentTask;

    /**
     * Get the next task and update the currently executing task.
     * 
     * @return The current task
     * @since 0.1.1
     */
    public ITaskWrapper poll()
    {
        final ITaskWrapper task = taskQueue.poll();
        currentTask = task;
        return task;
    }

    /**
     * Add a task to the end of the queue.
     * 
     * @param wrapper
     *            The task to add
     * @since 0.1.1
     */
    public void add(ITaskWrapper wrapper)
    {
        taskQueue.add(wrapper);
    }

    /**
     * Remove a task from the queue.
     * 
     * @param wrapper
     *            The task to remove
     * @since 0.1.1
     */
    public void remove(ITaskWrapper wrapper)
    {
        taskQueue.remove(wrapper);
    }

    /**
     * If the queue currently contain any tasks aside from the executing task.
     * 
     * @return True if the queue is empty
     * @since 0.1.1
     */
    public boolean isEmpty()
    {
        return taskQueue.isEmpty();
    }

    /**
     * Remove the currently executing task
     * 
     * @since 0.1.1
     */
    public void clearCurrentTask()
    {
        currentTask = null;
    }

    /**
     * Test if a task is the current task.
     * 
     * @param task
     *            The task to test
     * @return True if the task is currently executing
     * @since 0.1.1
     */
    public boolean isCurrentTask(final ITaskWrapper task)
    {
        if (task == null)
        {
            throw new NullPointerException();
        }
        return currentTask == task;
    }
}
