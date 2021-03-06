/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.cache;

import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.TaskOutputsInternal;
import org.gradle.api.internal.tasks.CacheableTaskOutputFilePropertySpec;
import org.gradle.api.internal.tasks.TaskOutputFilePropertySpec;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Makes sure no files are left over among the outputs of the task before actually loading a cached result.
 */
public class OutputClearingTaskOutputPacker implements TaskOutputPacker {
    private final TaskOutputPacker delegate;

    public OutputClearingTaskOutputPacker(TaskOutputPacker delegate) {
        this.delegate = delegate;
    }

    @Override
    public void pack(TaskOutputsInternal taskOutputs, OutputStream output) throws IOException {
        delegate.pack(taskOutputs, output);
    }

    @Override
    public void unpack(TaskOutputsInternal taskOutputs, InputStream input) throws IOException {
        for (TaskOutputFilePropertySpec propertySpec : taskOutputs.getFileProperties()) {
            CacheableTaskOutputFilePropertySpec property = (CacheableTaskOutputFilePropertySpec) propertySpec;
            File output = property.getOutputFile();
            switch (property.getOutputType()) {
                case DIRECTORY:
                    FileUtils.forceMkdir(output);
                    FileUtils.cleanDirectory(output);
                    break;
                case FILE:
                    if (output.exists()) {
                        FileUtils.forceDelete(output);
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
        delegate.unpack(taskOutputs, input);
    }
}
