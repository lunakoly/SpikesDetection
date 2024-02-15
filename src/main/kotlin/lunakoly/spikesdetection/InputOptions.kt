package lunakoly.spikesdetection

import lunakoly.arrrgh.*

class InputOptions : Options() {
    val inputFiles by list("--in")
    val outputFile by requiredString("--out")
    val fitting by requiredEnum("--fitting", Fitting.LINEAR)
    val deviation by requiredEnum("--deviation", Deviation.BINARY)
    val deviationScalar by optionalString("--deviation-scalar")

    enum class Fitting { CONSTANT, LINEAR }
    enum class Deviation { FAKE, BINARY }
}
