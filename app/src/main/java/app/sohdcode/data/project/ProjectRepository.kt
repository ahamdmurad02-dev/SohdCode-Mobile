package app.sohdcode.data.project

import app.sohdcode.domain.model.CodeProject
import app.sohdcode.domain.model.ProjectFile
import app.sohdcode.domain.model.SupportedLanguage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProjectRepository(private val client: SupabaseClient) {

    suspend fun list(): Result<List<CodeProject>> = runCatching {
        client.from("projects").select { order("updated_at", Order.DESCENDING) }.decodeList<CodeProject>()
    }

    suspend fun get(id: String): Result<CodeProject> = runCatching {
        client.from("projects").select { filter { eq("id", id) }; single() }.decodeSingle<CodeProject>()
    }

    suspend fun create(userId: String, name: String, description: String, language: SupportedLanguage): Result<CodeProject> = runCatching {
        val starter = starterFile(language)
        val payload = ProjectWrite(userId, name.trim(), description.trim(), language.id, listOf(starter))
        client.from("projects").insert(payload) { select() }.decodeSingle<CodeProject>()
    }

    suspend fun rename(id: String, name: String): Result<CodeProject> = runCatching {
        client.from("projects").update(ProjectNameUpdate(name.trim())) {
            filter { eq("id", id) }; select()
        }.decodeSingle<CodeProject>()
    }

    suspend fun saveFiles(id: String, files: List<ProjectFile>, language: String): Result<CodeProject> = runCatching {
        if (files.isEmpty()) error("Cannot save an empty project. Add at least one file.")
        client.from("projects").update(ProjectFilesUpdate(files, language)) {
            filter { eq("id", id) }; select()
        }.decodeSingle<CodeProject>()
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        client.from("projects").delete { filter { eq("id", id) } }
        Unit
    }

    private fun starterFile(language: SupportedLanguage): ProjectFile {
        val content = when (language) {
            SupportedLanguage.JAVASCRIPT -> "// SohdCode Mobile\nfunction greet(name) {\n  return 'Hello, ' + name;\n}\nconsole.log(greet('SohdCode'));\n"
            SupportedLanguage.JSON -> "{\n  \"app\": \"SohdCode Mobile\",\n  \"ok\": true\n}\n"
            SupportedLanguage.MARKDOWN -> "# New project\n\nWrite notes here.\n"
            SupportedLanguage.TEXT -> "New file\n"
            SupportedLanguage.PYTHON -> "print(\"Hello from SohdCode\")\n"
            SupportedLanguage.KOTLIN -> "fun main() {\n    println(\"Hello from SohdCode\")\n}\n"
            SupportedLanguage.JAVA -> "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello from SohdCode\");\n  }\n}\n"
        }
        return ProjectFile(path = language.defaultFile, content = content, language = language.id)
    }

    @Serializable private data class ProjectWrite(
        @SerialName("user_id") val userId: String,
        val name: String,
        val description: String,
        val language: String,
        val files: List<ProjectFile>
    )
    @Serializable private data class ProjectNameUpdate(val name: String)
    @Serializable private data class ProjectFilesUpdate(val files: List<ProjectFile>, val language: String)
}
