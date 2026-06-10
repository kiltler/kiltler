package com.bodyquest.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.seed.Article
import com.bodyquest.app.domain.seed.KnowledgeBase
import com.bodyquest.app.domain.seed.KnowledgeCategory
import com.bodyquest.app.ui.components.BqCard
import com.bodyquest.app.ui.components.SectionTitle
import com.bodyquest.app.ui.theme.BqSecondary
import com.bodyquest.app.ui.theme.BqTertiary

@Composable
fun KnowledgeScreen(onOpenArticle: (String) -> Unit) {
    var category by remember { mutableStateOf<KnowledgeCategory?>(null) }
    val articles = remember(category) {
        category?.let { KnowledgeBase.byCategory(it) } ?: KnowledgeBase.all
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Кодекс знаний", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("Мини-справочник по тренировкам, питанию, восстановлению и привычкам — по делу, без воды.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        SectionTitle("Разделы")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = category == null, onClick = { category = null }, label = { Text("Все") })
            KnowledgeCategory.entries.forEach { c ->
                FilterChip(
                    selected = category == c,
                    onClick = { category = c },
                    label = { Text("${c.emoji} ${c.title}") },
                )
            }
        }

        articles.forEach { article ->
            ArticleCard(article, onClick = { onOpenArticle(article.id) })
        }
    }
}

@Composable
private fun ArticleCard(article: Article, onClick: () -> Unit) {
    BqCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text("${article.category.emoji} ${article.category.title} · ${article.minutes} мин",
            style = MaterialTheme.typography.labelMedium, color = BqSecondary)
        Text(article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp))
        Text(
            article.body.lineSequence().firstOrNull { it.isNotBlank() }?.take(120)?.plus("…") ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text("Читать →", style = MaterialTheme.typography.labelLarge, color = BqTertiary,
            modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
fun ArticleScreen(articleId: String) {
    val article = remember(articleId) { KnowledgeBase.get(articleId) } ?: return
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("${article.category.emoji} ${article.category.title} · ${article.minutes} мин",
            style = MaterialTheme.typography.labelMedium, color = BqSecondary)
        Text(article.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        // Абзацы разделены пустой строкой; списки начинаются с «• ».
        article.body.split("\n\n").forEach { para ->
            Text(
                para.trim(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
