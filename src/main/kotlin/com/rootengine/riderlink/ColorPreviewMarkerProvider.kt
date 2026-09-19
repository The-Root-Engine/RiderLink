package com.github.roottool0.riderlink

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.ui.ColorIcon
import java.awt.Color

class ColorPreviewMarkerProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val text = element.text ?: return null

        // Ищем вызовы конструкторов FColor или FLinearColor
        val isFColor = text.startsWith("FColor(") || text.startsWith("FColor{")
        val isFLinearColor = text.startsWith("FLinearColor(") || text.startsWith("FLinearColor{")

        if (!isFColor && !isFLinearColor) return null

        try {
            // Извлекаем аргументы из скобок (...) или {...}
            val argsText = text.substringAfter("(").substringBefore(")").substringAfter("{").substringBefore("}")
            val tokens = argsText.split(",")

            if (tokens.size >= 3) {
                val finalColor = if (isFLinearColor) {
                    // FLinearColor использует float (0.0f - 1.0f)
                    val r = parseAsFloat(tokens[0])
                    val g = parseAsFloat(tokens[1])
                    val b = parseAsFloat(tokens[2])
                    Color(r, g, b)
                } else {
                    // FColor использует обычные int (0 - 255)
                    val r = parseAsInt(tokens[0])
                    val g = parseAsInt(tokens[1])
                    val b = parseAsInt(tokens[2])
                    Color(r, g, b)
                }

                // Создаем маркер для панели рядом с номерами строк
                return LineMarkerInfo(
                    element,
                    element.textRange,
                    ColorIcon(12, finalColor), // Квадрат 12x12 пикселей
                    { "Цвет в коде: ${finalColor.red}, ${finalColor.green}, ${finalColor.blue}" }, // Подсказка при наведении
                    null,
                    GutterIconRenderer.Alignment.RIGHT,
                    { "ColorPreviewMarker" }
                )
            }
        } catch (e: Exception) {
            return null
        }

        return null
    }

    private fun parseAsInt(token: String): Int {
        return token.trim().toIntOrNull()?.coerceIn(0, 255) ?: 0
    }

    private fun parseAsFloat(token: String): Int {
        val clean = token.trim().lowercase().removeSuffix("f")
        val value = clean.toFloatOrNull() ?: 0.0f
        return (value * 255).toInt().coerceIn(0, 255)
    }
}
