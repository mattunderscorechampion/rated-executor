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

import com.mattunderscore.executors.ITaskWrapper;

/**
 * The polling executor executes tasks no faster than a fixed rate.
 * <P>
 * It is not responsible for constructing futures, cancelling tasks or determining when to put
 * tasks on the queue. It is solely responsible for the timing of the execution of tasks.
 * 
 * @author Matt Champion
 * @since 0.1.1
 */
/*package*/ interface PollingExecutor
{
    /**
     * Submit a task to the polling executor.
     * 
     * @param wrapper
     *            The task to execute
     */
    public void submit(ITaskWrapper wrapper);

    /**
     * Stop the polling executor if it has no tasks to execute within its period. Any activity
     * within its period should prevent the stopping of the executor. The polling executor must
     * stop no sooner that the period of the task execution.
     */
    public void requestStop();

    /**
     * Stop the polling executor running. Calling this will prevent the polling executor from
     * trying to remove tasks from the queue. queue.
     */
    public void stop();
}