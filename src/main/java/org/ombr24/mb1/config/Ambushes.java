package org.ombr24.mb1.config;

import java.util.HashSet;
import java.util.Set;

public class Ambushes
{
    public final static Set<String> REPORTERS_MAP = initReportersMap();

    private static Set<String> initReportersMap() {
        Set<String> map = new HashSet<>();
        map.add("Дим");
        map.add("Троя");
        map.add("Чиж");
        map.add("Садиба");
        map.add("Карпати");
        map.add("Хмельниццький");
        map.add("Тернопіль");
        map.add("Кріт");
        map.add("Гайка");
        map.add("Свисток");

        return map;
    }
}
