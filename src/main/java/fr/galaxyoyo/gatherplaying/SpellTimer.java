package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;

public class SpellTimer
{
	private final List<CardShower> spells = Lists.newArrayList();
	private final Party party;
	private final Runnable run = () -> {
		try
		{
			Thread.sleep(3000L);
			handle();
		}
		catch (InterruptedException ignored)
		{
		}
	};
	private Thread timer;

	public SpellTimer(CardShower shower, Party party)
	{
		super();
		this.party = party;
		timer = new Thread();
		addSpell(shower);
	}

	public void addSpell(CardShower shower)
	{
		spells.add(shower);
		timer.interrupt();
		party.broadcastEvent(Party.EventType.CAST_SPELL, shower.played);
		timer = new Thread(run);
		timer.start();
		if (Utils.getSide() == Side.CLIENT)
		{
			Platform.runLater(() -> {
				Client.getStackPane().getChildren().add(shower);
				StackPane.setAlignment(shower, Pos.CENTER);
			});
		}
	}

	public void handle()
	{
		for (CardShower shower : spells)
		{
			PlayedCard card = shower.played;
			if (Utils.getSide() == Side.SERVER)
				shower.destroy();
			if (card.type.isPermanent())
			{
				party.getData(card.controller).getPlayed().add(card);
				if (Utils.getSide() == Side.CLIENT)
				{
					Platform.runLater(() -> Client.getStackPane().getChildren().remove(shower));
					if ((card.type.is(CardType.ENCHANTMENT) && !card.subtypes.contains(SubType.valueOf("Aura"))) || card.type.is(CardType.ARTIFACT) ||
							 card.type.is(CardType.PLANESWALKER))
					{
						if (card.owner == Client.localPlayer)
							Platform.runLater(() -> GameMenu.INSTANCE.enchants.getChildren().add(shower));
						else
							Platform.runLater(() -> GameMenu.INSTANCE.adverseEnchants.getChildren().add(shower));
					}
					else
					{
						if (card.owner == Client.localPlayer)
							Platform.runLater(() -> GameMenu.INSTANCE.creatures.getChildren().add(shower));
						else
							Platform.runLater(() -> GameMenu.INSTANCE.adverseCreatures.getChildren().add(shower));
					}
					Platform.runLater(() -> HBox.setMargin(shower, new Insets(0.0D, 10.5D, 0.0D, 10.5D)));
				}
			}
			else
			{
				shower.destroy();
				party.getData(card.controller).getGraveyard().add(new OwnedCard(card.getCard(), card.owner, card.foiled));
				if (Utils.getSide() == Side.CLIENT)
				{
					Platform.runLater(() -> Client.getStackPane().getChildren().remove(shower));
					if (card.owner == Client.localPlayer)
						GameMenu.INSTANCE.playerInfos.graveyard(card);
					else
						GameMenu.INSTANCE.adverseInfos.graveyard(card);
				}
			}
		}
		party.currentSpell = null;
	}
}