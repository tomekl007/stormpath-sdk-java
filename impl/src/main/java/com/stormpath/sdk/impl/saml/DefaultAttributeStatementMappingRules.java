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
package com.stormpath.sdk.impl.saml;

import com.stormpath.sdk.impl.ds.InternalDataStore;
import com.stormpath.sdk.impl.resource.AbstractInstanceResource;
import com.stormpath.sdk.impl.resource.Property;
import com.stormpath.sdk.impl.resource.SetProperty;
import com.stormpath.sdk.saml.AttributeStatementMappingRule;
import com.stormpath.sdk.saml.AttributeStatementMappingRules;

import java.util.Map;
import java.util.Set;

/**
 * @since 1.0.RC8
 */
public class DefaultAttributeStatementMappingRules extends AbstractInstanceResource implements AttributeStatementMappingRules {

    private static final SetProperty<AttributeStatementMappingRule> ITEMS = new SetProperty<AttributeStatementMappingRule>("items", AttributeStatementMappingRule.class);

    private static final String ITEMS_PROPERTY_NAME = "ITEMS";

    static final Map<String,Property> PROPERTY_DESCRIPTORS = createPropertyDescriptorMap(ITEMS);

    public DefaultAttributeStatementMappingRules(InternalDataStore dataStore) {
        super(dataStore);
    }

    public DefaultAttributeStatementMappingRules(InternalDataStore dataStore, Map<String, Object> properties) {
        super(dataStore, properties);
    }

    @Override
    public Map<String, Property> getPropertyDescriptors() {
        return PROPERTY_DESCRIPTORS;
    }

    @Override
    public Set<AttributeStatementMappingRule> getAttributeStatementMappingRules() {
        return getSetProperty(ITEMS_PROPERTY_NAME);
    }

    @Override
    public void setAttributeStatementMappingRules(Set<AttributeStatementMappingRule> attributeStatementMappingRules) {
        setProperty(ITEMS, attributeStatementMappingRules);
    }
}
