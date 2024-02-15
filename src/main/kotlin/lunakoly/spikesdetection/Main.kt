package lunakoly.spikesdetection

import lunakoly.arrrgh.fillFrom
import lunakoly.spikesdetection.modes.detectSpikes
import lunakoly.spikesdetection.modes.integrateSpikes
import lunakoly.spikesdetection.modes.visualizeNoise

fun main(args: Array<String>) {
    val options = InputOptions().fillFrom(args) ?: return

    when (options.mode) {
        InputOptions.Mode.SPIKES_DETECTION -> detectSpikes(options)
        InputOptions.Mode.NOISE_VISUALIZATION -> visualizeNoise(options)
        InputOptions.Mode.INTEGRATION -> integrateSpikes(options)
    }
}
