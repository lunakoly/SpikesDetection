package lunakoly.frequencies

import lunakoly.arrrgh.Options
import lunakoly.arrrgh.list
import lunakoly.arrrgh.requiredString

class InputOptions : Options() {
    val inputFiles by list("--in")
    val outputFile by requiredString("--out")
    val fitting by requiredString("--fitting", "median")
}
