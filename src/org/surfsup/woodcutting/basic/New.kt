package org.surfsup.woodcutting.basic

import org.rspeer.runetek.adapter.component.Item
import org.rspeer.runetek.adapter.scene.SceneObject
import org.rspeer.runetek.api.Game
import org.rspeer.runetek.api.commons.Time
import org.rspeer.runetek.api.component.tab.EquipmentSlot
import org.rspeer.runetek.api.component.tab.Inventory
import org.rspeer.runetek.api.component.tab.Skill
import org.rspeer.runetek.api.component.tab.Skills
import org.rspeer.runetek.api.movement.Movement
import org.rspeer.runetek.api.scene.Players
import org.rspeer.runetek.api.scene.SceneObjects
import org.rspeer.script.Script
import org.rspeer.script.ScriptCategory
import org.rspeer.script.ScriptMeta
import org.rspeer.ui.Log
import java.text.NumberFormat

@ScriptMeta(desc = "Basic script for cutting lumbridge trees", developer = "surfs_up", name = "Basic Cutter", category = ScriptCategory.WOODCUTTING, version = 1.0)
class New : Script() {
    //TODO Set as customisable
//private static Area area = Area.rectangular(2983, 3191, 2994, 3181);
    private var START_TIME: Long = 0
    private var START_XP = 0
    override fun onStart() {
        Log.fine("Running Basic Cutter by Surf, compliments Shteve")
        super.onStart()
    }

    override fun loop(): Int { //TODO Reimplement the login
        if (!initialised) {
            if (attemptInitialisation()) initialised = true else isStopping = true
        } else {
            val localPlayer = Players.getLocal()
            if (!localPlayer.isMoving) {
                if (!localPlayer.isAnimating) {
                    chopTree()
                } else {
                    Time.sleepUntil({ !localPlayer.isMoving }, 1500)
                }
            } else {
                Time.sleepUntil({ !localPlayer.isMoving }, 1500)
            }
        }
        return 988
    }

    override fun onStop() {
        val millisecondsRan = System.currentTimeMillis() - START_TIME
        val xpGained = Skills.getExperience(Skill.WOODCUTTING) - START_XP
        val xpGainedString = NumberFormat.getIntegerInstance().format(xpGained.toLong())
        Log.fine(
            "Run Time: " + formatTime(millisecondsRan) + " | XP Gained: " + xpGainedString + " | XP/HR: " + getXPPerHour(
                xpGained,
                millisecondsRan
            )
        )
        super.onStop()
    }

    private fun attemptInitialisation(): Boolean {
        var i = 0
        while (i < 3) {
            if (Game.isLoggedIn()) { /*
                //Attempted fix for below code executing before logged in
                InterfaceComponent loginPlayBtn = Interfaces.getComponent(378,81);
                if (loginPlayBtn != null && loginPlayBtn.isVisible()) {
                    Log.info("Login Attempting login from woodcut script.");
                    loginPlayBtn.click();
                    Time.sleepUntil(()->Interfaces.getComponent(378,81) == null, 5000);
                }
                */
                Time.sleep(3000)
                START_TIME = System.currentTimeMillis()
                START_XP = Skills.getExperience(Skill.WOODCUTTING)
                if (!playerHasAxe()) {
                    Log.severe("You need an axe to chop trees")
                    return false
                }
                if (!Movement.isRunEnabled()) Movement.toggleRun(true)
                return true
            } else {
                Log.info("Not logged in, waiting. Attempt #" + (i + 1))
                Time.sleepUntil({ Game.isLoggedIn() }, 10000)
            }
            i++
        }
        Log.severe("Sorry but we couldn't log you in. Stopping.")
        return false
    }

    private fun playerHasAxe(): Boolean { //Wielding axe
        val mainHandWeapon = EquipmentSlot.MAINHAND.item
        if (mainHandWeapon != null && mainHandWeapon.name.contains(" axe")) return true
        //Got axe in Invent
        val items =
            Inventory.getItems { x: Item ->
                x.name.contains(" axe")
            }
        return if (items.size > 0) true else false
        //Not got axe
    }

    private fun chopTree() { //Get nearest Tree in the closest 25 tiles and if not null chop it and wait
        val tree =
            SceneObjects.getNearest { s: SceneObject -> s.distance() <= 25 && s.name == "Tree" }
        //SceneObject tree = SceneObjects.getNearest("Tree");
        if (tree != null && tree.interact("Chop down")) {
            Time.sleepUntil(
                { Players.getLocal().animation != -1 },
                2000
            )
        }
    }

    //TODO implement deposit in bank rather than simply dropLogs
    private fun dropLogs() { //TODO Potentially hold down shift, if possible.
//Drop from top to bottom left to right
        val order = intArrayOf(
            0, 4, 8, 12, 16, 20, 24,
            1, 5, 9, 13, 17, 21, 25,
            2, 6, 10, 14, 18, 22, 26,
            3, 7, 11, 15, 19, 23, 27
        )
        //If item isn't an axe, drop it and wait.
        for (i in order) {
            val item = Inventory.getItemAt(i)
            if (item != null && !item.name.contains("axe")) {
                item.interact("Drop")
                Time.sleep(52, 213)
            }
        }
    }

    private fun getXPPerHour(inXPGained: Int, inMillisecondsRan: Long): String {
        val xpPerMillisecond = inXPGained / inMillisecondsRan.toDouble()
        return NumberFormat.getIntegerInstance().format(xpPerMillisecond * 1000 * 60 * 60)
    }

    private fun formatTime(inMilliseconds: Long): String {
        val second = inMilliseconds / 1000 % 60
        val minute = inMilliseconds / (1000 * 60) % 60
        val hour = inMilliseconds / (1000 * 60 * 60) % 24
        return String.format("%02d:%02d:%02d", hour, minute, second)
    }

    companion object {
        private var initialised = false
    }
}