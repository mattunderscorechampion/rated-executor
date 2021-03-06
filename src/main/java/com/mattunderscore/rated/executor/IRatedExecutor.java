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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.mattunderscore.executors.IRepeatingFuture;
import com.mattunderscore.executors.IUniversalExecutor;

/**
 * Interface for rated executors, it will execute tasks at a fixed rate.
 * <P>
 * Tasks, both {@link Runnable} and {@link Callable}, can be submitted for execution.
 * {@link Runnable} tasks can also be scheduled for repeated execution. Rated executors should not
 * execute tasks more frequently than a fixed rate.
 * <P>
 * When a {@link Runnable} or {@link Callable} task is submitted it is placed on a queue. Tasks will
 * be removed from the queue at a fixed rate. A repeating task will be placed back on the queue when
 * complete. If a single repeating task is submitted it will be executed at an interval equal to the
 * rate of the executor. If two repeating tasks are submitted to the executor each task will execute
 * at an interval twice the executor rate and their execution will be separated from each other by
 * an interval equal to the rate of the executor.
 * 
 * @author Matt Champion
 * @since 0.0.1
 */
public interface IRatedExecutor extends IUniversalExecutor
{
    /**
     * Submit a task to be executed once.
     * <P>
     * This task will be executed as soon as possible without exceeding the rate limit. A future
     * will be returned to allow the task to be cancelled and monitored.
     * 
     * @param task
     *            Task to execute
     * @return Future that allows the task to be cancelled and monitored
     * @since 0.0.1
     */
    public Future<?> submit(Runnable task);

    /**
     * Submit a task to be executed once.
     * <P>
     * This task will be executed as soon as possible without exceeding the rate limit. A future
     * will be returned to allow the task to be cancelled and monitored.
     * 
     * @param task
     *            Task to execute
     * @return Future that allows the task to be cancelled and monitored
     * @since 0.0.2
     */
    public <V> Future<V> submit(Callable<V> task);

    /**
     * Submit a task to be executed repeatedly.
     * <P>
     * This task will be executed as soon as possible without exceeding the rate limit. A future
     * will be returned to allow the task to be cancelled and monitored. This task will be repeated
     * until it is cancelled.
     * 
     * @param task
     *            Task to execute
     * @return Future that allows the task to be cancelled and monitored
     * @since 0.0.1
     */
    public Future<?> schedule(Runnable task);

    /**
     * Submit a task to be executed repeatedly.
     * <P>
     * This task will be executed as soon as possible without exceeding the rate limit. A future
     * will be returned to allow the task to be cancelled and monitored. This task will be repeated
     * a limited number of times or until it is cancelled.
     * 
     * @param task
     *            Task to execute
     * @param repetitions
     *            The number of times the task will be executed
     * @return Future that allows the task to be cancelled and monitored
     * @since 0.1.0
     */
    public IRepeatingFuture<?> schedule(Runnable task, int repetitions);
    
    /**
     * Submit a task to be executed repeatedly.
     * <P>
     * This task will be executed as soon as possible without exceeding the rate limit. A future
     * will be returned to allow the task to be cancelled and monitored. This task will be repeated
     * a limited number of times or until it is cancelled.
     *
     * @param task Task to execute
     * @param repetitions The number of times the task will be executed
     * @return Future that allows the task to be cancelled and monitored
     * @since 0.1.0
     */
    public <V> IRepeatingFuture<V> schedule(Callable<V> task, int repetitions);
}
