import androidx.appcompat.widget.SearchView
import com.example.fumetti.database.utility.ComicsAdapter

class SearchHandler(
    private val searchView: SearchView,
    private val adapter: ComicsAdapter
) {
    internal var lastQuery: String = ""
    private val debouncePeriod = 300L
    private var debounceRunnable: Runnable? = null

    init {
        setupSearchListener()
    }

    private fun setupSearchListener() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchView.removeCallbacks(debounceRunnable)
                    debounceRunnable = Runnable { performSearch(it) }
                    searchView.postDelayed(debounceRunnable, debouncePeriod)
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        if (query == lastQuery) return
        lastQuery = query

        val loweredQuery = query.lowercase()
        adapter.filter { comic ->
            comic.name.lowercase().contains(loweredQuery) ||
                    comic.series.toString().contains(loweredQuery) ||
                    comic.number.toString().contains(loweredQuery)
        }
    }

    fun clearSearch() {
        lastQuery = ""
        searchView.setQuery("", false)
        adapter.restoreOriginal()
    }
}
