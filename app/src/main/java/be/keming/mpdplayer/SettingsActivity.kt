package be.keming.mpdplayer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.view.View
import kotlinx.android.synthetic.main.activity_settings.*

public var EXTRA_IP = "be.keming.mpdplayer.EXTRA_IP"
public var EXTRA_PORT = "be.keming.mpdplayer.EXTRA_PORT"

class SettingsActivity : AppCompatActivity() {

    var ip : String? = null
    var port : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun onConnectPressed(view: View){

        GlobalClass.IP = editTextIP.text.toString()
        GlobalClass.port = Integer.parseInt(editTextPort.text.toString())


    }
}
