package org.ombr24.mb1.config;

import java.util.HashMap;
import java.util.Map;

public class Reporters {
    public final static Map<String, String> REPORTERS_MAP = initReportersMap();

    private static Map<String, String> initReportersMap() {
        Map<String, String> map = new HashMap<>();
        map.put("1 мр", "командира 1 мр");
        map.put("2 мр", "командира 2 мр");
        map.put("3 мр", "командира 3 мр");
        map.put("мінбатр", "командира мінбатр");
        map.put("РБАК", "командира рБпАК");
        map.put("вптрк", "командира вПТРК");
        map.put("ГВ", "командира гв");
        map.put("кулв", "командира кулв");
        map.put("зрв", "командира зрв");
        map.put("РВ", "командира рв");
        map.put("ісв", "командира ісв");
        map.put("ВРЕБ", "командира відРЕБ");
        map.put("вз", "командира вз");
        map.put("втз", "командира втз");
        map.put("вмз", "командира вмз");
        map.put("мп", "начальника мп");
        return map;
    }
}
