package com.andretietz.retroauth;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * This interceptor intercepts the okhttp requests and checks if authentication is required.
 * If so, it tries to get the owner of the token, then tries to get the token and
 * applies the token to the request
 *
 * @param <OWNER>
 * @param <TOKEN_TYPE>
 */
public final class CredentialInterceptor<OWNER, TOKEN_TYPE, TOKEN> implements Interceptor {
    private final AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler;

    public CredentialInterceptor(AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler) {
        this.authHandler = authHandler;
    }

    @Override
    public synchronized Response intercept(Chain chain) throws IOException {
        Response response;
        Request request = chain.request();
        // get the token type required by this request
        TOKEN_TYPE type = authHandler.methodCache.getTokenType(Utils.createUniqueIdentifier(request));
        // if the request does require authentication
        if (type != null) {
            TOKEN token;
            int tryCount = 0;
            do {
                try {
                    // get the owner of the token
                    OWNER owner = authHandler.ownerManager.getOwner(type);
                    // get the token
                    token = authHandler.tokenStorage.getToken(owner, type);
                    // modify the request using the token
                    request = authHandler.provider.modifyRequest(request, token);
                    // execute the request
                    response = chain.proceed(request);
                } catch (Exception e) {
                    throw new AuthenticationCanceledException(e);
                }
            } while (authHandler.provider.retryRequired(++tryCount, response, token));
        } else {
            // no authentication required, proceed as usual
            response = chain.proceed(request);
        }
        return response;
    }
}
