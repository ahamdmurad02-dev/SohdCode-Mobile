package app.sohdcode.domain.execution

import app.sohdcode.domain.model.ExecutionResult
import app.sohdcode.domain.model.ProjectFile
import app.sohdcode.domain.model.SupportedLanguage
import org.mozilla.javascript.Context
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.ScriptableObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

interface CodeRuntime {
    fun run(language: String, files: List<ProjectFile>, entryPath: String?): ExecutionResult
    fun stop()
}

interface IsolatedSandbox {
    val id: String
    fun available(): Boolean
    fun execute(language: String, source: String): ExecutionResult
}

class FutureLinuxSandbox : IsolatedSandbox {
    override val id = "linux"
    override fun available() = false
    override fun execute(language: String, source: String) = ExecutionResult(
        false, "", "Linux sandbox support is not part of this version.", true
    )
}

class SandboxedCodeRuntime(private val linux: IsolatedSandbox = FutureLinuxSandbox()) : CodeRuntime {
    private val cancel = AtomicBoolean(false)
    override fun stop() { cancel.set(true) }

    override fun run(language: String, files: List<ProjectFile>, entryPath: String?): ExecutionResult {
        val lang = SupportedLanguage.fromId(language)
        val file = files.firstOrNull { it.path == entryPath } ?: files.firstOrNull()
            ?: return ExecutionResult(false, "", "This project has no files to run.")
        return when (lang) {
            SupportedLanguage.JAVASCRIPT -> runJavascript(file.content)
            SupportedLanguage.JSON -> runCatching {
                kotlinx.serialization.json.Json.parseToJsonElement(file.content)
                ExecutionResult(true, "JSON is valid.")
            }.getOrElse { ExecutionResult(false, "", "Invalid JSON: ${it.message}") }
            SupportedLanguage.MARKDOWN, SupportedLanguage.TEXT ->
                ExecutionResult(false, "", "${lang.displayName} cannot be executed.", true)
            SupportedLanguage.PYTHON, SupportedLanguage.KOTLIN, SupportedLanguage.JAVA ->
                if (linux.available()) linux.execute(lang.id, file.content)
                else ExecutionResult(false, "", "${lang.displayName} cannot be executed safely on-device in this version. A future Linux sandbox can implement IsolatedSandbox.", true)
        }
    }

    private fun runJavascript(source: String): ExecutionResult {
        if (source.isBlank()) return ExecutionResult(false, "", "Nothing to run. The file is empty.")
        if (source.length > 80_000) return ExecutionResult(false, "", "Source is too large to run on-device.")
        cancel.set(false)
        val ticks = AtomicInteger(0)
        val factory = object : ContextFactory() {
            override fun observeInstructionCount(cx: Context, instructionCount: Int) {
                if (cancel.get()) throw InterruptedException("Stopped")
                if (ticks.incrementAndGet() > 80) throw InterruptedException("Instruction limit reached")
            }
            override fun makeContext(): Context {
                val cx = super.makeContext()
                cx.instructionObserverThreshold = 5000
                cx.optimizationLevel = -1
                return cx
            }
        }
        val pool = Executors.newSingleThreadExecutor()
        return try {
            pool.submit<ExecutionResult> {
                val cx = factory.enterContext()
                cx.optimizationLevel = -1
                cx.setClassShutter { name ->
                    name.startsWith("org.mozilla.javascript.") && !name.contains("NativeJava") && !name.contains("JavaAdapter")
                }
                try {
                    val scope = cx.initSafeStandardObjects()
                    cx.evaluateString(scope, "var __sohdLogs=[];var console={log:function(){var p=[];for(var i=0;i<arguments.length;i++)p.push(String(arguments[i]));__sohdLogs.push(p.join(' '));}};", "prelude.js", 1, null)
                    cx.evaluateString(scope, source, "sandbox.js", 1, null)
                    val logs = ScriptableObject.getProperty(scope, "__sohdLogs") as? ScriptableObject
                    val n = (logs?.let { ScriptableObject.getProperty(it, "length") } as? Number)?.toInt() ?: 0
                    val text = (0 until n).joinToString("\n") { i -> ScriptableObject.getProperty(logs, i).toString() }
                    ExecutionResult(true, text.ifBlank { "(finished with no console output)" })
                } finally { Context.exit() }
            }.get(3, TimeUnit.SECONDS)
        } catch (t: java.util.concurrent.TimeoutException) {
            cancel.set(true)
            ExecutionResult(false, "", "Execution timed out after 3s.", true)
        } catch (t: Throwable) {
            ExecutionResult(false, "", t.message ?: "JavaScript execution failed.")
        } finally {
            cancel.set(false)
            pool.shutdownNow()
        }
    }
}
