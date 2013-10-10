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

import java.util.concurrent.ExecutionException;

import net.jcip.annotations.Immutable;

/**
 * Wrap the result of a task execution into a single object.
 * <P>
 * The result of a task execution is either an object or an exception. This will wrap the exception
 * in a {@link ExecutionException}. This is a tuple, it is to make storage of the result easier.
 * 
 * @author Matt Champion
 * @param <V> The type of object returned by the task
 * @since 0.1.0
 */
@Immutable
/* package */final class TaskExecutionResult<V>
{
    /**
     * The result of the execution.
     */
    public final V result;
    /**
     * The execution thrown by the execution wrapped in an {@link ExecutionException}.
     */
    public final ExecutionException exception;

    /**
     * Create a result from an exception.
     * @param result The execution thrown by the execution
     */
    public TaskExecutionResult(Throwable t)
    {
        this.result = null;
        this.exception = new ExecutionException(t);
    }

    /**
     * Create a result from an object.
     * @param result The result of the execution
     */
    public TaskExecutionResult(V result)
    {
        this.result = result;
        this.exception = null;
    }
}
