package io.jwyoon1220.survival.plugin.chzzk

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import xyz.r2turntrue.chzzk4j.chat.ChatEventListener
import xyz.r2turntrue.chzzk4j.chat.ChatMessage
import xyz.r2turntrue.chzzk4j.chat.ChzzkChat
import java.lang.IndexOutOfBoundsException


class ChzzkListener(javaPlugin: JavaPlugin) : ChatEventListener {
    private val plugin = javaPlugin
    private val NID_AUT = "4VqdjJ93CAMj7us43t8A6OKxZhicR3oD+OB5rWQetxqbv+4f0Z2yDp8T40Rhvqvj"
    private val NID_SES = "AAABrc7auK+VYIEhZaHnmlLS58DEFmMZktTquglaoBqPRAwX0SQyR0g51qg2GfTUeSj2stN1vUgZMhKVuRi5mxmGSHJjs9koftJK6zrKsSAFVNgA1WRllyfqZNKF5GGNDyjxBD4WDWUJqftphSyfhC8hMXoS+LwJvBDCVSS+hbCXzY5bElq/hvn1T773ekMBm+KVOduqSvBfedD57LGmMKenRJRmsfYDo3K+7qSd21fCVgXywzEuvUb9IuUXv5NcmbDLmPXUPHb5OL3mQgP6QKOTFZ9rsuN387CGR4SzRYEpHz/PTj/CJ1opPsISf6yFWrZVafTO+AnT4Zdurwcy6gZ+SMK/C7hshEQP3wEhKvP3vtX+bdZZiUkpvKDJidFTGtF3HhxYtbhn0lV24JueP7QEMF6riTy6oPeYkcCt5HTTOBHiLzQKvNikel2niWNbHSXQsfRsb3kvGx0wPG3rLtaxW/d3JDzgz21rydP/nc6ZyP9vBv83RAEjVsMgYdh9KsqIVuauZOr65cCYZWla9fVoR9FH+FHCcvS2xh7+fpwuvhYyg8XlCTtusRA4NXp4uMkwCA=="



    override fun onConnect(chat: ChzzkChat, isReconnecting: Boolean) {
        plugin.logger.info("재연결됨.")
        chat.connectAsync()
        // !! when re-connecting, you shouldn't need to request recent chats!
        if (!isReconnecting) chat.requestRecentChat(50)
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
    }

    override fun onChat(msg: ChatMessage) {
        if (msg.content.contains("!등록")) {

            val message = msg.content
            val username = message.split(" ")
            Bukkit.getScheduler().runTask(plugin, Runnable {
                try {
                    val isSuccess = plugin.server.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add ${username[1]}")
                    if (isSuccess) {
                        Bukkit.broadcast(Component.text("성공! (MC: ${username[1]}, CHZZK: ${msg.profile?.nickname})이(가) 등록되었습니다."))
                    } else {
                        Bukkit.broadcast(Component.text("실패. (MC: ${username[1]}, CHZZK: ${msg.profile?.nickname})이(가) 화이트리스트에 등록되지 않았습니다. (정확한 닉네임이 아니거나 명령어를 제대로 작성하지 않았습니다. '!등록 <닉네임>' 으로 작성해주세요)"))
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    Bukkit.broadcast(Component.text("${ex.javaClass}: "))
                }
            })
        } else {
            for (player in Bukkit.getOnlinePlayers()) {
                player.sendActionBar("${msg.profile?.nickname}: ${msg.content}")
            }
        }
    }
}