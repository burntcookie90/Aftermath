package org.michaelevans.aftermath

import com.google.common.truth.Truth.assertThat
import com.squareup.sqldelight.FixtureName
import com.squareup.sqldelight.FixtureRunner
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class AftermathPluginTest {
  @get:Rule val fixture = FixtureRunner()

  @FixtureName("works-fine")
  @Test
  fun worksFine() {
    val result = fixture.execute()
    println("Output : ${result.standardOutput} \n Error: ${result.standardError}")
    assertThat(result.standardOutput).contains("BUILD SUCCESSFUL")
    assertExpectedFiles()
  }

  private fun assertExpectedFiles() {
    val expectedDir = File(fixture.root(), "expected/").toPath()
    val outputDir = File(fixture.root(), "build/generated/source/aftermath/").toPath()
    Files.walkFileTree(expectedDir, object : SimpleFileVisitor<Path>() {
      override fun visitFile(expectedFile: Path, attrs: BasicFileAttributes): FileVisitResult {
        val relative = expectedDir.relativize(expectedFile).toString()
        val actualFile = outputDir.resolve(relative)
        if (!Files.exists(actualFile)) {
          throw AssertionError("Expected file not found: $actualFile")
        }

        val expected = String(Files.readAllBytes(expectedFile), UTF_8)
        val actual = String(Files.readAllBytes(actualFile), UTF_8)
        assertThat(actual).named(relative).isEqualTo(expected)

        return CONTINUE
      }
    })
  }
}