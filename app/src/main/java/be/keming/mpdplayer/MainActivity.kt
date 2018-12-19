package be.keming.mpdplayer

import android.content.Intent
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import pl.droidsonroids.gif.GifImageView
import java.net.Socket
import pl.droidsonroids.gif.GifDrawable
import java.util.*
import kotlin.concurrent.thread
import android.view.MenuInflater
import java.io.*


// Some variables
    val TAG = "mpd-debug"

// Rand
val resId = intArrayOf( R.drawable.gif1,R.drawable.gif2, R.drawable.gif3,
                        R.drawable.gif4, R.drawable.gif5, R.drawable.gif6,
                        R.drawable.gif7, R.drawable.gif8, R.drawable.gif9,
                        R.drawable.gif10, R.drawable.gif11, R.drawable.gif12)
val rand = Random()

// Sockets --------------------------------------------------------------------------------------------------
//private var mySocket: Socket? = null

// MPD Server Settings ---------------------------------------------------------------------------------------
private var serverIp: String = "172.30.40.33"
private var serverPort:Int = 6600

class MainActivity : AppCompatActivity() {

    // Variables
    var isPlaying : Boolean = true

    // Set Font
    private var montserratBoldTypeface : Typeface? = null
    private var montserratRegularTypeface : Typeface? = null

    // textViews
    private var songTextView : TextView? = null

    private var titleTextView : TextView? = null

    // imageviews
    private var playPauseBtn : Button? = null
    private var gitImgView : GifImageView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)








        this.playPauseBtn = this.findViewById(R.id.btnPlay)
        this.gitImgView = this.findViewById(R.id.gifImage)


        // change seekbar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeText.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

    // https://www.youtube.com/watch?v=f2sxtl3UU98

        // Set Font
        initializeRessources ()

        //Set roung imageview
        //roundImageView(R.drawable.gif1, songImageview)

        // Socket
        /*thread {
            try {
                appSocket = Socket(serverIp, serverPort)
                printWriter = PrintWriter(appSocket!!.getOutputStream())

            }catch (ioe: IOException){
                ioe.printStackTrace()
            }
        }*/


    }

    override fun onResume() {
        super.onResume()
        GlobalClass.printer?.println(MPD_CURRENT_SONG)
        GlobalClass.printer?.flush()
        Log.d(TAG, "" +GlobalClass.printer)
        musicTiltle.text = GlobalClass.printer?.println(MPD_CURRENT_SONG).toString()
        GlobalClass.reader = BufferedReader(InputStreamReader(GlobalClass.mySocket!!.getInputStream()))

        GlobalClass.reader.readLine()

    }









    // ==========================================================================================================================================

    private fun initializeRessources (){
        // set regular font to App title
        this.titleTextView = this.findViewById(R.id.txtAppTitle)
        this.songTextView = this.findViewById(R.id.musicTiltle)

        // define fonts
        this.montserratRegularTypeface = ResourcesCompat.getFont(this.applicationContext, R.font.montserrat_regular)
        this.montserratBoldTypeface = ResourcesCompat.getFont(this.applicationContext, R.font.montserrat_bold)

        // assign font
        this.titleTextView!!.setTypeface(this.montserratRegularTypeface, Typeface.NORMAL)
        this.songTextView!!.setTypeface(this.montserratBoldTypeface, Typeface.NORMAL)
        this.liveStreamTxt!!.setTypeface(this.montserratBoldTypeface, Typeface.NORMAL)

        // set default gif
        gitImgView?.setBackgroundResource(R.drawable.radio)

    }

    fun onSettingsClick(view: View){
        openSettings()
    }

    public fun openSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }


/*    private fun roundImageView(image: Int, myImgView:ImageView?){
        val img = BitmapFactory.decodeResource(resources,image)
        val round = RoundedBitmapDrawableFactory.create(resources, img)
        round.cornerRadius = 55f
        myImgView?.setImageDrawable(round)
    }*/


    // Commands for MPD
    val MPD_COMMAND_PAUSE = "pause\n"
    val MPD_CURRENT_SONG = "currentsong\n"
    val MPD_COMMAND_PREVIOUS = "previous\n"
    val MPD_COMMAND_NEXT = "next\n"
    val MPD_COMMAND_STOP = "stop\n"

    fun MPD_COMMAND_PAUSE(pause: Boolean): String {
        return "pause " + if (pause) "1" else "0"
    }

    fun onPlayPauseClick(view: View){
        // set gif to static default
        gitImgView?.setBackgroundResource(R.drawable.radio)

       /* thread {
            this.playPauseBtn?.setBackgroundResource(R.drawable.pause)
            printWriter?.println(MPD_COMMAND_PAUSE(true))
            printWriter?.flush()
            printWriter?.close()

        }*/

        // Connect to Server
        connecToServer()
    }

    fun connecToServer (){
        thread {
            try {
                GlobalClass.mySocket = Socket(GlobalClass.IP, GlobalClass.port)
                Log.d(TAG,"socket connected : ${GlobalClass.mySocket!!.isConnected}")
                if (GlobalClass.mySocket!!.isConnected()){
                    Log.d(TAG,"socket connected : ${GlobalClass.mySocket!!.isConnected}")
                }
                if (GlobalClass.mySocket!!.isClosed()){
                    Log.d(TAG,"socket closed : ${GlobalClass.mySocket!!.isClosed}")
                }
                //GlobalClass.mySocket!!.close()
                Log.d(TAG,"socket closed : ${GlobalClass.mySocket!!.isClosed}")
            }catch (ioe: IOException){
                ioe.printStackTrace()
            }

            GlobalClass.printer = PrintWriter(GlobalClass.mySocket!!.getOutputStream())
            GlobalClass.reader = BufferedReader(InputStreamReader(GlobalClass.mySocket!!.getInputStream()))

            GlobalClass.reader.readLine()

        }
    }

    fun onPreviousClick(view: View){
        // Change gif randomly
        var index = rand.nextInt(resId.size - 1 - 0 + 1) + 0
        gitImgView?.setBackgroundResource(resId[index])

        // Send command
        thread {
            try {
                connecToServer()
                GlobalClass.printer?.println(MPD_COMMAND_PREVIOUS)
                GlobalClass.printer?.flush()
                Log.d(TAG,"previous pressed")
                GlobalClass.mySocket.close()
            } catch (ioe: IOException){
                ioe.printStackTrace()
            }
        }
    }

    fun onNextClick(view: View){
        // Change gif randomly
        var index = rand.nextInt(resId.size - 1 - 0 + 1) + 0
        gitImgView?.setBackgroundResource(resId[index])

        // Send command
        thread {
            try {
                connecToServer()
                GlobalClass.printer?.println(MPD_COMMAND_NEXT)
                GlobalClass.printer?.flush()
                Log.d(TAG,"next pressed")
                GlobalClass.mySocket.close()
            } catch (ioe: IOException){
                ioe.printStackTrace()
            }
        }
    }
}
