package lunakoly.spikesdetection

import lunakoly.arrrgh.*

class InputOptions : Options() {
    val mode by requiredEnum<Mode>("--mode", null)
    val inputFiles by list("--in")
    val outputFile by requiredString("--out")
    val fitting by requiredEnum("--fitting", Fitting.LINEAR)
    val deviation by requiredEnum("--deviation", Deviation.BINARY)
    val deviationScalar by optionalString("--deviation-scalar")
    val bellSigma by requiredString("--bell", "5.0")

    enum class Fitting { CONSTANT, LINEAR }
    enum class Deviation { FAKE, BINARY }
    enum class Mode { SPIKES_DETECTION, NOISE_VISUALIZATION, INTEGRATION }
}
