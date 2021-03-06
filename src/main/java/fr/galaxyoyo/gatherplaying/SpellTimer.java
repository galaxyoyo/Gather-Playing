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
	private final List<PlayedCard> spells = Lists.newArrayList();
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

	public SpellTimer(PlayedCard card, Party party)
	{
		super();
		this.party = party;
		timer = new Thread();
		addSpell(card);
	}

	public void addSpell(PlayedCard card)
	{
		spells.add(card);
		timer.interrupt();
		party.broadcastEvent(Party.EventType.CAST_SPELL, card);
		timer = new Thread(run);
		timer.start();
		if (Utils.getSide() == Side.CLIENT)
		{
			Platform.runLater(() -> {
				CardShower shower = CardShower.getShower(card);
				shower.reload();
				Client.getStackPane().getChildren().add(shower);
				StackPane.setAlignment(shower, Pos.CENTER);
				card.setDefaultStats();
			});
		}
	}

	private void handle()
	{
		for (PlayedCard card : spells)
		{
			if (card.getType().isPermanent())
			{
				party.getData(card.getController()).getPlayed().add(card);
				if (Utils.getSide() == Side.CLIENT)
				{
					CardShower shower = CardShower.getShower(card);
					Platform.runLater(() -> {
					//	Client.getStackPane().getChildren().removeIf(n -> n instanceof CardShower && ((CardShower) n).card == card);
						Client.getStackPane().getChildren().remove(shower);
					});
					if ((card.getType().is(CardType.ENCHANTMENT) && !card.getSubtypes().contains(SubType.valueOf("Aura"))) || card.getType().is(CardType.ARTIFACT) ||
							card.getType().is(CardType.PLANESWALKER))
					{
						if (card.getOwner() == Client.localPlayer)
							Platform.runLater(() -> GameMenu.instance().enchants.getChildren().add(shower));
						else
							Platform.runLater(() -> GameMenu.instance().adverseEnchants.getChildren().add(shower));
					}
					else
					{
						if (card.getOwner() == Client.localPlayer)
							Platform.runLater(() -> GameMenu.instance().creatures.getChildren().add(shower));
						else
							Platform.runLater(() -> GameMenu.instance().adverseCreatures.getChildren().add(shower));
					}
					Platform.runLater(() -> HBox.setMargin(shower, new Insets(0.0D, 10.5D, 0.0D, 10.5D)));
				}
			}
			else
			{
				party.getData(card.getController()).getGraveyard().add(card);
				if (Utils.getSide() == Side.CLIENT)
				{
					Platform.runLater(() -> spells.stream().forEach(c -> Client.getStackPane().getChildren().remove(CardShower.getShower(c))));
					if (card.getOwner() == Client.localPlayer)
						GameMenu.instance().playerInfos.graveyard(card);
					else
						GameMenu.instance().adverseInfos.graveyard(card);
				}
			}
		}
		party.setCurrentSpell(null);
	}
}