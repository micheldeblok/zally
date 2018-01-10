package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter
import java.math.BigDecimal

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "PaginatedCollectionsSupportPageSizeQueryParameter",
        severity = Severity.SHOULD,
        title = "Paginated Resources Support 'pageSize' Query Parameter"
)
class PaginatedCollectionsSupportPageSizeQueryParameter : AbstractRule() {
    val description = "Paginated resources support a 'pageSize' query parameter " +
            "with type:integer, format:int32, minimum:1 so that clients can easily iterate over the collection."

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.collections()
                    .map { (pattern, path) ->
                        when {
                            hasPageSizeQueryParam(path.get) -> null
                            else -> "paths $pattern GET parameters: does not include a valid pageSize query parameter"
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }

    private fun hasPageSizeQueryParam(op: Operation?): Boolean =
            op?.parameters?.find { isPageSizeQueryParam(it) } != null

    private fun isPageSizeQueryParam(param: Parameter): Boolean {
        if (param !is QueryParameter) {
            return false
        }
        return when {
            param.name != "pageSize" -> false
            param.type != "integer" -> false
            param.format != "int32" -> false
            param.minimum != BigDecimal(1) -> false
            else -> true
        }
    }
}