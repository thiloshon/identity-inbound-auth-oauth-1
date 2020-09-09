/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.oauth.common;

import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.as.validator.TokenValidator;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import static org.wso2.carbon.identity.oauth.common.OAuthConstants.OAuth20Params.CLIENT_ID;
import static org.wso2.carbon.identity.oauth.common.OAuthConstants.OAuth20Params.REQUEST;
import static org.wso2.carbon.identity.oauth.common.OAuthConstants.OAuth20Params.REQUEST_URI;
import static org.wso2.carbon.identity.oauth.common.OAuthConstants.OAuth20Params.RESPONSE_TYPE;
import static org.wso2.carbon.identity.oauth.common.OAuthConstants.OAuth20Params.SCOPE;

/**
 * Validator for hybrid flow code token requests
 */
public class CodeTokenResponseValidator extends TokenValidator {

    public CodeTokenResponseValidator() {

    }

    /**
     * Method to check whether the scope parameter string contains 'openid' as a scope.
     *
     * @param scope
     * @return
     */
    private static boolean isContainOIDCScope(String scope) {

        String[] scopeArray = scope.split("\\s+");
        for (String anyScope : scopeArray) {
            if (anyScope.equals(OAuthConstants.Scope.OPENID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void validateRequiredParameters(HttpServletRequest request) throws OAuthProblemException {

        if (!StringUtils.isBlank(request.getParameter(REQUEST_URI))) {
            requiredParams = new ArrayList<>(Arrays.asList(CLIENT_ID, RESPONSE_TYPE, SCOPE, REQUEST_URI));
            notAllowedParams.add(REQUEST);
        }

        super.validateRequiredParameters(request);

        // For code token response type, the scope parameter should contain 'openid' as one of the scopes.
        String openIdScope = request.getParameter(SCOPE);
        if (StringUtils.isBlank(openIdScope) || !isContainOIDCScope(openIdScope)) {
            String clientID = request.getParameter(CLIENT_ID);
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_REQUEST)
                    .description("Request with \'client_id\' = \'" + clientID + "\' has " +
                            "\'response_type\' for \'hybrid flow\'; but \'openid\' scope not found.");
        }
    }

    @Override
    public void validateMethod(HttpServletRequest request) throws OAuthProblemException {

        String method = request.getMethod();
        if (!OAuth.HttpMethod.GET.equals(method) && !OAuth.HttpMethod.POST.equals(method)) {
            throw OAuthProblemException.error(OAuthError.CodeResponse.INVALID_REQUEST)
                    .description("Method not correct.");
        }
    }

    @Override
    public void validateContentType(HttpServletRequest request) throws OAuthProblemException {

    }
}
