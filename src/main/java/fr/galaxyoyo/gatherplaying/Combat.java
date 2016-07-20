package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.galaxyoyo.gatherplaying.capacity.DeathTouch;
import fr.galaxyoyo.gatherplaying.capacity.DoubleStrike;
import fr.galaxyoyo.gatherplaying.capacity.FirstStrike;
import fr.galaxyoyo.gatherplaying.capacity.Trample;
import fr.galaxyoyo.gatherplaying.protocol.packets.PacketMixDestroyCard;

import java.util.*;

public class Combat
{
	private final Party party;
	private final Map<PlayedCard, Targetable> attackers = Maps.newHashMap();
	private final Map<PlayedCard, List<PlayedCard>> blockers = Maps.newHashMap();

	public Combat(Party party)
	{
		this.party = party;
	}

	public void addAttacker(PlayedCard card, Targetable target)
	{
		if (!(target instanceof PlayerData) && !(target instanceof PlayedCard && ((PlayedCard) target).getType().is(CardType.PLANESWALKER)))
			return;
		attackers.put(card, target);
	}

	public void addBlocker(PlayedCard blocker, PlayedCard blocked)
	{
		blockers.putIfAbsent(blocker, Lists.newArrayList());
		blockers.get(blocker).add(blocked);
	}

	public void resolveDamages()
	{
		for (Map.Entry<PlayedCard, Targetable> initAttacker : attackers.entrySet())
		{
			PlayedCard attacker = initAttacker.getKey();
			if (attacker.hasCapacity(FirstStrike.class) || attacker.hasCapacity(DoubleStrike.class))
			{
				boolean blocked = false;
				int dealtDamages = 0;
				for (Map.Entry<PlayedCard, List<PlayedCard>> entry : blockers.entrySet())
				{
					if (!entry.getValue().contains(attacker))
						continue;
					blocked = true;
					int toughness = entry.getKey().getType().is(CardType.CREATURE) ? entry.getKey().getToughness() : entry.getKey().getLoyalty();
					int toDeal = Math.min(attacker.getPower() - dealtDamages, toughness - entry.getKey().getDamages());
					entry.getKey().sendDamages(toDeal);
					if (attacker.hasCapacity(DeathTouch.class) && toDeal > 0)
						entry.getKey().destroy(PacketMixDestroyCard.Destination.GRAVEYARD);
					dealtDamages += toDeal;
				}

				if (!blocked || attacker.hasCapacity(Trample.class))
				{
					int damages = attacker.getPower() - dealtDamages;
					initAttacker.getValue().sendDamages(damages);
				}
			}
		}

		for (Map.Entry<PlayedCard, List<PlayedCard>> initBlocker : blockers.entrySet())
		{
			PlayedCard blocker = initBlocker.getKey();
			if (blocker.hasCapacity(FirstStrike.class) || blocker.hasCapacity(DoubleStrike.class))
			{
				int dealtDamages = 0;
				for (PlayedCard blocked : initBlocker.getValue())
				{
					int toughness = blocked.getType().is(CardType.CREATURE) ? blocked.getToughness() : blocked.getLoyalty();
					int toDeal = Math.min(blocker.getPower() - dealtDamages, toughness - blocked.getDamages());
					blocked.sendDamages(toDeal);
					if (blocker.hasCapacity(DeathTouch.class) && toDeal > 0)
						blocked.destroy(PacketMixDestroyCard.Destination.GRAVEYARD);
					dealtDamages += toDeal;
				}
			}
		}

		for (Map.Entry<PlayedCard, Targetable> normalAttacker : attackers.entrySet())
		{
			PlayedCard attacker = normalAttacker.getKey();
			if (attacker.getDamages() >= attacker.getToughness())
				continue;
			if (!attacker.hasCapacity(FirstStrike.class))
			{
				boolean blocked = false;
				int dealtDamages = 0;
				for (Map.Entry<PlayedCard, List<PlayedCard>> entry : blockers.entrySet())
				{
					if (!entry.getValue().contains(attacker))
						continue;
					blocked = true;
					int toughness = entry.getKey().getType().is(CardType.CREATURE) ? entry.getKey().getToughness() : entry.getKey().getLoyalty();
					int toDeal = Math.min(attacker.getPower() - dealtDamages, toughness - entry.getKey().getDamages());
					entry.getKey().sendDamages(toDeal);
					if (attacker.hasCapacity(DeathTouch.class) && toDeal > 0)
						entry.getKey().destroy(PacketMixDestroyCard.Destination.GRAVEYARD);
					dealtDamages += toDeal;
				}

				if (!blocked || attacker.hasCapacity(Trample.class))
				{
					int damages = attacker.getPower() - dealtDamages;
					normalAttacker.getValue().sendDamages(damages);
				}
			}
		}

		for (Map.Entry<PlayedCard, List<PlayedCard>> normalBlocker : blockers.entrySet())
		{
			PlayedCard blocker = normalBlocker.getKey();
			if (blocker.getDamages() >= blocker.getToughness())
				continue;
			if (!blocker.hasCapacity(FirstStrike.class))
			{
				int dealtDamages = 0;
				for (PlayedCard blocked : normalBlocker.getValue())
				{
					int toughness = blocked.getType().is(CardType.CREATURE) ? blocked.getToughness() : blocked.getLoyalty();
					int toDeal = Math.min(blocker.getPower() - dealtDamages, toughness - blocked.getDamages());
					blocked.sendDamages(toDeal);
					if (blocker.hasCapacity(DeathTouch.class) && toDeal > 0)
						blocked.destroy(PacketMixDestroyCard.Destination.GRAVEYARD);
					dealtDamages += toDeal;
				}
			}
		}
	}
}
