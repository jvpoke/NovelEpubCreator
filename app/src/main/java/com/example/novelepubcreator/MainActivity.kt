package com.example.novelepubcreator

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.novelepubcreator.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubWriter
import java.io.FileOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val chapterLinks = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cbTranslate.setOnCheckedChangeListener { _, isChecked ->
            binding.rgTranslateMode.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.rgDetectionMode.setOnCheckedChangeListener { _, checkedId ->
            binding.llManualInput.visibility = if (checkedId == R.id.rbManual) View.VISIBLE else View.GONE
        }

        binding.btnAddChapter.setOnClickListener {
            val url = binding.etChapterUrl.text.toString()
            if (url.isNotEmpty()) {
                chapterLinks.add(url)
                Toast.makeText(this, "Capítulo adicionado!", Toast.LENGTH_SHORT).show()
                binding.etChapterUrl.text?.clear()
            }
        }

        binding.btnStart.setOnClickListener {
            startProcess()
        }
    }

    private fun startProcess() {
        val novelUrl = binding.etNovelUrl.text.toString()
        val firstChapterUrl = binding.etChapterUrl.text.toString()

        if (novelUrl.isEmpty()) {
            Toast.makeText(this, "Insira o link da novel", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnStart.isEnabled = false
            
            try {
                val book = Book()
                // Obter metadados da novel
                val doc = withContext(Dispatchers.IO) { Jsoup.connect(novelUrl).get() }
                book.metadata.addTitle(doc.title())

                if (binding.rbAuto.isChecked) {
                    autoDetectChapters(firstChapterUrl, book)
                } else {
                    processManualChapters(book)
                }

                exportEpub(book)
                
            } catch (e: Exception) {
                binding.tvStatus.text = "Erro: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnStart.isEnabled = true
            }
        }
    }

    private suspend fun autoDetectChapters(startUrl: String, book: Book) {
        var currentUrl = startUrl
        var count = 1
        
        while (currentUrl.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                binding.tvStatus.text = "Processando capítulo $count..."
            }
            
            try {
                val doc = withContext(Dispatchers.IO) { Jsoup.connect(currentUrl).get() }
                var content = doc.body().text() // Usando text para tradução mais limpa
                
                if (binding.cbTranslate.isChecked) {
                    content = if (binding.rbOffline.isChecked) {
                        TranslationHelper.translateOffline(content)
                    } else {
                        TranslationHelper.translateOnline(content)
                    }
                }
                
                val htmlContent = "<html><body>$content</body></html>"
                val resource = Resource(htmlContent.toByteArray(), "chapter$count.html")
                book.addSection("Capítulo $count", resource)
                
                // Lógica de alteração sequencial (exemplo: trocar numero no link)
                currentUrl = tryNextUrl(currentUrl)
                count++
                
                if (count > 50) break // Limite de segurança para o exemplo
            } catch (e: Exception) {
                break
            }
        }
    }

    private fun tryNextUrl(url: String): String {
        // Tenta encontrar um número no final da URL e incrementar
        val regex = "(\\d+)(?!.*\\d)".toRegex()
        val match = regex.find(url)
        return if (match != null) {
            val number = match.value.toInt()
            url.replaceRange(match.range, (number + 1).toString())
        } else {
            ""
        }
    }

    private suspend fun processManualChapters(book: Book) {
        chapterLinks.forEachIndexed { index, url ->
            val doc = withContext(Dispatchers.IO) { Jsoup.connect(url).get() }
            val resource = Resource(doc.body().html().toByteArray(), "chapter${index + 1}.html")
            book.addSection("Capítulo ${index + 1}", resource)
        }
    }

    private fun exportEpub(book: Book) {
        val fileName = "novel_export.epub"
        val file = File(getExternalFilesDir(null), fileName)
        val out = FileOutputStream(file)
        EpubWriter().write(book, out)
        out.close()
        
        binding.tvStatus.text = "Concluído! Salvo em: ${file.absolutePath}"
        Toast.makeText(this, "EPUB Gerado com sucesso!", Toast.LENGTH_LONG).show()
    }
}
