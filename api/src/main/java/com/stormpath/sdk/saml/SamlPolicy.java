/*
* Copyright 2015 Stormpath, Inc.
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
package com.stormpath.sdk.saml;

import com.stormpath.sdk.provider.SamlProvider;
import com.stormpath.sdk.resource.Resource;

/**
 * A SamlPolicy resource contains information about the SAML configuration for the parent {@link com.stormpath.sdk.application.Application}.
 *
 * @since 1.0.RC8
 */
public interface SamlPolicy extends Resource {

    /**
     * Returns the SAML Service Provider ({@link SamlProvider SamlProvier}) associated to this policy.
     * @return
     */
    SamlProvider getSamlProvider();
}
