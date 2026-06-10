package com.bodyquest.app.domain

import com.bodyquest.app.domain.seed.KnowledgeBase
import com.bodyquest.app.domain.seed.KnowledgeCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnowledgeBaseTest {

    @Test fun idsAreUnique() {
        val ids = KnowledgeBase.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun everyArticleHasContent() {
        KnowledgeBase.all.forEach { a ->
            assertTrue("пустой заголовок: ${a.id}", a.title.isNotBlank())
            assertTrue("слишком короткий текст: ${a.id}", a.body.trim().length > 80)
            assertTrue("нет времени чтения: ${a.id}", a.minutes > 0)
        }
    }

    @Test fun everyCategoryHasArticles() {
        KnowledgeCategory.entries.forEach { c ->
            assertTrue("в разделе ${c.title} нет статей", KnowledgeBase.byCategory(c).isNotEmpty())
        }
    }

    @Test fun getByIdWorks() {
        val first = KnowledgeBase.all.first()
        assertNotNull(KnowledgeBase.get(first.id))
        assertEquals(null, KnowledgeBase.get("no_such_id"))
    }
}
