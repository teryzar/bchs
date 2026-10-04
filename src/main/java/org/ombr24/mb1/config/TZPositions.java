package org.ombr24.mb1.config;

import java.util.HashSet;
import java.util.Set;

public class TZPositions
{
    public final static Set<String> POSITION_TYPE_MAP = initTzPositionMap();

    private static Set<String> initTzPositionMap() {
        Set<String> map = new HashSet<>();
        map.add("Спектра");
        map.add("Самсунг");

        return map;
    }
}
