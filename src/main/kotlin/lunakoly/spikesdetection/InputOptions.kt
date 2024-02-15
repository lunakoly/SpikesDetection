package lunakoly.spikesdetection

import lunakoly.arrrgh.Options
import lunakoly.arrrgh.list
import lunakoly.arrrgh.optionalString
import lunakoly.arrrgh.requiredString

class InputOptions : Options() {
    val inputFiles by list("--in")
    val outputFile by requiredString("--out")
    val fitting by requiredString("--fitting", "linear")
    val deviation by requiredString("--deviation", "binary")
    val deviationScalar by optionalString("--deviation-scalar")
}
