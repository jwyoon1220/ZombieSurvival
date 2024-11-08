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
import xyz.r2turntrue.chzzk4j.chat.*
import java.util.*


class SurvivalPlugin : JavaPlugin() {

    private val NID_AUT = "YGue3cb4RiSLKfinJW0ZyZLiLErHecqaUb+D0f0pdhljhT/GJrCl4vpW9lb9TY/i"
    private val NID_SES = "AAABjKkYuzomLXC7NVDDeGdTeye100FQ/E6fug6LFTjStjlLBhqWPJeWj2VEVfgY4xQ1GvMcsf8pm8qqwrFlQXiaC8bN3Pp+LWB6Fb9LSmxzrPj64zABkOcfcxZ6HkGCm6RAkcFe+m13UHZvXqOwQGVHQOyj3XakuB3p9MYIa5Xchxrs/maWKaazEb4p+4z1usU491P0ltVrzUM2qLjx5MRoz+c7ta+YQyK3WAAcOSZmgYoVh1aCGLLcMxZLs8YqTQ1Ouojk2BmNHw0puoeL5J6gN9zOznA58kvDJeOFA+rEBAzXzcFs0eS16axaRGhb/X1J3fo8G9BotfL54wfrglbC89cWvm52/lOeihOLB97qAI2dTIEswmKKP/bMyoc0F1A9oWHxELp1JAXrJ4iFopq0+hYF/wCruclo9siX0BeaDYQeirIYjknWMUBOYogH2ue3BpogqZTHzRiGTw/9hwQjhOsVoaaEa4Imjyaqu0KxtwTZE5F2wpiQil5BDrXkwMzP93uZR8zpKt4fteX67+RUB+A="
    private val chzzk = ChzzkBuilder().withAuthorization(NID_AUT, NID_SES).build()
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
        EventListener(this, zteam, pteam, )
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