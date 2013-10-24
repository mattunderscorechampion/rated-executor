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

import java.util.concurrent.Future;

/**
 * Interface that allows the result of a future to be set.
 * <P>
 * The setters names are different because it is not possible to rely on parameter overloading as it
 * is possible to return an exception as the result of a {@link Callable}.
 * 
 * @author Matt Champion
 * @param <V>
 *            The type of object returned by the task
 * @since 0.1.0
 */
public interface ISettableFuture<V> extends Future<V>
{
    /**
     * Set the result of the task execution.
     * 
     * @param result
     *            The result of the execution
     */
    public void setResult(V result);

    /**
     * Set the result of the task execution.
     * 
     * @param result
     *            The execution thrown by the execution
     */
    public void setException(Throwable result);

    /**
     * Set the task
     * @param wrapper TaskWrapper for task of the future
     */
    public void setTask(ITaskWrapper wrapper);
}
