/*
 * Copyright 2014 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.sdk.resource;

import com.stormpath.sdk.error.StormpathError;
import com.stormpath.sdk.lang.Assert;

/**
 * @since 0.2
 */
public class ResourceException extends RuntimeException implements StormpathError {

    private final StormpathError stormpathError;

    /**
     * Ensures the message used for the exception (i.e. exception.getMessage()) reports the {@code developerMessage}
     * returned by the Stormpath API Server.  The regular Stormpath response body {@code message} field is targeted
     * at applicadtion end-users that could very likely be non-technical.  Since an exception should be helpful to
     * developers, it is better to show a more technical message.
     * <p/>
     * Added as a fix for <a href="https://github.com/stormpath/stormpath-sdk-java/issues/28">Issue #28</a>.
     *
     * @param stormpathError the response StormpathError. Cannot be null.
     * @return {@code stormpathError.getDeveloperMessage()}
     * @since 0.9.2
     */
    private static String buildExceptionMessage(StormpathError stormpathError) {
        Assert.notNull(stormpathError, "StormpathError argument cannot be null.");
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP ").append(stormpathError.getStatus())
                .append(", Stormpath ").append(stormpathError.getCode())
                .append(" (").append(stormpathError.getMoreInfo()).append("): ")
                .append(stormpathError.getDeveloperMessage());
        return sb.toString();
    }

    public ResourceException(StormpathError stormpathError) {
        super(buildExceptionMessage(stormpathError));
        this.stormpathError = stormpathError;
    }

    @Override
    public int getStatus() {
        return stormpathError.getStatus();
    }

    @Override
    public int getCode() {
        return stormpathError.getCode();
    }

    @Override
    public String getDeveloperMessage() {
        return stormpathError.getDeveloperMessage();
    }

    @Override
    public String getMoreInfo() {
        return stormpathError.getMoreInfo();
    }

    public StormpathError getStormpathError() {
        return this.stormpathError;
    }

    /**
     * Returns the underlying REST {@link com.stormpath.sdk.error.StormpathError} returned from the Stormpath API server.
     * <p/>
     * Because this class's {@link #getMessage() getMessage()} value returns a developer-friendly message to help you
     * debug when you see stack traces, you might want to acquire the underlying {@code StormpathError} to show an end-user
     * the simpler end-user appropriate stormpathError message.  The end-user stormpathError message is non-technical in nature - as a
     * convenience, you can show this message directly to your application end-users.
     * <p/>
     * For example:
     * <pre>
     * try {
     *
     *     //something that causes a ResourceException
     *
     * } catch (ResourceException re) {
     *
     *     String endUserMessage = re.getError().getMessage();
     *
     *     warningDialog.setText(endUserMessage);
     * }
     * </pre>
     *
     * @return the underlying REST {@link com.stormpath.sdk.error.StormpathError} resource representation returned from the Stormpath API server.
     * @since 0.10
     *
        public StormpathError getError() {
        return this.stormpathError;
    }*/
}
