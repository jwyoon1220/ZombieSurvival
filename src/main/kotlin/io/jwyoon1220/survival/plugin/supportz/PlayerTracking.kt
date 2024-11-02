package io.jwyoon1220.survival.plugin.supportz

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.security.SecureRandom

class PlayerTracking(plugin: JavaPlugin) : Listener {
    private val javaPlugin: JavaPlugin = plugin
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR) {
            if (event.player.isZombie()) {
                if (event.player.inventory.itemInMainHand.type == Material.DIAMOND) {
                    val amount = event.player.inventory.itemInMainHand.amount
                    val subeda = amount - 1
                    event.player.setItemInHand(ItemStack(Material.DIAMOND, subeda))
                    event.player.playSound(event.player.location, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
                    val players = event.player.location.getNearbyPlayers(1000.0)
                    val sb = StringBuilder()
                    val message = ("${ChatColor.GREEN}탐지된 생존자:\n")
                    for (player in players) {
                       if (player.isPlayer()) {
                           player.playSound(player.location, Sound.AMBIENT_CAVE, 1f, 1f)
                           player.sendMessage("${ChatColor.RED}좀비의 기운이 느껴집니다....")
                           val random = SecureRandom()
                           val r = random.nextInt(10)
                           if (r == 1) {
                               sb.append("${player.name}: X:${player.location.x}, Z:${player.location.z}\n")
                           } else if (r == 2 || r == 3) {
                               sb.append("${player.name}: X:${player.location.x}\n")
                           } else if (r == 4 || r == 5) {
                               sb.append("${player.name}: Z:${player.location.z}\n")
                           } else {
                               sb.append("${player.name}: 탐지 실패\n")
                           }

                       }
                    }
                    if (sb.isEmpty()) event.player.sendMessage("${message} 탐지된 생존자가 없습니다.") else event.player.sendMessage("${message}${sb}")
                }
                // 에메날드 사용시 사용자가 좀비라면 빙의시킴.
                if (event.player.inventory.itemInMainHand.type == Material.EMERALD) {
                    val originLocation = event.player.location
                    val amount = event.player.inventory.itemInMainHand.amount
                    event.player.inventory.itemInMainHand.amount = amount - 1
                    val players = event.player.location.getNearbyPlayers(1000.0)
                    val random = SecureRandom()
                    val rangeMax = players.size
                    val getIndex = random.nextInt(rangeMax)
                    if (players.toMutableList()[getIndex].isPlayer()) {
                        event.player.gameMode = GameMode.SPECTATOR
                        event.player.teleportAsync(players.toMutableList()[getIndex].location)
                        players.toMutableList()[getIndex].sendMessage(
                            "${ChatColor.RED}좀비의 기운이 느껴집니다....\n" +
                                "좀비가 당신에게 빙의합니다."
                        )
                        val a = event.player.inventory.itemInMainHand.amount - 1
                        event.player.inventory.itemInMainHand.amount = a
                        object : BukkitRunnable() {
                            override fun run() {
                                event.player.teleportAsync(originLocation)
                                event.player.gameMode = GameMode.SURVIVAL
                            }
                        }.runTaskLater(javaPlugin, 10 * 20)
                    } else {
                        event.player.sendMessage("${ChatColor.RED}생존자 탐지를 실패하였습니다.\n" +
                                "당신의 운이 안좋거나 당신의 근처에 생존자가 없습니다.")
                    }
                }
            }
        }
    }

    fun Player.isZombie(): Boolean {
        val plugin = server.pluginManager.getPlugin("Survival")
        val config = plugin?.config
        if (!config!!.getBoolean("partner.${player?.name}")) {
            return true
        } else {
            return false
        }
    }
    fun Player.isPlayer() : Boolean {
        val plugin = server.pluginManager.getPlugin("Survival")
        val config = plugin?.config
        if (config!!.getBoolean("partner.${player?.name}")) {
            return true
        } else {
            return false
        }
    }
}