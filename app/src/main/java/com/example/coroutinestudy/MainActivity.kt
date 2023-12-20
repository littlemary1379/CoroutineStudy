package com.example.coroutinestudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.coroutines.CloseableCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {

    private val dispatcher = newSingleThreadContext(name = "ServiceCall")
    private val defDsp = newSingleThreadContext(name = "ServiceCall")
    private val factory = DocumentBuilderFactory.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch(dispatcher) {
            loadNews()
        }

        asyncLoadNews()

    }

    private fun loadNews() {
        // MainThread에서 네트워크 요청할 시 : NetworkOnMainThreadException 발생 -> 백그라운드 스레드 생성 필요
        GlobalScope.launch(dispatcher) {
            val headline = fetchRSSHeadlines()
            val newsCount = findViewById<TextView>(R.id.newsCount)

            // CalledFromWrongThreadException 발생 : UI변경은 반드시 UI스레드에셔 변경해야함.
            GlobalScope.launch(Dispatchers.Main) {
                newsCount.text = "find ${headline.count()} news"
            }
        }
    }

    private fun asyncLoadNews() = GlobalScope.launch(dispatcher) {
        val headline = fetchRSSHeadlines()
        val newsCount = findViewById<TextView>(R.id.newsCount)

        GlobalScope.launch(Dispatchers.Main) {
            newsCount.text = "find ${headline.count()} news"
        }
    }

    // 디스패처를 유연하게 주는 방법도 있음
    private fun asyncLoadNews(dispatcher: CoroutineDispatcher = defDsp) = GlobalScope.launch(dispatcher) {
        val headline = fetchRSSHeadlines()
        val newsCount = findViewById<TextView>(R.id.newsCount)

        GlobalScope.launch(Dispatchers.Main) {
            newsCount.text = "find ${headline.count()} news"
        }
    }

    private fun fetchRSSHeadlines() : List<String> {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        val news = xml.getElementsByTagName("channel").item(0)
        return (0 until news.childNodes.length)
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map {  it as Element }
            .filter { "item" == it.tagName }
            .map { it.getElementsByTagName("title").item(0).textContent }
        //return emptyList()
    }

//    override fun onResume() {
//        super.onResume()
//        Thread.sleep(5000)
//    }
}