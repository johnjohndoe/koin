/*
 * Copyright 2017-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.koin.androidx.scope

import android.app.Activity
import android.content.ComponentCallbacks
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import org.koin.android.ext.android.getKoin
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import org.koin.core.scope.Scope

/**
 * Provide Koin Scope tied to ComponentActivity
 */
@Deprecated("Delegate Lazy API is deprecatede. Please use AppCompatActivity.createActivityScope()")
fun ComponentActivity.activityScope() = LifecycleScopeDelegate<Activity>(this, this.getKoin())
@Deprecated("Delegate Lazy API is deprecatede. Please use AppCompatActivity.createActivityRetainedScope()")
fun ComponentActivity.activityRetainedScope() = LifecycleViewModelScopeDelegate(this, this.getKoin())
@Deprecated("Internal API not used anymore")
internal fun ComponentActivity.createScope(source: Any? = null): Scope = getKoin().createScope(getScopeId(), getScopeName(), source)
fun ComponentActivity.getScopeOrNull(): Scope? = getKoin().getScopeOrNull(getScopeId())

/**
 * Create Scope for AppCompatActivity, given it's extending AndroidScopeComponent.
 * Also register it in AndroidScopeComponent.scope
 */
fun AppCompatActivity.createActivityScope() {
    if (this !is AndroidScopeComponent) {
        error("Activity should implement AndroidScopeComponent")
    }
    if (this.scope != null) {
        error("Activity Scope is already created")
    }
    val scope = getKoin().getScopeOrNull(getScopeId()) ?: createScopeForCurrentLifecycle(this)
    this.scope = scope
}

internal fun ComponentCallbacks.createScopeForCurrentLifecycle(owner: LifecycleOwner): Scope {
    val scope = getKoin().createScope(getScopeId(), getScopeName(), this)
    owner.registerScopeForLifecycle(scope)
    return scope
}

internal fun LifecycleOwner.registerScopeForLifecycle(
    scope: Scope
) {
    lifecycle.addObserver(
        object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                scope.close()
            }
        }
    )
}

/**
 * Create Retained Scope for AppCompatActivity, given it's extending AndroidScopeComponent.
 * Also register it in AndroidScopeComponent.scope
 */
fun ComponentActivity.createActivityRetainedScope() {
    if (this !is AndroidScopeComponent) {
        error("Activity should implement AndroidScopeComponent")
    }
    if (this.scope != null) {
        error("Activity Scope is already created")
    }
    val scopeViewModel = viewModels<ScopeHandlerViewModel>().value
    if (scopeViewModel.scope == null) {
        val scope = getKoin().createScope(getScopeId(), getScopeName())
        scopeViewModel.scope = scope
    }
    this.scope = scopeViewModel.scope
}