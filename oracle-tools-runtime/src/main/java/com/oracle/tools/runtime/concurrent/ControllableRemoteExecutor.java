/*
 * File: ControllableRemoteExecutor.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.tools.runtime.concurrent;

import java.io.Closeable;

/**
 * A {@link RemoteExecutor} that may have its lifecycle controlled and observed.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see RemoteExecutor
 */
public interface ControllableRemoteExecutor extends RemoteExecutor, Closeable
{
    /**
     * Closes the {@link ControllableRemoteExecutor}.
     * <p>
     * After being closed attempts to make submissions to the {@link ControllableRemoteExecutor}
     * will throw an {@link IllegalStateException}.  Should the {@link ControllableRemoteExecutor}
     * already be closed, nothing happens.
     */
    public void close();


    /**
     * Registers the specified {@link RemoteExecutorListener} on the {@link ControllableRemoteExecutor}.
     *
     * @param listener  the {@link RemoteExecutorListener}
     */
    public void addListener(RemoteExecutorListener listener);
}