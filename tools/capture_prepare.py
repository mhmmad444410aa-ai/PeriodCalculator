from pathlib import Path
main = Path("app/src/main/java/com/mhmmad/periodcalculator/MainActivity.kt")
root = main.parent / "ui/PeriodRoot.kt"
p = main.read_text()
seed = """        if (BuildConfig.DEBUG && intent.hasExtra("capture_route")) {
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                val repository = com.mhmmad.periodcalculator.data.PeriodRepository(this@MainActivity)
                val preferences = com.mhmmad.periodcalculator.data.PreferenceRepository(this@MainActivity)
                repository.clear()
                val day = java.time.LocalDate.now().toEpochDay()
                listOf(133L, 105L, 78L, 50L, 21L).forEach { ago ->
                    repository.savePeriod(com.mhmmad.periodcalculator.data.PeriodEntity(startEpochDay = day - ago, endEpochDay = day - ago + 4, flowIntensity = 2, note = ""))
                }
                listOf(1L, 4L, 20L, 21L, 22L, 27L).forEachIndexed { i, ago ->
                    repository.saveDay(com.mhmmad.periodcalculator.data.DailyEntryEntity(epochDay = day - ago, cramps = i % 4, headache = i % 3, bloating = (i + 1) % 4, fatigue = (i + 2) % 4, mood = 3, sleepHours = 7f))
                }
                preferences.finishSetup(28, 5)
                preferences.resetKit()
                preferences.toggleKit("products")
                preferences.toggleKit("bag")
                preferences.toggleKit("water")
            }
        }
"""
anchor = "        vm = androidx.lifecycle.ViewModelProvider(this)[PeriodViewModel::class.java]"
assert p.count(anchor) == 1
p = p.replace(anchor, seed + anchor)
anchor = "canLock = { supportsDeviceAuth() })"
assert p.count(anchor) == 1
p = p.replace(anchor, 'canLock = { supportsDeviceAuth() }, initialRoute = if (BuildConfig.DEBUG) intent.getStringExtra("capture_route") else null)')
main.write_text(p)
p = root.read_text()
anchor = "canLock: () -> Boolean) {"
assert p.count(anchor) == 1
p = p.replace(anchor, "canLock: () -> Boolean, initialRoute: String? = null) {")
anchor = 'startDestination = if (prefs.onboarded) "home" else "onboarding"'
assert p.count(anchor) == 1
p = p.replace(anchor, 'startDestination = initialRoute ?: if (prefs.onboarded) "home" else "onboarding"')
root.write_text(p)
