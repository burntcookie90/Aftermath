package org.michaelevans.aftermath

import com.android.build.api.transform.*
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

class AftermathTransform(val project: Project) : Transform() {
  override fun getInputTypes() = mutableSetOf(QualifiedContent.DefaultContentType.CLASSES)
  override fun getName() = "aftermath"
  override fun getScopes() = mutableSetOf(QualifiedContent.Scope.PROJECT)
  override fun getReferencedScopes() = mutableSetOf(QualifiedContent.Scope.PROJECT)
  override fun isIncremental() = true

  override fun transform(context: Context,
                         inputs: MutableCollection<TransformInput>,
                         referencedInputs: MutableCollection<TransformInput>,
                         outputProvider: TransformOutputProvider,
                         isIncremental: Boolean) {
    referencedInputs.forEach {
      val outputDirectory = outputProvider.getContentLocation("aftermath", outputTypes, scopes, Format.DIRECTORY)

      it.directoryInputs.forEach {
        val inputFile = it.file
        var changed: FileCollection?
        if (isIncremental) {
          changed = project.files()
          it.changedFiles.forEach {
            println("file: ${it.component1()}")
            println("status: ${it.component2()}")
            if (it.component2() == Status.ADDED || it.component2() == Status.CHANGED) {
              changed = (changed as FileCollection) + project.files(it.component1())
            }
          }
        }
        else {
          changed = project.files()
          it.file.walkBottomUp()
                  .filter { it.name.contains("$\$Aftermath") }
                  .forEach {
                    changed = (changed as FileCollection) + project.files(it)
                  }
        }

        changed.forEach {
          
        }
      }
    }
  }
}

