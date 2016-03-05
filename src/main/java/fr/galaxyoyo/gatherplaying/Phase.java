package fr.galaxyoyo.gatherplaying;

public enum Phase
{
	UNTAP, UPKEEP, DRAW, MAIN, COMBAT_START, ATTACKERS, BLOCKERS, DAMAGES, COMBAT_END, MAIN_2, END;

	public boolean isMain() { return this == MAIN || this == MAIN_2; }
}