package be.keming.mpdplayer

import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.util.Log.d
import android.view.Menu
import android.view.View
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.gif.GifImageView
import java.net.Socket
import pl.droidsonroids.gif.GifDrawable
import java.util.*
import kotlin.concurrent.thread
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.*
import java.io.*
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.file.Files.size
import android.system.Os.socket
import android.os.StrictMode












class MainActivity : AppCompatActivity() {

    // Variables
    var line: String? = null
    val readingLines = ArrayList<String>()
    val listSongs = ArrayList<String>()
    var isButtonClicked = false

    private var listv: ListView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listv = findViewById(R.id.listPlaylist) as ListView

        if (android.os.Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }


        readingLines.clear()
        listSongs.clear()
        runOnUiThread {
            try {
                FillListView()
            }
            catch (ex:Exception){
                Toast.makeText(this, "Server ${GlobalClass.IP} is not available", Toast.LENGTH_SHORT).show()
            }
        }




        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                setServerVolume(seekBar!!.progress)
            }
        })
    }

    /*override fun onResume() {
        super.onResume()

    }
*/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.actionbar, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.settingsbtn -> {
                openSettings()
                return true
            }
            R.id.refreshbtn -> {
                refresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }



    // ==========================================================================================================================================


    private fun setServerVolume(vol : Int){
        thread {
            try {
                connecToServer()
                GlobalClass.outToServer.writeBytes("setvol $vol\n")
            } catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }

    fun refresh(){

        runOnUiThread {
            readingLines.clear()
            listSongs.clear()
            runOnUiThread {
                try {
                    FillListView()
                }
                catch (ex:Exception){
                    Toast.makeText(this, "Server ${GlobalClass.IP} is not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun closeConnection() {

        try {
            GlobalClass.bufreader.close()
        } finally {
            try {
                GlobalClass.outToServer.close()
                GlobalClass.printer.close()
            } finally {
                GlobalClass.mySocket.close()
            }
        }

    }

    private fun FillListView (){

            try {
                thread {
                connecToServer()
                // Write line to server
                GlobalClass.outToServer.writeBytes(MPD_GET_PLAYLIST)

                  /*  // write properly to server
                    var writer = OutputStreamWriter(GlobalClass.mySocket.getOutputStream())
                    var prinWriter = PrintWriter(writer)
                    writer.write(MPD_GET_PLAYLIST)
                    writer.flush()

                    // read proprely
                    val br = BufferedReader(InputStreamReader(GlobalClass.mySocket.getInputStream()))
                    val st = br.readLine()
                    d("res",st)*/

                while (GlobalClass.bufreader.ready() && ({ line = GlobalClass.bufreader.readLine() }() != null)) {
                    readingLines.add(line!!)
                }
                    closeConnection()

                //GlobalClass.bufreader.close()

                for (i in 0 until readingLines.size) {

                    if (readingLines[i].contains("file", ignoreCase = true)) {
                        val song = "${readingLines[i].replace("file: ", "")}"
                        listSongs.add(song)
                    }
                }
            }
                listv!!.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listSongs) as ListAdapter?

                listv!!.setOnItemClickListener { parent, view, position, id ->
                    //Toast.makeText(this, getMusic(position), Toast.LENGTH_SHORT).show()
                    playThisSong(position)
                    btnPlay.setBackgroundResource(R.drawable.pause)
                    textViewCurSong.text = listSongs[position]
                }

            }catch (ex: Exception){
                ex.printStackTrace()
            }

    }


    public fun openSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }



    fun getMusic (id:Int): String {
        val MPD_COMMAND_PLAY = "play $id\n"
        return MPD_COMMAND_PLAY
    }


    fun playThisSong(pos : Int){
        thread {
            try {
                connecToServer()
                GlobalClass.printer?.println(getMusic(pos))
                GlobalClass.printer?.flush()
                GlobalClass.mySocket.close()
            } catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }


    // Commands for MPD
    val MPD_STATUS = "status\n"
    val MPD_COMMAND_PREVIOUS = "previous\n"
    val MPD_COMMAND_NEXT = "next\n"
    val MPD_GET_PLAYLIST = "playlistinfo\n"


    fun MPD_COMMAND_PAUSE(pause: Boolean): String {
        return "pause " + if (pause) "1\n" else "0\n"
    }

    fun onPlayPauseClick(view: View){
       if (btnPlay.id != R.drawable.play){
           isButtonClicked = !isButtonClicked
           btnPlay.setBackgroundResource(if (isButtonClicked) R.drawable.pause else R.drawable.play)
           thread {
               try {
                   connecToServer()
                   // Write line to server
                   GlobalClass.outToServer.writeBytes(MPD_COMMAND_PAUSE(!isButtonClicked))
               } catch (ex: Exception){
                   ex.printStackTrace()
               }
           }
       }
    }

    fun connecToServer (){

            GlobalClass.mySocket = Socket(GlobalClass.IP, GlobalClass.port)
            GlobalClass.printer = PrintWriter(GlobalClass.mySocket!!.getOutputStream())

            // Create output stream attached to socket
            GlobalClass.outToServer = DataOutputStream(GlobalClass.mySocket.getOutputStream())

            // Create (buffered) input stream attached to socket
            GlobalClass.bufreader = BufferedReader(InputStreamReader(GlobalClass.mySocket.getInputStream()))
    }


    fun onPreviousClick(view: View){
        thread {
            try {
                connecToServer()
                // Write line to server
                GlobalClass.outToServer.writeBytes(MPD_COMMAND_PREVIOUS)
            } catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }

    fun onNextClick(view: View){
        thread {
            try {
                connecToServer()
                // Write line to server
                GlobalClass.outToServer.writeBytes(MPD_COMMAND_NEXT)
            } catch (ex: Exception){
                ex.printStackTrace()
            }
        }
    }
}
