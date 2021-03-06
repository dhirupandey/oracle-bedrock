/*
 * File: JprofilerProfileTest.java
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

package com.oracle.bedrock.jprofiler;

import applications.WaitingApplication;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.Freeform;
import com.oracle.bedrock.runtime.java.options.Freeforms;
import com.oracle.bedrock.runtime.options.Console;
import org.junit.After;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.InetAddress;
import java.util.Iterator;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Jonathan Knight
 */
public class JprofilerProfileTest
{
    /**
     * The default JProfiler agent on OSX.
     * If not running this test on OSX and on a system with JProfiler then
     * set the bedrock.profile.jprofiler System property to the location
     * of the JProfiler agent.
     */
    public static final String DEFAULT_AGENT =
        "/Applications/JProfiler.app/Contents/Resources/app/bin/macos/libjprofilerti.jnilib";

    /**
     * A JUnit rule to create temporary folders.
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();


    @After
    public void cleanupProperties()
    {
        System.clearProperty("bedrock.profile.jprofiler");
    }


    @Test
    public void shouldCreateEnabledProfileWithDefaultAddress() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib");

        assertThat(profile.isEnabled(), is(true));

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849");

        JprofilerProfile.ListenAddress address = optionsByType.get(JprofilerProfile.ListenAddress.class);

        assertThat(address.getInetAddress(), is(nullValue()));
        assertThat(address.getPort().get(), is(8849));
    }


    @Test
    public void shouldCreateEnabledNoWaitProfile() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabledNoWait("mylib");

        assertThat(profile.isEnabled(), is(true));

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849", "nowait");
    }


    @Test
    public void shouldAddNoWait() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").noWait();

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849", "nowait");
    }


    @Test
    public void shouldAddVerbose() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").verbose(true);

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849", "verbose-instr");
    }


    @Test
    public void shouldSetSamplingStack() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").samplingStack(1234);

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849", "samplingstack=1234");
    }


    @Test
    public void shouldSetDefaultSamplingStack() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").samplingStack(1234).defaultSamplingStack();

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849");
    }


    @Test
    public void shouldSetStack() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").stack(1234);

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849", "stack=1234");
    }


    @Test
    public void shouldSetDefaultStack() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").stack(1234).defaultStack();

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849");
    }


    @Test
    public void shouldCreateDisabledProfile() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.disabled();

        assertThat(profile.isEnabled(), is(false));

        profile.onLaunching(platform, metaClass, optionsByType);

        Freeforms freeforms = optionsByType.get(Freeforms.class);

        assertThat(freeforms, is(notNullValue()));
        assertThat(freeforms.iterator().hasNext(), is(false));
    }


    @Test
    public void shouldAddJniInterception() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").jniInterception(true);

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849", "jniInterception");
    }


    @Test
    public void shouldSetListenMode() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").listenMode();

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "port=8849");
    }


    @Test
    public void shouldSetListenModeWithSpecificAddress() throws Exception
    {
        Platform                       platform      = LocalPlatform.get();
        MetaClass                      metaClass     = new JavaApplication.MetaClass();
        OptionsByType                  optionsByType = OptionsByType.empty();

        InetAddress                    inetAddress   = InetAddress.getLocalHost();
        int                            port          = 9000;
        JprofilerProfile.ListenAddress address       = new JprofilerProfile.ListenAddress(inetAddress, port);

        JprofilerProfile               profile       = JprofilerProfile.enabled("mylib").listenMode(address);

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=", "address=" + inetAddress.getHostName(), "port=" + port);

        JprofilerProfile.ListenAddress result = optionsByType.get(JprofilerProfile.ListenAddress.class);

        assertThat(result, is(address));
    }


    @Test
    public void shouldSetOfflineModeWithFileAndSession() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        File             file          = new File("config.xml");
        int              session       = 1234;
        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").offlineMode(file, session);

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=offline", "id=1234", "nowait", "config=config.xml");
    }


    @Test
    public void shouldSetOfflineModeWithSession() throws Exception
    {
        Platform         platform      = LocalPlatform.get();
        MetaClass        metaClass     = new JavaApplication.MetaClass();
        OptionsByType    optionsByType = OptionsByType.empty();

        int              session       = 1234;
        JprofilerProfile profile       = JprofilerProfile.enabled("mylib").offlineMode(session);

        profile.onLaunching(platform, metaClass, optionsByType);

        assertAgentString(optionsByType, "-agentpath:mylib=offline", "id=1234", "nowait");
    }


    public void assertAgentString(OptionsByType optionsByType,
                                  String        agent,
                                  String...     expectedValues)
    {
        Freeforms freeforms = optionsByType.get(Freeforms.class);

        assertThat(freeforms, is(notNullValue()));
        assertThat(freeforms.iterator().hasNext(), is(true));

        Freeform         freeform = freeforms.iterator().next();
        Iterator<String> iterator = freeform.resolve(optionsByType).iterator();

        assertThat(iterator.hasNext(), is(true));

        String   agentString = iterator.next();
        String[] parts       = agentString.split(",");

        assertThat(parts[0], is(agent));

        assertThat(parts.length, is(expectedValues.length + 1));

        for (int i = 0; i < expectedValues.length; i++)
        {
            assertThat(parts[i + 1], is(expectedValues[i]));
        }
    }


    @Test
    public void shouldRunWithJProfilerProfileAsOption() throws Exception
    {
        String agent = System.getProperty("test.jprofiler.agent", DEFAULT_AGENT);

        // If the JProfiler agent does not exist the we cannot run this test
        Assume.assumeThat("Skipping test as JProfiler agent does not exist", new File(agent).exists(), is(true));

        // Create a schema to run the SleepingApplication with a JProfilerProfile

        JprofilerProfile profile = JprofilerProfile.enabled(agent).noWait();

        assertApplicationUsesJProfiler(ClassName.of(WaitingApplication.class), profile);
    }


    @Test
    public void shouldRunWithJProfilerProfileAsSystemProperty() throws Exception
    {
        String agent = System.getProperty("test.jprofiler.agent", DEFAULT_AGENT);

        // If the JProfiler agent does not exist the we cannot run this test
        Assume.assumeThat("Skipping test as JProfiler agent does not exist", new File(agent).exists(), is(true));

        // define the JProfiler profile as a System Property
        System.getProperties().setProperty("bedrock.profile.jprofiler", agent);

        assertApplicationUsesJProfiler(ClassName.of(WaitingApplication.class));
    }


    private void assertApplicationUsesJProfiler(Option... options) throws Exception
    {
        // The console to capture application output to verify JProfiler starts
        CapturingApplicationConsole console       = new CapturingApplicationConsole();

        OptionsByType               launchOptions = OptionsByType.of(options);

        launchOptions.add(Console.of(console));

        try (JavaApplication application = LocalPlatform.get().launch(JavaApplication.class, launchOptions.asArray()))
        {
            // assert the JProfiler is initialized
            Eventually.assertThat(invoking(console).getCapturedErrorLines(), hasItem("JProfiler> VM initialized"));

            // Run the createSnapshot callable and verify JProfiler creates a snapshot
            File                 folder   = temporaryFolder.newFolder();
            File                 snapshot = new File(folder, "test.jps");
            RemoteCallable<Void> callable = JProfiler.saveSnapshot(snapshot);

            application.submit(callable).join();

            assertThat(snapshot.exists(), is(true));

            // Wake up the application so that it terminates
            application.submit(new WaitingApplication.Terminate());
        }
    }
}
