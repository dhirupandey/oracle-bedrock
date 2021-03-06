/*
 * File: OutputToOutputStreamRedirectorTest.java
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

package com.oracle.bedrock.runtime.console;

import com.oracle.bedrock.runtime.ApplicationConsole;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Jonathan Knight
 */
public class OutputToOutputStreamRedirectorTest
{
    @Test
    public void shouldRedirectOutput() throws Exception
    {
        ApplicationConsole             console    = mock(ApplicationConsole.class);
        ByteArrayOutputStream          out        = new ByteArrayOutputStream(1000);
        PipedOutputStream              pipe       = new PipedOutputStream();
        PipedInputStream               in         = new PipedInputStream(pipe);
        OutputToOutputStreamRedirector redirector = new OutputToOutputStreamRedirector(out);
        
        redirector.start("foo", "x", in, console, 1234, false);
        redirector.awaitRunning(1, TimeUnit.MINUTES);

        pipe.write("Hello ".getBytes());
        pipe.write("World!".getBytes());
        pipe.flush();
        pipe.close();
        
        redirector.join();

        assertThat(new String(out.toByteArray()), is("Hello World!"));
    }

}
