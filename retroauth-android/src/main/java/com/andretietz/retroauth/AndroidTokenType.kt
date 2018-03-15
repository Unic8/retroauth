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


package com.andretietz.retroauth

import android.content.Context

/**
 * represents a token bound to an account
 */
data class AndroidTokenType(val accountType: String, val tokenType: String) {

    class Factory private constructor(private val context: Context) : TokenTypeFactory<AndroidTokenType> {
        override fun create(annotationValues: IntArray): AndroidTokenType {
            return AndroidTokenType(context.getString(annotationValues[0]), context.getString(annotationValues[1]))
        }

        companion object {
            @JvmStatic
            fun create(context: Context): Factory = Factory(context)
        }
    }
}
