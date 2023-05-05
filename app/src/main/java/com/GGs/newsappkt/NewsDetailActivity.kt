package com.GGs.newsappkt

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class NewsDetailActivity : AppCompatActivity() {

    lateinit var title: String
    lateinit var desc: String
    lateinit var content: String
    lateinit var imageURL: String
    lateinit var url: String
    lateinit var txvTitulo: TextView
    lateinit var txvSubDesc: TextView
    lateinit var txvContenido: TextView
    lateinit var ivNews: ImageView
    lateinit var btnLeer: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        title = intent.getStringExtra("title").toString()
        desc = intent.getStringExtra("desc").toString()
        content = intent.getStringExtra("content").toString()
        imageURL = intent.getStringExtra("image").toString()
        url = intent.getStringExtra("url").toString()
        txvTitulo = findViewById(R.id.txvTitulo)
        txvSubDesc = findViewById(R.id.txvSubDesc)
        txvContenido = findViewById(R.id.txvContenido)
        ivNews = findViewById(R.id.ivNews)
        btnLeer = findViewById(R.id.btnLeer)
        txvTitulo.text = title
        txvSubDesc.text = desc
        txvContenido.text = content
        Picasso.get().load(imageURL).into(ivNews)
        btnLeer.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
            }

        })
    }
}