package org.michaelevans.aftermath

import com.squareup.javapoet.*
import com.squareup.javapoet.TypeName.INT
import com.squareup.javapoet.TypeName.VOID
import com.vishnurajeevan.javapoet.dsl.classType
import com.vishnurajeevan.javapoet.dsl.model.JavaPoetValue
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PUBLIC

internal class BindingClass(private val classPackage: String,
                            private val className: String,
                            private val targetClass: String) {
  private val bindings = hashMapOf<Int, MutableMap<Int, MethodBinding>>()

  fun createAndAddResultBinding(element: Element, annotationClass: String) {
    val binding = MethodBinding.Companion.newInstance(element, annotationClass)
    var methodBindings: MutableMap<Int, MethodBinding>? = bindings[binding.type]
    if (methodBindings == null) {
      methodBindings = HashMap<Int, MethodBinding>()
      bindings.put(binding.type, methodBindings)
    }

    if (methodBindings.containsKey(binding.requestCode)) {
      throw IllegalStateException("Duplicate attr assigned for field ${binding.name} " +
                                  "and ${methodBindings[binding.requestCode]?.name}")
    }
    else {
      methodBindings.put(binding.requestCode, binding)
    }
  }

  fun generateAftermath(): TypeSpec {
    return classType(PUBLIC, className) {
      parameterizedTypes.add(TypeVariableName.get("T", ClassName.get(classPackage, targetClass)))
      val callback = ClassName.get("org.michaelevans.aftermath", "IAftermathDelegate")
      implements.add(ParameterizedTypeName.get(callback, TypeVariableName.get("T")))

      method(PUBLIC, VOID, "onActivityResult",
             setOf(JavaPoetValue(FINAL, TypeVariableName.get("T"), "target"),
                   JavaPoetValue(FINAL, INT, "requestCode"),
                   JavaPoetValue(FINAL, INT, "resultCode"),
                   JavaPoetValue(FINAL, ClassName.get("android.content", "Intent"), "data"))) {
        
        annotations = setOf(AnnotationSpec.builder(Override::class.java).build())

        val methodBindings = bindings[MethodBinding.Companion.onActivityResult]
        methodBindings?.let {
          controlFlow {
            val format = "target.\$L(resultCode, data)"
            it.values.first()
                    .let {
                      begin("if(requestCode == \$L)", args = arrayOf(it.requestCode)) {
                        statement(format, it.name)
                      }
                    }

            it.values.toList()
                    .takeLast(it.values.size - 1)
                    .forEach {
                      next("else if (requestCode == \$L)", args = arrayOf(it.requestCode)) {
                        statement(format, it.name)
                      }
                    }
            end()
          }
        }
      }

      method(PUBLIC, VOID, "onRequestPermissionsResult",
             setOf(JavaPoetValue(FINAL, TypeVariableName.get("T"), "target"),
                   JavaPoetValue(FINAL, INT, "requestCode"),
                   JavaPoetValue(FINAL, Array<String>::class.java, "permissions"),
                   JavaPoetValue(FINAL, IntArray::class.java, "grantResults"))) {

        annotations = setOf(AnnotationSpec.builder(Override::class.java).build())

        val methodBindings = bindings[MethodBinding.Companion.onPermissionRequestResult]
        methodBindings?.let {
          controlFlow {
            val format = "target.\$L(permissions, grantResults)"
            it.values.first()
                    .let {
                      begin("if(requestCode == \$L)", args = arrayOf(it.requestCode)) {
                        statement(format, it.name)
                      }
                    }

            it.values.toList()
                    .takeLast(it.values.size - 1)
                    .forEach {
                      next("else if (requestCode == \$L)", args = arrayOf(it.requestCode)) {
                        statement(format, it.name)
                      }
                    }

            end()
          }
        }
      }
    }
  }

  @Throws(IOException::class)
  fun writeToFiler(filer: Filer) {
    JavaFile.builder(classPackage, generateAftermath()).build().writeTo(filer)
  }

  private class MethodBinding(element: Element,
                              internal val requestCode: Int,
                              internal val type: Int) {
    internal val name: String

    init {
      val executableElement = element as ExecutableElement
      name = executableElement.simpleName.toString()
    }

    companion object {

      var onActivityResult = 0
      var onPermissionRequestResult = 1

      fun newInstance(element: Element, annotationClass: String): MethodBinding {
        val requestCode: Int
        val type: Int
        if (annotationClass == OnActivityResult::class.java.simpleName) {
          val instance = element.getAnnotation(OnActivityResult::class.java)
          requestCode = instance.value
          type = MethodBinding.Companion.onActivityResult
        }
        else {
          val instance = element.getAnnotation(OnRequestPermissionResult::class.java)
          requestCode = instance.value
          type = MethodBinding.Companion.onPermissionRequestResult
        }
        return MethodBinding(element, requestCode, type)
      }
    }
  }

}
