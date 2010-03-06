package rl.item;

import jade.core.Actor;
import jade.core.Console;
import java.awt.Color;
import rl.creature.Player;
import rl.item.Enchantment.ProtoEnchant;
import rl.world.Script;

public class Item extends Actor
{
	public enum Slot
	{
		ARMOR(true), WEAPON(true), BOW(true), SCROLL(false);
		private boolean isEquipment;

		Slot(boolean isEquipment)
		{
			this.isEquipment = isEquipment;
		}
	}

	private Slot slot;
	private Enchantment enchantment;

	public Item(ProtoItem prototype)
	{
		this(prototype.slot, prototype.face, prototype.color, prototype.enchantment);
	}

	private Item(Slot slot, char face, Color color, ProtoEnchant enchantment)
	{
		super(face, color);
		this.slot = slot;
		if(enchantment != null)
			enchant(new Enchantment(enchantment.effect, enchantment.magnitude));
	}

	private void enchant(Enchantment enchantment)
	{
		this.enchantment = enchantment;
		enchantment.attachTo(this);
	}

	public Slot slot()
	{
		return slot;
	}

	@Override
	public void act()
	{
	}

	public void use()
	{
		if(slot == Slot.SCROLL)
		{
			if(enchantment != null)
			{
				Player player = world().getActorAt(pos(), Player.class);
				player.learnSpell(enchantment.effect());
				player.appendMessage(player + " learns " + enchantment.effect());
				appendMessage(this + " vanishes in a puff of smoke");
				expire();
			}
			else
				appendMessage("Scroll is blank");
		}
	}

	public boolean isEquipment()
	{
		return slot.isEquipment;
	}

	public void activateEnchant()
	{
		if(enchantment != null)
			enchantment.activate();
	}

	public void deactivateEnchant()
	{
		if(enchantment != null)
			enchantment.deactivate();
	}
	
	public void attachArtifactScript()
	{
		Script script = new Script()
		{
			public void act()
			{
				if(player() == holder())
				{
					Console console = console();
					console.saveBuffer();
					console.clearBuffer();
					console.buffLine(0, "The thingi-ma-jib feels powerful!", Color.white);
					console.refreshScreen();
					console.getKey();
					console.recallBuffer();
					console.refreshScreen();
					expire();
				}
			}
		};
		script.attachTo(this);
	}
	
	public static class ProtoItem
	{
		public Slot slot;
		public char face;
		public Color color;
		public ProtoEnchant enchantment;

		public ProtoItem(Slot slot, char face, Color color, ProtoEnchant enchantment)
		{
			this.slot = slot;
			this.face = face;
			this.color = color;
			this.enchantment = enchantment;
		}
	}
}
