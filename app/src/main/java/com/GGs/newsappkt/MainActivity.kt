package com.GGs.newsappkt

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(),CategoryRVAdapter.CategoryClickInterface {
    //9b4f038f0a4c45ffa0e5f28919dfb5af

    private lateinit var rvNews: RecyclerView
    private lateinit var rvCategorias: RecyclerView
    private lateinit var pbCargando: ProgressBar
    private lateinit var categoryRVModalArrayList: ArrayList<CategoryRVModal>
    private lateinit var articlesArrayList: ArrayList<Articles>
    private lateinit var categoryRVAdapter: CategoryRVAdapter
    private lateinit var newsRVAdapter: NewsRVAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvNews = findViewById(R.id.rvNews)
        rvCategorias = findViewById(R.id.rvCategorias)
        pbCargando = findViewById(R.id.pbCargando)
        articlesArrayList = ArrayList()
        categoryRVModalArrayList = ArrayList()
        newsRVAdapter = NewsRVAdapter(articlesArrayList, this)
        categoryRVAdapter = CategoryRVAdapter(categoryRVModalArrayList,this,object : CategoryRVAdapter.CategoryClickInterface {
            override fun onCategoryClick(position: Int) {
                val categoria: String = categoryRVModalArrayList[position].category
                getNews(categoria)
            }
        })
        rvNews.layoutManager = LinearLayoutManager(this)
        rvNews.adapter = newsRVAdapter
        rvCategorias.adapter = categoryRVAdapter
        getCategories()
        getNews("All")
        newsRVAdapter.notifyDataSetChanged()
    }

    private fun getCategories(){
        categoryRVModalArrayList.add(CategoryRVModal("All","https://images.unsplash.com/photo-1504711434969-e33886168f5c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8M3x8bm90aWNpYXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Technology","https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8VGVjbm9sb2dpYXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Science","https://plus.unsplash.com/premium_photo-1676325102583-0839e57d7a1f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8N3x8Q2llbmNpYXxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Sports","https://images.unsplash.com/photo-1517649763962-0c623066013b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NHx8RGVwb3J0ZXN8ZW58MHx8MHx8&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("General","https://images.unsplash.com/photo-1494059980473-813e73ee784b?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NXx8R2VuZXJhbHxlbnwwfHwwfHw%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Business","https://images.unsplash.com/photo-1544377193-33dcf4d68fb5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Nnx8RmluYW56YXN8ZW58MHx8MHx8&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Entertainment","https://images.unsplash.com/photo-1598899134739-24c46f58b8c0?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8NHx8ZW50cmV0ZW5pbWllbnRvfGVufDB8fDB8fA%3D%3D&auto=format&fit=crop&w=400&q=60"))
        categoryRVModalArrayList.add(CategoryRVModal("Health","https://images.unsplash.com/photo-1532938911079-1b06ac7ceec7?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Nnx8U2FsdWR8ZW58MHx8MHx8&auto=format&fit=crop&w=400&q=60"))
        categoryRVAdapter.notifyDataSetChanged()
    }

    private fun getNews(categoria: String){
        pbCargando.visibility = View.VISIBLE
        articlesArrayList.clear()
        val categoriaURL: String = "https://newsapi.org/v2/top-headlines?country=us&category="+categoria+"&sortBy=publishedAt&apikey=9b4f038f0a4c45ffa0e5f28919dfb5af"
        val url: String = "https://newsapi.org/v2/top-headlines?country=us&sortBy=publishedAt&apikey=9b4f038f0a4c45ffa0e5f28919dfb5af"
        val url_base: String = "https:/newsapi.org/"
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url_base)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val retrofitAPI: RetrofitAPI = retrofit.create(RetrofitAPI::class.java)
        var call: Call<NewsModal>
        if(categoria == "All"){
            call = retrofitAPI.getAllNews(url)
        }else{
            call = retrofitAPI.getNewsByCategory(categoriaURL)
        }
        call.enqueue(object: Callback<NewsModal>{
            override fun onResponse(call: Call<NewsModal>, response: Response<NewsModal>) {
                val newsModal: NewsModal = response.body()!!
                pbCargando.visibility = View.GONE
                val articles: ArrayList<Articles> = newsModal.articles
                for (i in 0 until articles.size){
                    articlesArrayList.add(Articles(articles[i].title,
                    articles[i].description,articles[i].urlToImage
                        ,articles[i].url,articles[i].content))
                }
                newsRVAdapter.notifyDataSetChanged()
            }

            override fun onFailure(call: Call<NewsModal>, t: Throwable) {
                Toast.makeText(this@MainActivity,"Error al obtener las noticias", Toast.LENGTH_LONG).show()
            }

        })




    }

    override fun onCategoryClick(position: Int) {
        val categoria: String = categoryRVModalArrayList[position].category
        getNews(categoria)
    }
}