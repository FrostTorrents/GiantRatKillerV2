package Rias;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import Rias.util.mouse.paint.DrawMouseEvent;

@ScriptManifest(name = "Giant Rat Killer", description = "Kills giant rats & loots raw meat", author = "Rias Gremory", version = 1.0, category = Category.COMBAT, image = "https://i.imgur.com/OoKb3MF.png")

public class Main extends AbstractScript {

	Area Ratpit = new Area(3190, 3209, 3199, 3201, 0);

	private Image background = getImage("https://i.imgur.com/JmEP2jL.jpeg");
	private AntiBan antiban;
	private int killCount = 0;
	private int lootCount = 0;
	private long startTime;
	String foodName = null;
	private static final Font font1 = new Font("Arial Nova Cond", 1, 12);

	public void onStart() {
		log("Welcome to Giant Rat Killer v1.0");
		log("if any issues contact [Rias Gremory#2037]");
		this.antiban = new Rias.AntiBan(this);
		startTime = System.currentTimeMillis();
		SkillTracker.start();
		System.currentTimeMillis();
		Skills.getExperience(Skill.HITPOINTS);
	}

	@Override
	public int onLoop() {

		if (this.antiban.doRandom()) {
			log("Script-specific random flag triggered");
		}

		attacK();

		walkToBank();

		walkToRats();

		return this.antiban.antiBan();

	}
	

	private void attacK() {
		if (!getLocalPlayer().isInCombat() && !Inventory.isFull() && Ratpit.contains(getLocalPlayer())) {

			NPC Rats = NPCs.closest("Giant rat");
			

			GroundItem meat = GroundItems.closest("Raw rat meat");

			if (Rats != null && Rats.hasAction("Attack") && !Rats.isInCombat()
					&& (meat == null || meat.distance() >= 5)) {
				Rats.interact("Attack");
				killCount++;
				log("Attacking Lord of rats");
				randomSleep();
				sleepUntil(() -> !getLocalPlayer().isInteractedWith(), 10000);

				if (Combat.getHealthPercent() <= 32 && !getLocalPlayer().isInCombat() && Ratpit.contains(getLocalPlayer())) {
					Inventory.interact("Shrimps", "Eat");
					log("num");
				}
				
				if (!getLocalPlayer().isInteractedWith() && !Inventory.isFull() && Ratpit.contains(getLocalPlayer())) {
					GroundItem Fmeat = GroundItems.closest("Raw rat meat");
					int looting = Inventory.count("Raw rat meat");
					if (Fmeat != null && Fmeat.hasAction("Take")) {
						Fmeat.interact("Take");
						lootCount++;
						log("Looting Rat Meat");
						randomSleep();
						sleepUntil(() -> Inventory.count("Raw rat meat") > looting, 5000);
					}
				}
			}

			if (!getLocalPlayer().isInteractedWith() && !Inventory.isFull() && Ratpit.contains(getLocalPlayer())) {

				if (meat != null && meat.hasAction("Take") && meat.distance() <= 5) {
					meat.interact("Take");
					randomSleep();
				}
			}

		}
	}

	 public void walkToBank() {
		 if (Inventory.isFull()) {
	        while (!Bank.isOpen()) {
	            Bank.openClosest();
	            randomSleep();
	        }


	        if (Bank.isOpen()) {
	        	randomSleep();

	            Bank.depositAllItems();
	        }

	        while (!Bank.isOpen()) {
	            Bank.openClosest();
	            Mouse.move();
	             randomSleep();
	        }

	        if(Bank.isOpen()) {
	            Bank.withdraw("Shrimps", 7);
	            Mouse.move();
	            randomSleep();
	            }

	        while (Bank.isOpen()) {

	            Bank.close();
	            Mouse.move();
	            randomSleep();

	        }
		 }

	    }

	private void walkToRats() {
		if (!Ratpit.contains(getLocalPlayer()) && !Inventory.isFull()) {
			Walking.walk(Ratpit.getRandomTile());
			log("Walking back");
			randomSleep();
			sleepUntil(() -> Walking.getDestinationDistance() > 5, 5000);
		}
	}

	public void onPaint(final Graphics2D g)
	{
		long runtime = (System.currentTimeMillis() - startTime)/1000;
		long xpGained = SkillTracker.getGainedExperience(Skill.STRENGTH);
		g.setColor(Color.WHITE);
		g.setFont(font1);
		g.drawImage(background, 0, 339, null);
		g.drawString("XP Gained (Hr): " + xpGained + " (" + (int)(xpGained*(3600/(double)runtime)) + ")", 15,410);
		g.drawString("Time running: " + String.format("%02d:%02d:%02d", runtime / 3600, (runtime % 3600) / 60, runtime % 60), 390,355);
		g.drawString("KillCount: " + killCount, 250, 355);
		g.drawString("LootCount: " + lootCount, 250, 365);
		g.drawString("Anti-Ban Status: " + (this.antiban.getStatus().equals("") ? "Inactive" : this.antiban.getStatus()), 15,355);
		g.drawString("HP: " + Skills.getRealLevel(Skill.HITPOINTS) + " (+"+ SkillTracker.getGainedLevels(Skill.HITPOINTS) + ")" + " ["+ SkillTracker.getGainedExperiencePerHour(Skill.HITPOINTS) + "]", 15, 425);
		g.drawString("Attack: " + Skills.getRealLevel(Skill.ATTACK) + " (+" + SkillTracker.getGainedLevels(Skill.ATTACK)+ ")" + " [" + SkillTracker.getGainedExperiencePerHour(Skill.ATTACK) + "]",15, 440);
		g.drawString("Strength: " + Skills.getRealLevel(Skill.STRENGTH) + " (+"+ SkillTracker.getGainedLevels(Skill.STRENGTH) + ")" + " ["+ SkillTracker.getGainedExperiencePerHour(Skill.STRENGTH) + "]", 15, 455);
		g.drawString("Defence: " + Skills.getRealLevel(Skill.DEFENCE) + " (+"+ SkillTracker.getGainedLevels(Skill.DEFENCE) + ")" + " ["+ SkillTracker.getGainedExperiencePerHour(Skill.DEFENCE) + "]", 15, 470);
		DrawMouseEvent.getInstance().setTrailColor(new Color(199, 36, 177));
		DrawMouseEvent.getInstance().setTrailColor(Color.red);
		DrawMouseEvent.getInstance().drawTrail(g);
		DrawMouseEvent.getInstance().drawPlusMouse(g);
	}

	

	private Image getImage(String url){
		try {
			return ImageIO.read(new URL(url));
		}catch (IOException e){
			return null;
		}
	}

	private void randomSleep() {
		final int randomSleepNumber = Calculations.random(0, 20);
		if (randomSleepNumber <= 4) {
			sleep(1200, 1600);
		} else if (randomSleepNumber <= 9) {
			sleep(1000, 1400);
		} else if (randomSleepNumber <= 16) {
			sleep(1300, 1800);
		} else if (randomSleepNumber <= 20) {
			sleep(2100, 2500);
		}
	}
}