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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A repeating future represents a repeated asynchronous computation.
 * <P>
 * A computation task has been scheduled to repeat a fixed number of times. This future allows the
 * result of each repetition to be retrieved and the progress thought repetitions is be monitored.
 * The {@link #get()} methods inherited from {@link Future} refer to the most recent execution.
 * 
 * @author Matt Champion
 * @param <V> The type of object returned by the task
 * @since 0.0.2
 */
public interface IRepeatingFuture<V> extends Future<V>
{
    /**
     * Waits if necessary for the ith computation to complete, and then retrieves its result.
     * 
     * @param i
     *            The execution of the task to get the result for
     * @return The result of the execution
     * @throws InterruptedException
     *             Thrown if the thread was interrupted while the waiting
     * @throws ExecutionException
     *             Thrown if the execution of the task resulted in an exception
     * @throws CancellationException
     *             Thrown if the task was cancelled before reaching the ith execution
     * @throws IndexOutOfBoundsException
     *             Thrown if the ith value is greater than the number of executions or it is less
     *             than 0.
     */
    public V getResult(int i) throws InterruptedException, ExecutionException,
            CancellationException, IndexOutOfBoundsException;

    /**
     * Waits if necessary for the ith computation to complete, and then retrieves its result.
     * 
     * @param i
     *            The execution of the task to get the result for
     * @param timeout
     *            The maximum time to wait
     * @param unit
     *            The time unit of the timeout argument
     * @return The result of the execution
     * @throws InterruptedException
     *             Thrown if the thread was interrupted while the waiting
     * @throws ExecutionException
     *             Thrown if the execution of the task resulted in an exception
     * @throws CancellationException
     *             Thrown if the task was cancelled before reaching the ith execution
     * @throws TimeoutException
     *             Thrown if the ith execution does not happen within the timeout period
     * @throws IndexOutOfBoundsException
     *             Thrown if the ith value is greater than the number of executions or it is less
     *             than 0.
     */
    public V getResult(int i, long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, CancellationException, TimeoutException, IndexOutOfBoundsException;

    /**
     * Get the number of executions that should happen.
     * 
     * @return Number of times the task will try to execute.
     */
    public int getExpectedExecutions();

    /**
     * Returns the number of times the task has run this allows progress through the repetitions to
     * be monitored.
     * 
     * @return Number of times the task has executed.
     */
    public int getCompletedExecutions();
}
