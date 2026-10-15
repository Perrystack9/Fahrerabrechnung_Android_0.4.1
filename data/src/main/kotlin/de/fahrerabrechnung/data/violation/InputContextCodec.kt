package de.fahrerabrechnung.data.violation

import de.fahrerabrechnung.domain.violation.input.*
import java.time.Instant
import kotlinx.serialization.json.*

/** Explicit, versioned SQLCipher payload; no reflection, locale, raw card bytes or display options. */
internal object InputContextCodec {
    const val VERSION = 1
    fun encode(context: InputContext): String = buildJsonObject {
        put("version", VERSION)
        put("profileId", context.profileId)
        putJsonArray("assertions") {
            for (entry in context.assertions.sortedBy { it.semanticId }) add(buildJsonObject {
                put("key", entry.key.name)
                put("subjectKind", entry.subject.kind.name); put("subjectId", entry.subject.id)
                put("from", entry.from.toString()); put("until", entry.until?.toString()?.let(::JsonPrimitive) ?: JsonNull)
                put("origin", entry.origin.name); put("originReference", entry.originReference)
                put("recordedAt", entry.recordedAt.toString())
                when (val value = entry.value) {
                    is ContextValue.Flag -> { put("type", "flag"); put("value", value.value) }
                    is ContextValue.Number -> { put("type", "number"); put("value", value.value) }
                    is ContextValue.Text -> { put("type", "text"); put("value", value.value) }
                }
            })
        }
    }.toString()

    fun decode(payload: String, revisionId: String): InputContext {
        val root = Json.parseToJsonElement(payload).jsonObject
        require(root.keys == setOf("version", "profileId", "assertions"))
        require(root.getValue("version").jsonPrimitive.int == VERSION)
        val assertions = root.getValue("assertions").jsonArray.map { value ->
            val entry = value.jsonObject
            require(entry.keys == setOf("key", "subjectKind", "subjectId", "from", "until", "origin",
                "originReference", "recordedAt", "type", "value"))
            fun text(key: String) = entry.getValue(key).jsonPrimitive.content
            val actual = entry.getValue("value").jsonPrimitive
            val contextValue = when (text("type")) {
                "flag" -> { require(!actual.isString); ContextValue.Flag(actual.boolean) }
                "number" -> { require(!actual.isString); ContextValue.Number(actual.long) }
                "text" -> { require(actual.isString); ContextValue.Text(actual.content) }
                else -> error("Unsupported context value type")
            }
            ContextAssertion(ContextKey.valueOf(text("key")), contextValue,
                ContextSubject(ContextSubjectKind.valueOf(text("subjectKind")), text("subjectId")),
                Instant.parse(text("from")), entry.getValue("until").takeUnless { it == JsonNull }?.jsonPrimitive?.content?.let(Instant::parse),
                ContextOrigin.valueOf(text("origin")), text("originReference"), Instant.parse(text("recordedAt")))
        }
        return InputContext(root.getValue("profileId").jsonPrimitive.content, revisionId, assertions).freeze()
    }
}
