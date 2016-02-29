package org.michaelevans.aftermath

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AftermathPlugin : Plugin<Project> {
    override fun apply(project : Project) {
        val transform = AftermathTransform(project)
        project.extensions.create("org.michaelevans.aftermath", AftermathExtension::class.java)
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(transform)
    }
}

