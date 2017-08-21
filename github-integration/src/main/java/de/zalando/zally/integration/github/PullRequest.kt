package de.zalando.zally.integration.github

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.zally.Configuration
import org.apache.commons.io.IOUtils
import org.kohsuke.github.GHCommitState
import org.kohsuke.github.GHPullRequestFileDetail
import org.kohsuke.github.GHRepository
import org.kohsuke.github.PagedIterable
import java.nio.charset.StandardCharsets
import java.util.Optional

open class PullRequest(private val yamlMapper: ObjectMapper,
                       private val repository: GHRepository,
                       private val commitHash: String) {
class PullRequest(private val yamlMapper: ObjectMapper,
                  private val repository: GHRepository,
                  val eventInfo: PullRequestEvent,
                  private val changedFiles: PagedIterable<GHPullRequestFileDetail>) {

    private val ZALLY_CONFIGURATION_PATH = ".zally.yaml"

    open fun updateCommitState(state: GHCommitState, url: String, description: String) {
        repository.createCommitStatus(commitHash(), state, url, description, "Zally")
    }

    open fun getConfiguration(): Optional<Configuration> =
            getFileContents(ZALLY_CONFIGURATION_PATH)
            .map { yamlMapper.readValue(it, Configuration::class.java) }

    open fun getSwaggerFile(): Optional<String> =
            getConfiguration().flatMap {
            getFileContents(it.swaggerPath)
    }

    private fun getFileContents(path: String): Optional<String> =
            Optional.ofNullable(repository.getTreeRecursive(commitHash(), 1).getEntry(path))
            .map { IOUtils.toString(it.readAsBlob(), StandardCharsets.UTF_8) }

    fun getChangedFiles(): Optional<List<String>> {
        return Optional.of(changedFiles.map { t -> t.filename })
    }

    fun isAPIChanged(): Boolean {
        val changedFiles = getChangedFiles().get()
        if (changedFiles.contains(ZALLY_CONFIGURATION_PATH) || changedFiles.contains(getConfiguration().get().swaggerPath)) {
            return true
        }
        return false
    }
    private fun commitHash(): String = eventInfo.pullRequest.head.sha
}
