/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native

import org.gradle.api.attributes.Category
import org.gradle.api.attributes.Usage
import org.jetbrains.kotlin.gradle.plugin.categoryByName
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf
import org.jetbrains.kotlin.gradle.targets.KotlinTargetSideEffect

internal val ConfigureFrameworkExportSideEffect = KotlinTargetSideEffect<KotlinNativeTarget> { target ->
    val project = target.project

    target.compilations.all {
        // Allow resolving api configurations directly to be able to check that
        // all exported dependency are also added in the corresponding api configurations.
        // The check is performed during a link task execution.
        project.configurations.maybeCreate(it.apiConfigurationName).apply {
            isCanBeResolved = true
            usesPlatformOf(target)
            attributes.attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.consumerApiUsage(target))
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.categoryByName(Category.LIBRARY))
        }
    }

    target.binaries.withType(AbstractNativeLibrary::class.java).all { framework ->
        project.configurations.maybeCreate(framework.exportConfigurationName).apply {
            isVisible = false
            isTransitive = false
            isCanBeConsumed = false
            isCanBeResolved = true
            usesPlatformOf(target)
            attributes.attribute(Usage.USAGE_ATTRIBUTE, KotlinUsages.consumerApiUsage(target))
            attributes.attribute(Category.CATEGORY_ATTRIBUTE, project.categoryByName(Category.LIBRARY))
            description = "Dependenceis to be exported in framework ${framework.name} for target ${target.targetName}"
        }
    }
}
