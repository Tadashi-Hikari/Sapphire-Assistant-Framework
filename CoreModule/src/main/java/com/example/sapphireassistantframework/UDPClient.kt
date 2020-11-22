package www.mabase.tech.mycroft

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import android.util.Log


// UDPClient is an object that takes the utterance, and runs it in its own thread
class UDPClient(var utterance: String): Service(), Runnable{

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun run(){
        Log.v("Utterance", this.utterance)
        System.out.println("The utterance is "+this.utterance)
        UDPClientSend(this.utterance)
    }

    // this needs to be run in a separate thread
    fun UDPClientSend(message: String) {
        var address: InetAddress? = null
        try {
            address = InetAddress.getByName("10.0.2.2")
            val ds = DatagramSocket()
            val dp = DatagramPacket(message.toByteArray(), message.length, address, 9999)
            ds.send(dp)
        } catch (e: Exception) {
            e.printStackTrace()
            println("There is an error. It didn't send")
        }
    }
}