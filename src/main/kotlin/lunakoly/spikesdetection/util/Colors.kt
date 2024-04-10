package lunakoly.spikesdetection.util

import lunakoly.colortools.generateColors
import lunakoly.colortools.prepareAngleForNColors
import org.jetbrains.kotlinx.kandy.dsl.categorical
import org.jetbrains.kotlinx.kandy.letsplot.layers.context.aes.WithColor
import org.jetbrains.kotlinx.kandy.util.color.Color

fun createColorIterator(optimalCount: Int) = generateColors(
    hueAngularStep = prepareAngleForNColors(optimalCount),
    rgbToColor = { r, g, b -> Color.rgb(r, g, b) },
).iterator()

class NameToColorMapper {
    private val names = mutableListOf<String>()
    private val colors = mutableListOf<Color>()

    private fun <T> nameColumnFor(points: List<T>, name: String): List<String> = List(points.size) { name }

    fun assign(name: String) = ColorName(name, this)

    fun <T> WithColor.configureColorFor(points: List<T>, name: String, color: Color) {
        if (name !in names) {
            names.add(name)
            colors.add(color)
        }

        color(nameColumnFor(points, name), "Color") {
            scale = categorical(colors, names)
        }
    }
}

class ColorName(
    private val name: String,
    private val nameToColorMapper: NameToColorMapper,
) {
    fun <T> configureFor(points: List<T>, color: Color, context: WithColor) {
        with (nameToColorMapper) {
            context.configureColorFor(points, name, color)
        }
    }
}
