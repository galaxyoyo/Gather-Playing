package fr.galaxyoyo.gatherplaying;

import com.google.common.collect.Lists;
import fr.galaxyoyo.gatherplaying.client.Client;
import fr.galaxyoyo.gatherplaying.client.gui.CardShower;
import fr.galaxyoyo.gatherplaying.client.gui.GameMenu;
import fr.galaxyoyo.gatherplaying.markers.Marker;
import fr.galaxyoyo.gatherplaying.markers.MarkerType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;
import java8.util.stream.StreamSupport;

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
				CardShower shower = new CardShower(card);
				Client.getStackPane().getChildren().add(shower);
				StackPane.setAlignment(shower, Pos.CENTER);

				if (card.getCard().getType().is(CardType.CREATURE))
				{
					try
					{
						card.setPower(Integer.parseInt(card.getCard().getPower()));
					}
					catch (NumberFormatException ex)
					{
						card.setPower(0);
					}
					try
					{
						card.setToughness(Integer.parseInt(card.getCard().getToughness()));
					}
					catch (NumberFormatException ex)
					{
						card.setToughness(0);
					}
				}
				else if (card.getCard().getType().is(CardType.PLANESWALKER))
				{
					card.setLoyalty(0);
					for (int i = 0; i < card.getCard().getLoyalty(); ++i)
					{
						Marker m = MarkerType.LOYALTY.newInstance();
						m.onCardMarked(card);
						card.getMarkers().add(m);
					}
				}
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
					Platform.runLater(() -> Client.getStackPane().getChildren().remove(shower));
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
				party.getData(card.getController()).getGraveyard().add(new OwnedCard(card.getCard(), card.getOwner(), card.isFoiled()));
				if (Utils.getSide() == Side.CLIENT)
				{
					Platform.runLater(() -> StreamSupport.stream(spells).forEach(c -> Client.getStackPane().getChildren().remove(CardShower.getShower(c))));
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