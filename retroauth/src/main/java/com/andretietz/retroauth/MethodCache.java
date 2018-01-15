/*
 * Copyright (c) 2016 Andre Tietz
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

package com.andretietz.retroauth;

import java.util.HashMap;

/**
 * This cache stores the unique hash of a request to identify it later on when
 * authenticating the request itself. The identifier is created right now
 * in {@link Utils#createUniqueIdentifier}, this may change.
 */
public interface MethodCache<TOKEN_TYPE> {

    /**
     * Registers a token type with a specific identifier.
     *
     * @param requestIdentifier to identify the request later on
     * @param type              type of token to bind to the requestIdentifier
     */
    void register(int requestIdentifier, TOKEN_TYPE type);

    /**
     * @param requestIdentifier the request identifier
     * @return the token type to authenticate the request
     */
    TOKEN_TYPE getTokenType(int requestIdentifier);


    /**
     * The default implementation of the {@link MethodCache}.
     *
     * @param <TOKEN_TYPE>
     */
    class DefaultMethodCache<TOKEN_TYPE> implements MethodCache<TOKEN_TYPE> {
        private final HashMap<Integer, TOKEN_TYPE> map = new HashMap<>();

        @Override
        public void register(int hash, TOKEN_TYPE type) {
            map.put(hash, type);
        }

        @Override
        public TOKEN_TYPE getTokenType(int hashCode) {
            return map.get(hashCode);
        }
    }
}
