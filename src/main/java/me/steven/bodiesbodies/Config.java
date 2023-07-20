package me.steven.bodiesbodies;

public class Config {
    public static Config CONFIG = new Config();
    public int bodyTurnSkeletonTime = 20*60*30; // 30 minutes

    public int bodyAccessibleByAnyoneAfter = 20*60*30; // 30 minutes

    public int emptyBodyDisappearAfter = 20*60*10; // 10 minutes

    public int nonEmptyBodyDisappearAfter = -1; // disabled by default
}
