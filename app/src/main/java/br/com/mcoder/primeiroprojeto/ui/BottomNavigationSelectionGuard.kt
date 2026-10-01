package br.com.mcoder.primeiroprojeto.ui

class BottomNavigationSelectionGuard {
    private var isProgrammaticSelectionInProgress = false

    fun shouldHandleSelection(): Boolean = !isProgrammaticSelectionInProgress

    fun runProgrammaticUpdate(update: () -> Unit) {
        if (isProgrammaticSelectionInProgress) {
            return
        }

        isProgrammaticSelectionInProgress = true
        try {
            update()
        } finally {
            isProgrammaticSelectionInProgress = false
        }
    }
}
