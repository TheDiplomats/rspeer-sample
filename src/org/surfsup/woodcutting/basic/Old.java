package org.surfsup.woodcutting.basic;

//@ScriptMeta(developer = "surfs_up", name = "SurfingArise", desc="just a test script")

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptCategory;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.text.NumberFormat;

@ScriptMeta(desc = "Basic script for cutting lumbridge trees", developer = "surfs_up", name = "Basic Cutter", category = ScriptCategory.WOODCUTTING, version = 1)
public class Old extends Script {

    //TODO Set as customisable
    //private static Area area = Area.rectangular(2983, 3191, 2994, 3181);


    private long START_TIME;
    private int START_XP;

    private static boolean initialised = false;

    @Override
    public void onStart() {
        Log.fine("Running Basic Cutter by Surf, compliments Shteve");

        super.onStart();
    }

    @Override
    public int loop() {
//TODO Reimplement the login
        if (!initialised){
            if (attemptInitialisation())
                initialised = true;
            else
                setStopping(true);
        }else {
            Player localPlayer = Players.getLocal();
            if(!localPlayer.isMoving()) {
                if(!localPlayer.isAnimating()){
                    chopTree();
                } else {
                    Time.sleepUntil(() -> !localPlayer.isMoving(), 1500);
                }

            } else {
                Time.sleepUntil(() -> !localPlayer.isMoving(), 1500);
            }
        }

        return 988;
    }

    @Override
    public void onStop() {

        long millisecondsRan = System.currentTimeMillis() - START_TIME;

        int xpGained = Skills.getExperience(Skill.WOODCUTTING) - START_XP;
        String xpGainedString = NumberFormat.getIntegerInstance().format(xpGained);

        Log.fine("Run Time: " + formatTime(millisecondsRan) + " | XP Gained: " + xpGainedString + " | XP/HR: " +  getXPPerHour(xpGained,millisecondsRan));
        super.onStop();
    }

    private boolean attemptInitialisation() {
        int i = 0;
        while (i < 3) {
            if (Game.isLoggedIn()) {
                /*
                //Attempted fix for below code executing before logged in
                InterfaceComponent loginPlayBtn = Interfaces.getComponent(378,81);
                if (loginPlayBtn != null && loginPlayBtn.isVisible()) {
                    Log.info("Login Attempting login from woodcut script.");
                    loginPlayBtn.click();
                    Time.sleepUntil(()->Interfaces.getComponent(378,81) == null, 5000);
                }
                */


                Time.sleep(3000);

                START_TIME = System.currentTimeMillis();
                START_XP = Skills.getExperience(Skill.WOODCUTTING);

                if (!playerHasAxe()) {
                    Log.severe("You need an axe to chop trees");
                    return false;
                }

                if (!Movement.isRunEnabled())
                    Movement.toggleRun(true);

                return true;
            } else {
                Log.info("Not logged in, waiting. Attempt #" + (i + 1));
                Time.sleepUntil(Game::isLoggedIn, 10000);
            }
            i++;
        }
        Log.severe("Sorry but we couldn't log you in. Stopping.");
        return false;
    }

    private boolean playerHasAxe(){
        //Wielding axe
        Item mainHandWeapon = EquipmentSlot.MAINHAND.getItem();
        if (mainHandWeapon != null && mainHandWeapon.getName().contains(" axe"))
            return true;

        //Got axe in Invent
        Item[] items = Inventory.getItems(x -> x.getName().contains(" axe"));
        if (items.length > 0)
            return true;

        //Not got axe
        return false;
    }


    private void chopTree() {
        //Get nearest Tree in the closest 25 tiles and if not null chop it and wait
        SceneObject tree = SceneObjects.getNearest(s -> s.distance() <= 25 && s.getName().equals("Tree"));
        //SceneObject tree = SceneObjects.getNearest("Tree");
        if (tree != null && tree.interact("Chop down")) {
            Time.sleepUntil(() -> Players.getLocal().getAnimation() != -1,2000);
        }
    }

    //TODO implement deposit in bank rather than simply dropLogs

    private void dropLogs() {
        //TODO Potentially hold down shift, if possible.

        //Drop from top to bottom left to right
        int[] order = {
                0, 4, 8, 12, 16, 20, 24,
                1, 5, 9, 13, 17, 21, 25,
                2, 6, 10, 14, 18, 22, 26,
                3, 7, 11, 15, 19, 23, 27
        };

        //If item isn't an axe, drop it and wait.
        for (int i : order) {
            Item item = Inventory.getItemAt(i);
            if (item != null && !item.getName().contains("axe")) {
                item.interact("Drop");
                Time.sleep(52, 213);
            }
        }
    }


    private String getXPPerHour(int inXPGained, long inMillisecondsRan){
        double xpPerMillisecond = (inXPGained / (double)(inMillisecondsRan));
        return NumberFormat.getIntegerInstance().format((int)(xpPerMillisecond * 1000 * 60 * 60));
    }

    private String formatTime(long inMilliseconds){
        long second = (inMilliseconds / 1000) % 60;
        long minute = (inMilliseconds / (1000 * 60)) % 60;
        long hour = (inMilliseconds / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

}