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

import net.jcip.annotations.Immutable;

/**
 * Task result processor that discards any return value but invokes an uncaught exception handler for any throwables.
 * @author matt on 01/06/14.
 */
@Immutable
public final class UncaughtExceptionResult<V> implements ITaskResultProcessor<V> {
    public static final UncaughtExceptionResult VOID_RESULT_PROCESSOR = new UncaughtExceptionResult<Void>();

    public UncaughtExceptionResult() {
    }

    @Override
    public void onThrowable(ITaskWrapper task, Throwable t) {
        final Thread currentThread = Thread.currentThread();
        final Thread.UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        if (handler != null) {
            handler.uncaughtException(currentThread, t);
        }
    }

    @Override
    public void onResult(ITaskWrapper task, V result) {
    }
}
