package io.agora.app.sample.ht

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.sample.ht.databinding.LaunchActivityBinding
import io.agora.app.sample.ht.utils.PermissionHelp

class LaunchActivity : AppCompatActivity() {

    private val mBinding by lazy {
        LaunchActivityBinding.inflate(LayoutInflater.from(this))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        val permissionHelp = PermissionHelp(this)

        mBinding.btnJoin.setOnClickListener {
            val channelName = mBinding.etChannelName.text.toString()
            if (channelName.isEmpty()) {
                Toast.makeText(this@LaunchActivity, "Channel Name is empty!", Toast.LENGTH_LONG).show()
            }else{
                permissionHelp.checkMicPerm(
                    granted = {
                        RoomActivity.start(this@LaunchActivity, channelName)
                    },
                    unGranted = {
                        Toast.makeText(this@LaunchActivity, "Leak Permissions!", Toast.LENGTH_LONG).show()
                    },
                    true
                )
            }
        }
    }



}