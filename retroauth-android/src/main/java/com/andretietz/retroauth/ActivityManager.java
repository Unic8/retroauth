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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * The {@link ActivityManager} provides an application {@link android.content.Context} as well as an {@link Activity} if
 * this was not stopped already. It registers {@link ActivityLifecycleCallbacks} to be able to know if there's an active
 * {@link Activity} or not. The {@link Activity} is required in case the user calls an {@link Authenticated} request
 * and there are not tokens provided, to be able to open the {@link Activity} for login, using the
 * {@link android.accounts.AccountManager#getAuthToken(android.accounts.Account, String, Bundle, Activity,
 * android.accounts.AccountManagerCallback, android.os.Handler)}. If you don't provide an {@link Activity} there, the
 * login screen wont open. So in case you're calling an {@link Authenticated} request from a {@link android.app.Service}
 * there will be no Login if required.
 */
@SuppressWarnings("Singleton")
public final class ActivityManager {

    private static final String TAG = ActivityManager.class.getSimpleName();
    @SuppressLint("StaticFieldLeak")
    private static ActivityManager instance;
    private final Application application;
    private final LifecycleHandler handler;

    private ActivityManager(@NonNull Application application) {
        this.application = application;
        handler = new LifecycleHandler();
        application.registerActivityLifecycleCallbacks(handler);
    }

    /**
     * @param application some {@link Activity} to be able to create the instance
     * @return a singleton instance of the {@link ActivityManager}.
     */
    @NonNull
    public static ActivityManager get(@NonNull Application application) {
        if (instance == null) {
            synchronized (ActivityManager.class) {
                if (instance == null) {
                    instance = new ActivityManager(application);
                }
            }
        }
        return instance;
    }

    /**
     * @return an {@link Activity} if there's one available. If not this method returns {@code null}
     */
    @Nullable
    public Activity getActivity() {
        synchronized (this) {
            Activity current = handler.getCurrent();
            if (current == null) {
                Log.w(TAG, "Requesting activity, when there is no active one");
            }
            return current;
        }
    }

    /**
     * An implementation of {@link ActivityLifecycleCallbacks} which stores a reference to the {@link Activity} as long as
     * it is not stopped. If the {@link Activity} is stopped, the reference will be removed.
     */
    private static class LifecycleHandler implements ActivityLifecycleCallbacks {
        private static final String TAG = LifecycleHandler.class.getSimpleName();
        private final WeakActivityStack activityStack = new WeakActivityStack();

        LifecycleHandler() {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }


        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            activityStack.push(activity);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityStack.remove(activity);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Nullable
        Activity getCurrent() {
            return activityStack.peek();
        }
    }
}
