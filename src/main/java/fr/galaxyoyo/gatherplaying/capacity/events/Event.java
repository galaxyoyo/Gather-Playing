package fr.galaxyoyo.gatherplaying.capacity.events;

public class Event
{
	private Result result = Result.ALLOW;

	public Result getResult() { return result; }

	public void setResult(Result result) { this.result = result; }

	public enum Result
	{
		ALLOW, DENY
	}
}