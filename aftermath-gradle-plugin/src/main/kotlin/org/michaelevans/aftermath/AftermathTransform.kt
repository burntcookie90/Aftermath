package org.michaelevans.aftermath

import com.android.build.api.transform.*
import com.squareup.javapoet.*
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.util.*
import javax.lang.model.element.Modifier

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
      val outputDirectory = outputProvider.getContentLocation("aftermath",
                                                              outputTypes,
                                                              scopes,
                                                              Format.DIRECTORY)
      val aftermathClasses = arrayListOf<String>()

      it.directoryInputs.forEach { findAftermaths(aftermathClasses, isIncremental, it) }

      val aftermathBuilder = TypeSpec.classBuilder("Aftermath")
              .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
              .addMethod(generateOnActivityResultMethod(aftermathClasses))

    }
  }

  private fun findAftermaths(aftermathClasses: ArrayList<String>,
                             isIncremental: Boolean,
                             it: DirectoryInput) {
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
              .forEach { changed = (changed as FileCollection) + project.files(it) }
    }

    changed?.forEach {
      aftermathClasses.add(it.name)
    }
  }

  private fun generateOnActivityResultMethod(aftermathClasses: List<String>): MethodSpec {
    val intentClass = ClassName.get("android.content", "Intent")
    val methodBuilder = MethodSpec.methodBuilder("onActivityResult")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(TypeName.VOID)
            .addParameter(ParameterSpec.builder(Object::class.java,
                                                "target",
                                                Modifier.FINAL).build())
            .addParameter(ParameterSpec.builder(TypeName.INT,
                                                "requestCode",
                                                Modifier.FINAL)
                                  .build())
            .addParameter(ParameterSpec.builder(TypeName.INT,
                                                "resultCode",
                                                Modifier.FINAL)
                                  .build())
            .addParameter(ParameterSpec.builder(intentClass,
                                                "data",
                                                Modifier.FINAL)
                                  .build())

    aftermathClasses.forEach {
    }

    return methodBuilder.build()
  }

}

