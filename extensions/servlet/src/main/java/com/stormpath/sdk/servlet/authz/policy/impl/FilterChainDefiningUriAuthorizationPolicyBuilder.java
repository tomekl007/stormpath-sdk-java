/*
 * Copyright 2015 Stormpath, Inc.
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
package com.stormpath.sdk.servlet.authz.policy.impl;

import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.lang.Collections;
import com.stormpath.sdk.lang.Strings;
import com.stormpath.sdk.servlet.authz.policy.UriAuthorizationPolicyBuilder;
import com.stormpath.sdk.servlet.filter.DefaultFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FilterChainDefiningUriAuthorizationPolicyBuilder implements UriAuthorizationPolicyBuilder {

    public static final String NEGATION_BEFORE_AUTHENTICATED_MSG =
        "Negation (calling the 'not()' method) is not supported before specifying the 'authenticated' rule.  " +
        "If you need this functionality, please open an enhancement issue in the project issue tracker.";

    public static final String NEGATION_BEFORE_UNRESTRICTED_MSG =
        "Cannot negate the 'unrestricted' condition. 'not unrestricted' means 'restricted' and you " +
        "must indicate what those restrictions are by using other authorization rule methods " +
        "as necessary.";

    public static final String UNRESTRICTED_RULE_MSG =
        "The 'unrestricted' rule may not be combined with any other rule.";

    private boolean started = false;
    private boolean last = false;
    private boolean negateNext = false;
    private boolean unrestricted = false;
    private boolean accountRuleEnabled = false;

    private List<String> currentPatterns;
    private List<String> currentRules;

    private final Map<String, String> chainDefinitions;

    public FilterChainDefiningUriAuthorizationPolicyBuilder() {
        currentPatterns = new ArrayList<String>();
        currentRules = new ArrayList<String>();
        chainDefinitions = new LinkedHashMap<String, String>();
    }

    @Override
    public UriAuthorizationPolicyBuilder to(String... uriAntPatterns) {

        Assert.notEmpty(uriAntPatterns, "URI pattern argument(s) cannot be null or empty.");

        if (last) {
            throw new IllegalArgumentException("Cannot define more rules after 'toAnythingElse'");
        }

        completePreviousChain();

        List<String> patterns = new ArrayList<String>(uriAntPatterns.length);
        for(String pattern : uriAntPatterns) {
            Assert.hasText(pattern, "URI pattern array element cannot be null or whitespace.");
            patterns.add(pattern);
        }

        currentPatterns = patterns;
        started = true;

        return this;
    }

    private void completePreviousChain() {
        if (!started) {
            return;
        }
        doCompleteChain();
        reset();
    }

    private void completeRule() {
        negateNext = false;
        accountRuleEnabled = false;
    }

    private void reset() {
        currentPatterns = new ArrayList<String>();
        currentRules = new ArrayList<String>();
        unrestricted = false;
        negateNext = false;
        accountRuleEnabled = false;
    }

    protected void assertPatternsExist() {
        Assert.notEmpty(currentPatterns,
                        "You must specify one or more URI patterns first before defining an authorization rule.");
    }

    private void doCompleteChain() {
        assertPatternsExist();
        if (Collections.isEmpty(currentRules)) {
            throw new IllegalArgumentException("You must specify one or more authorization rules for URI(s) [" +
                                               Strings.collectionToCommaDelimitedString(currentPatterns) + "].");
        }

        StringBuilder chainDefinitionBuilder = new StringBuilder();
        for (String rule : currentRules) {
            if (chainDefinitionBuilder.length() != 0) {
                chainDefinitionBuilder.append(",");
            }
            chainDefinitionBuilder.append(rule);
        }

        String chainDefinition = chainDefinitionBuilder.toString();

        for (String pattern : currentPatterns) {
            chainDefinitions.put(pattern, chainDefinition);
        }
    }

    @Override
    public UriAuthorizationPolicyBuilder toAnythingElse() {
        Assert.isTrue(!last, "Cannot specify 'toAnythingElse' multiple times.");
        completePreviousChain();
        currentPatterns.add("/**");
        started = true;
        last = true;
        return this;
    }

    @Override
    public UriAuthorizationPolicyBuilder are() {
        started = true;
        assertPatternsExist();
        return this;
    }

    /*
    @Override
    public UriAuthorizationPolicyBuilder not() {
        started = true;
        assertPatternsExist();
        assertRestrictable();
        negateNext = !negateNext;
        return this;
    }
    */

    @Override
    public UriAuthorizationPolicyBuilder fromAccounts() {
        started = true;
        assertPatternsExist();
        assertRestrictable();
        currentRules.add(DefaultFilter.account.name());
        accountRuleEnabled = true;
        return this;
    }

    @Override
    public UriAuthorizationPolicyBuilder where(String authzExpression) {
        started = true;
        assertRestrictable();
        assertPatternsExist();

        String expr = Strings.clean(authzExpression);
        Assert.notNull(expr, "'where' expression cannot be null or empty whitespace.");

        if (accountRuleEnabled) {
            currentRules.remove(currentRules.size() - 1);
        }

        currentRules.add(DefaultFilter.account + "(" + expr + ")");

        completeRule();

        return this;
    }

    @Override
    public UriAuthorizationPolicyBuilder authenticated() {
        started = true;
        assertPatternsExist();
        assertRestrictable();

        // Implementation note:  This next assertion is because the AuthenticationFilter that will be utilized to
        // reflect this rule does not support negation.  That's because the filter only has an 'UnauthenticatedHandler'
        // component to use if the current request is unauthenticated and must be authenticated.
        //
        // To support a 'not authenticated' assertion we have to ensure that if the current request *is* authenticated
        // and the assertion says they should not be, then a corresponding 'AuthenticatedHandler' needs to be created
        // to support the logic of what happens when a currently authenticated account attempts to access a
        // 'not authenticated' protected URL (not to mention that 'AuthenticatedHandler' is misleading as it implies
        // what to do during authentication). That is an edge case that is currently out of scope until a
        // user has a strong case for why it might be necessary.
        //
        // Presumably the only time you might ever want to assert 'not authenticated' (and redirect the currently
        // authenticated user) is maybe for a login page so the already logged-in user can't attempt to login yet again.
        // However, this case is already covered in the LoginController logic: the already-authenticated account will
        // just immediately be redirected to the configured stormpath.web.login.nextUri location.
        Assert.isTrue(!negateNext, NEGATION_BEFORE_AUTHENTICATED_MSG);

        currentRules.add(DefaultFilter.authc.name());

        completeRule();

        return this;
    }

    private void assertRestrictable() {
        Assert.isTrue(!unrestricted, UNRESTRICTED_RULE_MSG);
    }

    @Override
    public UriAuthorizationPolicyBuilder unrestricted() {
        started = true;
        assertPatternsExist();
        Assert.isTrue(!negateNext, NEGATION_BEFORE_UNRESTRICTED_MSG);
        if (unrestricted) {
            return this;
        }
        Assert.isTrue(Collections.isEmpty(currentRules), UNRESTRICTED_RULE_MSG);

        currentRules.add(DefaultFilter.anon.name());
        unrestricted = true;

        completeRule();

        return this;
    }

    @Override
    public UriAuthorizationPolicyBuilder and() {
        started = true;
        assertPatternsExist();
        completeRule();
        return this;
    }

    public Map<String, String> getChainDefinitions() {
        if (!started) {
            throw new IllegalArgumentException("No patterns or rules have been specified.");
        }
        completePreviousChain();
        return chainDefinitions;
    }
}