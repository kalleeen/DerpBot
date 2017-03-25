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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class MegaHalBrain {

    private static final Logger LOG = Logger.getLogger(MegaHalBrain.class);

    private final Map<String, Set<Quad>> wordToQuad = new HashMap<>();
    private final Map<Quad, Character> quads = new HashMap<>();
    private final Map<Quad, Set<String>> possibleNextWords = new HashMap();
    private final Map<Quad, Set<String>> possiblePreviousWords = new HashMap();
    private final Random rand = new Random();

    public String getStats() {
        int words = wordToQuad.size();
        int chains = quads.size();
        int wordAssociations = wordToQuad.values().stream().mapToInt(Set::size).sum();
        return String.format("%d words, %d higher order chains, %d word to chain associations", words, chains, wordAssociations);
    }

    public void add(String message) {
        List<String> sentences = Arrays.asList(message.trim().toLowerCase().split("(?<=[.!\\?])\\s+"));
        sentences.forEach((sentence) -> {
            List<String> words = Arrays.asList(sentence.split("\\s+"));
            if (words.size() >= 4) {
                for (int i = 0; i < words.size() - 3; i++) {
                    Quad quad = new Quad((String) words.get(i), (String) words.get(i + 1), (String) words.get(i + 2), (String) words.get(i + 3));
                    Character end = null;
                    if (i == 0) {
                        quad.canStart = true;
                    }
                    if (i == words.size() - 4) {
                        quad.canEnd = true;
                        String lastWord = quad.tokens[quad.tokens.length - 1];
                        if (lastWord.endsWith(".") || lastWord.endsWith("!") || lastWord.endsWith("?")) {
                            quad.tokens[quad.tokens.length - 1] = lastWord.substring(0, lastWord.length() - 1);
                            end = lastWord.charAt(lastWord.length() - 1);
                        }
                    }
                    if (!quads.containsKey(quad)) {
                        quads.put(quad, end);
                    }

                    for (int n = 0; n < 4; n++) {
                        String token = words.get(i + n);
                        if (token.endsWith(".") || token.endsWith("!") || token.endsWith("?")) {
                            token = token.substring(0, token.length() - 1);
                        }
                        if (!wordToQuad.containsKey(token)) {
                            wordToQuad.put(token, new HashSet(1));
                        }
                        wordToQuad.get(token).add(quad);
                    }

                    if (i > 0) {
                        String token = words.get(i - 1);
                        if (token.endsWith(".") || token.endsWith("!") || token.endsWith("?")) {
                            token = token.substring(0, token.length() - 1);
                        }
                        if (!possiblePreviousWords.containsKey(quad)) {
                            possiblePreviousWords.put(quad, new HashSet(1));
                        }
                        possiblePreviousWords.get(quad).add(token);
                    }

                    if (i < words.size() - 4) {
                        String token = words.get(i + 4);
                        if (token.endsWith(".") || token.endsWith("!") || token.endsWith("?")) {
                            token = token.substring(0, token.length() - 1);
                        }
                        if (!possibleNextWords.containsKey(quad)) {
                            possibleNextWords.put(quad, new HashSet(1));
                        }
                        possibleNextWords.get(quad).add(token);
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
            startQuads = new ArrayList<>(quads.keySet());
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

        for (int i = 0; i < 3; i++) {
            parts.add(quad.tokens[i]);
        }
        Character end = quads.get(quad);
        String token = quad.tokens[3];
        if (end != null) {
            token += end;
        }
        parts.add(token);
        System.out.println("HERPA" + Arrays.toString(quad.tokens) + "#" + end);
        while (quad.canEnd == false) {
            Set<String> nextSet = possibleNextWords.get(quad);
            if (nextSet == null) {
                break;
            }
            String[] nextTokens = nextSet.toArray(new String[0]);
            String nextToken = nextTokens[rand.nextInt(nextTokens.length)];
            quad = new Quad(quad.tokens[1], quad.tokens[2], quad.tokens[3], nextToken);
            System.out.println("DERPA" + Arrays.toString(quad.tokens) + "#" + end);
            if (!quads.containsKey(quad)) {
                break;
            }
            end = quads.get(quad);
            if (end != null) {
                nextToken += end;
            }
            System.out.println(Arrays.toString(quad.tokens) + "#" + end);
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
            quad = new Quad(previousToken, quad.tokens[0], quad.tokens[1], quad.tokens[2]);
            if (!quads.containsKey(quad)) {
                break;
            }
            parts.addFirst(previousToken);
        }

        return StringUtils.capitalize(parts.stream().collect(Collectors.joining(" ")));
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
