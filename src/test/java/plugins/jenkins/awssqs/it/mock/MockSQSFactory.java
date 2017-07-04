/*
 * Copyright 2017 Ribose Inc. <https://www.ribose.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package plugins.jenkins.awssqs.it.mock;

import com.amazonaws.services.sqs.AmazonSQS;
import io.relution.jenkins.awssqs.Context;
import io.relution.jenkins.awssqs.interfaces.SQSExecutorFactory;
import io.relution.jenkins.awssqs.interfaces.SQSFactory;
import io.relution.jenkins.awssqs.interfaces.SQSQueue;
import io.relution.jenkins.awssqs.interfaces.SQSQueueMonitor;
import io.relution.jenkins.awssqs.net.RequestFactory;
import io.relution.jenkins.awssqs.net.SQSChannel;
import io.relution.jenkins.awssqs.net.SQSChannelImpl;
import io.relution.jenkins.awssqs.threading.SQSQueueMonitorImpl;

import javax.inject.Inject;
import java.util.concurrent.ExecutorService;

public class MockSQSFactory implements SQSFactory {

    @Inject
    private RequestFactory factory;

    @Inject
    private SQSExecutorFactory SQSExecutorFactory;

    private final static MockSQSFactory instance = new MockSQSFactory();

    private MockSQSFactory() {
        Context.injector().injectMembers(this);
    }

    public static MockSQSFactory get() {
        return instance;
    }

    @Override
    public AmazonSQS createSQSAsync(SQSQueue queue) {
        return MockAwsSqs.get().getSqsClient();
    }

    @Override
    public AmazonSQS createSQSAsync(String accessKey, String secretKey) {
        return MockAwsSqs.get().getSqsClient();
    }

    @Override
    public SQSQueueMonitor createMonitor(ExecutorService executor, SQSQueue queue) {
        final AmazonSQS sqs = this.createSQSAsync(queue);
        final SQSChannel channel = new SQSChannelImpl(sqs, queue, this.factory);
        return new SQSQueueMonitorImpl(executor, queue, channel);
    }

    @Override
    public SQSQueueMonitor createMonitor(SQSQueueMonitor monitor, SQSQueue queue) {
        final SQSChannel channel = this.createChannel(queue);
        return monitor.clone(queue, channel);
    }

    private SQSChannel createChannel(final SQSQueue queue) {
        final AmazonSQS sqs = this.createSQSAsync(queue);
        return new SQSChannelImpl(sqs, queue, this.factory);
    }
}
