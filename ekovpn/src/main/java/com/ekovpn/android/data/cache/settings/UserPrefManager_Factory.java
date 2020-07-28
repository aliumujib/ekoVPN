/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

// Generated by Dagger (https://dagger.dev).
package com.ekovpn.android.data.cache.settings;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class UserPrefManager_Factory implements Factory<UserPrefManager> {
  private final Provider<Context> contextProvider;

  public UserPrefManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserPrefManager get() {
    return newInstance(contextProvider.get());
  }

  public static UserPrefManager_Factory create(Provider<Context> contextProvider) {
    return new UserPrefManager_Factory(contextProvider);
  }

  public static UserPrefManager newInstance(Context context) {
    return new UserPrefManager(context);
  }
}
