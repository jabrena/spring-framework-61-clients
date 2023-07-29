package bootiful.clients;

import java.net.URL;
import java.time.Instant;
import java.util.List;

record Planet(String name, int rotationPeriod, int orbitalPeriod, String climate,
        String gravity, String terrain, String surfaceWater, String population,
        List<Object> residents, String[] films, Instant created, Instant edited, URL url) { }