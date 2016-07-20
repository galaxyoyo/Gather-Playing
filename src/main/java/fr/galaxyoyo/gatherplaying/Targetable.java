package fr.galaxyoyo.gatherplaying;

import javafx.scene.Node;

public interface Targetable
{
	int getDamages();

	void sendDamages(int damages);

	void resetDamages();

	Node getVisible();
}
