package www.mabase.tech.mycroft

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

// This class will need to run in its own loop, I suppose
// This needs to be more intergrated, since it is the dispatch method as well.
// Its thread can't be blocked when dispatching a message
// I may need to implemente a queue as well
class UDPServer: Runnable{

    private var serverState: Boolean = false
    private var socket: DatagramSocket? = null

    fun UDPServer(){
        var address: InetAddress = InetAddress.getByName("127.0.0.1")
        var port = 9000
        val socket = DatagramSocket(port)

        //Found this number on wikipedia. May need to change it
    }

    override fun run() {
        var bMessage: ByteArray = ByteArray(65536)
        var packet = DatagramPacket(bMessage, bMessage.size)
        var message = ""

        try {
            // This is basically the main program "loop"
            while(serverState){
                socket?.receive(packet)
                message = String(bMessage, 0, packet.length)
                Log.i("UDPServer",message)
            }
        }catch(e: Throwable){
            Log.e("UDPServer", "${e}")
        }
    }

    fun startServer(){
        this.serverState = true
        this.run()
    }

    fun shutdownServer(){
        this.serverState = false
    }
}