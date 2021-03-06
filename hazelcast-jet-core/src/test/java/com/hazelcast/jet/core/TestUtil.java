/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.jet.core;

import com.hazelcast.jet.Job;
import com.hazelcast.jet.function.DistributedSupplier;
import com.hazelcast.jet.impl.util.ThrottleWrappedP;
import com.hazelcast.jet.impl.util.WrappingProcessorMetaSupplier;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.hazelcast.jet.impl.util.ExceptionUtil.peel;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public final class TestUtil {

    private TestUtil() {

    }

    public static void executeAndPeel(Job job) throws Throwable {
        try {
            job.join();
        } catch (Exception e) {
            throw peel(e);
        }
    }

    /**
     * Asserts that {@code caught} exception is equal to {@code expected} or one of its causes.
     * <p>
     * <p>Exceptions are considered equal, if their {@code message}s and classes are equal.
     *
     * @param expected Expected exception
     * @param caught   Caught exception
     */
    public static void assertExceptionInCauses(final Throwable expected, final Throwable caught) {
        // peel until expected error is found
        boolean found = false;
        Throwable t = caught;
        while (!found && t != null) {
            found = Objects.equals(t.getMessage(), expected.getMessage()) && t.getClass() == expected.getClass();
            t = t.getCause();
        }

        if (!found) {
            assertEquals("expected exception not found in causes chain", expected, caught);
        }
    }

    public static final class DummyUncheckedTestException extends RuntimeException {
    }

    @Nonnull
    public static ProcessorMetaSupplier throttle(@Nonnull DistributedSupplier<Processor> wrapped, long itemsPerSecond) {
        return new WrappingProcessorMetaSupplier(ProcessorMetaSupplier.of(wrapped), p -> new ThrottleWrappedP(p,
                itemsPerSecond));
    }

    @Nonnull
    public static ProcessorMetaSupplier throttle(@Nonnull ProcessorMetaSupplier wrapped, long itemsPerSecond) {
        return new WrappingProcessorMetaSupplier(wrapped, p -> new ThrottleWrappedP(p, itemsPerSecond));
    }

    /**
     * Create {@code HashSet} from a list of items.
     */
    public static <T> Set<T> set(T ... foo) {
        return new HashSet<>(asList(foo));
    }
}
