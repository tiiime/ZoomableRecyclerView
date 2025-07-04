package io.github.tiiime.manga

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.tiiime.manga.databinding.ActivityMainBinding
import io.github.tiiime.manga.databinding.MangaItemBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.list.layoutManager = LinearLayoutManager(this,
            RecyclerView.VERTICAL, false)
        binding.list.adapter = MangaAdapter(this)

        binding.orientation.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.vertical -> {
                    binding.list.layoutManager = LinearLayoutManager(this,
                        RecyclerView.VERTICAL, false)
                }
                R.id.horizontal -> {
                    binding.list.layoutManager = LinearLayoutManager(this,
                        RecyclerView.HORIZONTAL, false)
                }
            }
        }

    }

    class MangaAdapter(val context: Context) :
        RecyclerView.Adapter<MangaAdapter.MangaViewHolder>() {
        class MangaViewHolder(val binding: MangaItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
            val binding =
                MangaItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MangaViewHolder(binding)
        }

        override fun onBindViewHolder(holder: MangaViewHolder, position: Int) {
            val res = arrayOf(R.drawable.img_1, R.drawable.img_2, R.drawable.img_3)
            holder.binding.image.setImageResource(res[position % 3])
        }

        override fun getItemCount(): Int = 10
    }
}