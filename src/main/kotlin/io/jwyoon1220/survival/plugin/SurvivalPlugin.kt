package io.jwyoon1220.survival.plugin

import io.jwyoon1220.survival.plugin.chzzk.ChzzkListener
import io.jwyoon1220.survival.plugin.supportz.PlayerTracking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import xyz.r2turntrue.chzzk4j.ChzzkBuilder
import java.util.*


class SurvivalPlugin : JavaPlugin() {
    // 이거 다른데다 갔다 쓰면 고소미 직접 가져다줌.
    // 다른데다 쓰면 고소한다. 내 네이버 계정이다. 진짜 쓰면 고소할거임
    private val nidAUT = "/36x6nQEBNgaYBTPpVzUOsWEpTEKQmZBCdoXc6p8MLSFVDVD29S+KoKOolEyRmtz"
    private val nidSES = "AAABi029eZDMgXaZDp74Bd5FStxH3GnZRF8YgZDLaqdUHS4i6lIEanUB5YQ0biO4bsu4MA1yBW2A4U+ZGNA3bflzOD5rbHgH7TsmwlcGNTobJ5SaVInr08Si7Zz0XwXQT2ut+D2BecFHWJ1ZhuYKYqIfc8g/WctMrF6NzH8f0xgkHgvHefary8MF4hgNb4czgAdWlZ1L1eRrOlXrsaxdvl/eWAp8vUxjM4bRYoYTcj69I/RgNMirrXkxt4ZCJuttXOYM4fGp3XZB4zZ8rYFAR1Fjxo97cJ0o4SxGk9SWsKXHfNG2zzP92ImlJQKe5/AbcOxgCWPhlaCKJzfbuF8cxlZWe1to2Rq2OGYwrzYngHNdcJTLcMq73ycC0ufhGesD/wL+taSozrDNqCufcx8I8wHjKSk3sm96CvuUqeARBcWvGKYnjo+U8RY6tjpkU03V4Q8Tcq+H3+eHtT8pRSjY88EVv7/+T66JbqfjtfxDYH966Wc2/qFuO2IYOQB2StTl5Rfv9H33C2sZcivxn4iga0fizJg="
    private val chzzk = ChzzkBuilder().withAuthorization(nidAUT, nidSES).build()
    var chat = chzzk.chat("4d951e8a8538899f8f37aa80827fc4d5").withChatListener(ChzzkListener(this)).build()


    override fun onEnable() {
        val scoreboardManager = Bukkit.getScoreboardManager()
        val scoreboard = scoreboardManager.mainScoreboard
        lateinit var zteam: Team
        lateinit var pteam: Team
        server.setWhitelist(true)
        config.set("chzzk.msgc", 0)
        logger.info("치지직 접속중...")
        chat.connectAsync()
        logger.info("접속 성공")
        server.maxPlayers = 50

        if (scoreboard.getTeam("player") == null) {
            pteam = scoreboard.registerNewTeam("player").apply {
                displayName(Component.text("생존자"))
                color(NamedTextColor.GREEN)
            }
        } else {
            pteam = scoreboard.getTeam("player")!!
        }
        if (scoreboard.getTeam("zombie") == null) {
            zteam = scoreboard.registerNewTeam("zombie").apply {
                displayName(Component.text("좀비"))
                color(NamedTextColor.RED)
            }
        } else {
            zteam = scoreboard.getTeam("zombie")!!
        }
        addVaccineRecipe()
        EventListener(this, zteam, pteam)
        PlayerTracking(this)
        RegisterKommand(this, chat)
        saveDefaultConfig()

        for (world in Bukkit.getWorlds()) {
            world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)
            world.difficulty = Difficulty.HARD
        }
    }

    override fun onDisable() {
        chat.closeAsync()
    }
    fun createVaccine(): ItemStack {
        // ItemStack 생성 (물약으로 만들기 위해 Material를 POTION으로 설정)
        val vaccine = ItemStack(Material.LINGERING_POTION, 1)


        // PotionMeta 생성 및 설정
        val meta = vaccine.itemMeta as PotionMeta
        meta.setDisplayName("§a좀비 바이러스 백신(가스)") // 이름 설정


        // 설명 추가
        meta.lore = Arrays.asList(
            "§7이 포션은 좀비 바이러스의",
            "§7감염을 예방하고 증상을 완화합니다.",
            "§7사용 시 공격력이 감소합니다."
        )


        // 나약함 효과 추가
        meta.addCustomEffect(PotionEffect(PotionEffectType.WEAKNESS, 600, 5, true), true)


        // 메타데이터를 ItemStack에 적용
        vaccine.setItemMeta(meta)

        return vaccine // 완성된 ItemStack 반환
    }


    private fun addVaccineRecipe() {
        // 커스텀 아이템 생성 (네더의 별)
        val vaccineItem = ItemStack(Material.GHAST_TEAR)

        // 아이템 메타 설정
        val meta: ItemMeta = vaccineItem.itemMeta ?: return
        meta.setDisplayName("${ChatColor.GREEN}좀비 바이러스 백신(고(固)형제)") // 아이템 이름
        meta.lore = listOf("백신이지만 치료제도 된다고 한다.", "뭔가 맛이 매우 없을거 같다.") // 아이템 설명
        vaccineItem.itemMeta = meta

        // 레시피 키 생성
        val key = NamespacedKey(this, "vaccine")


        val recipe = ShapelessRecipe(key, vaccineItem).apply {
            addIngredient(Material.GOLD_INGOT)    // 금괴
            addIngredient(Material.GHAST_TEAR)     // 네더의 별
            addIngredient(Material.ZOMBIE_HEAD)      // 좀비 머리
            addIngredient(Material.MILK_BUCKET)      // 우유 양동이
            addIngredient(Material.DIAMOND)          // 다이아몬드
        }

        val key2 = NamespacedKey(this, "vaccine_gas")


        val recipe2 = ShapelessRecipe(key2, createVaccine()).apply {
            addIngredient(Material.WATER_BUCKET)
            addIngredient(vaccineItem)

        }
        // 레시피 등록
        server.addRecipe(recipe)
        //server.addRecipe(recipe2)

        // 서버 메시지 출력
        logger.info("백신 제작법이 추가되었습니다!")
    }


}