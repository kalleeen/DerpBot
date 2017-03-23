package fi.derpnet.derpbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

public class MegaHalBrain {

    private static final Logger LOG = Logger.getLogger(MegaHalBrain.class);

    private final Map<String, Set<Quad>> wordToQuad = new HashMap<>();
    private final Set<Quad> quads = new HashSet<>();
    private final Map<Quad, Set<String>> possibleNextWords = new HashMap();
    private final Map<Quad, Set<String>> possiblePreviousWords = new HashMap();
    private final Random rand = new Random();

    public void add(String message) {
        List<String> sentences = Arrays.asList(message.trim().split("\\p{Punct}\\s+"));
        sentences.forEach((sentence) -> {
            List<String> words = Arrays.asList(sentence.split("\\s+"));
            if (words.size() >= 4) {
                for (int i = 0; i < words.size() - 3; i++) {
                    Quad quad = new Quad((String) words.get(i), (String) words.get(i + 1), (String) words.get(i + 2), (String) words.get(i + 3));
                    if (!quads.contains(quad)) {
                        quads.add(quad);
                    }

                    if (i == 0) {
                        quad.canStart = true;
                    }
                    if (i == words.size() - 4) {
                        quad.canEnd = true;
                    }

                    for (int n = 0; n < 4; n++) {
                        String token = (String) words.get(i + n);
                        if (!wordToQuad.containsKey(token)) {
                            wordToQuad.put(token, new HashSet(1));
                        }
                        wordToQuad.get(token).add(quad);
                    }

                    if (i > 0) {
                        String previousToken = (String) words.get(i - 1);
                        if (!possiblePreviousWords.containsKey(quad)) {
                            possiblePreviousWords.put(quad, new HashSet(1));
                        }
                        possiblePreviousWords.get(quad).add(previousToken);
                    }

                    if (i < words.size() - 4) {
                        String nextToken = (String) words.get(i + 4);
                        if (!possibleNextWords.containsKey(quad)) {
                            possibleNextWords.put(quad, new HashSet(1));
                        }
                        possibleNextWords.get(quad).add(nextToken);
                    }
                }
            }
        });
    }

    public String getSentence() {
        return getSentence(null);
    }

    public String getSentence(String word) {
        LinkedList<String> parts = new LinkedList<>();

        List<Quad> startQuads;
        if (wordToQuad.containsKey(word)) {
            startQuads = new ArrayList<>(wordToQuad.get(word));
        } else {
            startQuads = new ArrayList<>(quads);
            if (word != null) {
                LOG.info("Didn't find chains containing word " + word);
            }
        }

        if (startQuads.isEmpty()) {
            LOG.warn("No possible words found for creating a response");
            return "";
        }

        Quad middleQuad = startQuads.get(rand.nextInt(startQuads.size()));
        Quad quad = middleQuad;

        for (int i = 0; i < 4; i++) {
            parts.add(quad.getToken(i));
        }

        while (quad.canEnd == false) {
            Set<String> nextSet = possibleNextWords.get(quad);
            if (nextSet == null) {
                break;
            }
            String[] nextTokens = nextSet.toArray(new String[0]);
            String nextToken = nextTokens[rand.nextInt(nextTokens.length)];
            quad = new Quad(quad.getToken(1), quad.getToken(2), quad.getToken(3), nextToken);
            if (!quads.contains(quad)) {
                break;
            }
            parts.add(nextToken);
        }

        quad = middleQuad;
        while (quad.canStart == false) {
            Set<String> previousSet = possiblePreviousWords.get(quad);
            if (previousSet == null) {
                break;
            }
            String[] previousTokens = previousSet.toArray(new String[0]);
            String previousToken = previousTokens[rand.nextInt(previousTokens.length)];
            quad = new Quad(previousToken, quad.getToken(0), quad.getToken(1), quad.getToken(2));
            if (!quads.contains(quad)) {
                break;
            }
            parts.addFirst(previousToken);
        }

        return parts.stream().collect(Collectors.joining(" "));
    }

    private static class Quad {

        @Expose
        String[] tokens;
        @Expose
        boolean canStart = false;
        @Expose
        boolean canEnd = false;

        public Quad(String s1, String s2, String s3, String s4) {
            tokens = new String[]{s1, s2, s3, s4};
        }

        public String getToken(int index) {
            return tokens[index];
        }

        @Override
        public int hashCode() {
            return tokens[0].hashCode()
                    + tokens[1].hashCode()
                    + tokens[2].hashCode()
                    + tokens[3].hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Quad) {
                Quad other = (Quad) o;
                return other.tokens[0].equals(tokens[0])
                        && other.tokens[1].equals(tokens[1])
                        && other.tokens[2].equals(tokens[2])
                        && other.tokens[3].equals(tokens[3]);
            }
            return false;
        }
    }

    /**
     * WORK IN PROGRESS DO NOT USE
     */
    public static class HalSerializer implements JsonSerializer<MegaHalBrain> {

        @Override
        public JsonElement serialize(MegaHalBrain src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            Gson g = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().enableComplexMapKeySerialization().create();
            obj.add("words", g.toJsonTree(src.wordToQuad, Map.class));
            obj.add("quads", g.toJsonTree(src.quads, Set.class));
            obj.add("next", g.toJsonTree(src.possibleNextWords, Map.class));
            obj.add("previous", g.toJsonTree(src.possiblePreviousWords, Map.class));
            return obj;
        }
    }

    /**
     * WORK IN PROGRESS DO NOT USE
     */
    public static class HalDeserializer implements JsonDeserializer<MegaHalBrain> {

        @Override
        public MegaHalBrain deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            MegaHalBrain hal = new MegaHalBrain();
            // TODO
            return hal;
        }
    }
}
