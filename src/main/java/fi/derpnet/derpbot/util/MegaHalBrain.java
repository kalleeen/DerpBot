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
    private static final int CHAIN_LENGTH = 3;

    private final Map<String, Set<Triple>> wordToChain = new HashMap<>();
    private final Map<Triple, Character> chains = new HashMap<>();
    private final Map<Triple, Set<String>> possibleNextWords = new HashMap();
    private final Map<Triple, Set<String>> possiblePreviousWords = new HashMap();
    private final Random rand = new Random();

    public String getStats() {
        int words = wordToChain.size();
        int chainCount = chains.size();
        int wordAssociations = wordToChain.values().stream().mapToInt(Set::size).sum();
        return String.format("%d words, %d higher order chains, %d word to chain associations", words, chainCount, wordAssociations);
    }

    public void add(String message) {
        List<String> sentences = Arrays.asList(message.trim().toLowerCase().split("(?<=[.!\\?])\\s+"));
        sentences.forEach((sentence) -> {
            List<String> words = new LinkedList<>();
            Arrays.asList(sentence.split("\\s+")).forEach(s -> {
                if (StringUtils.endsWithAny(s, ",", ":", ";")) {
                    words.add(s.substring(0, s.length() - 1));
                    words.add(s.substring(s.length() - 1));
                } else if (s.contains("-")) {
                    boolean b = true;
                    for (String ss : s.split("-")) {
                        if (b) {
                            b = false;
                        } else {
                            words.add("-");
                        }
                        words.add(ss);
                    }
                } else {
                    words.add(s);
                }
            });
            if (words.size() >= CHAIN_LENGTH) {
                for (int i = 0; i <= words.size() - CHAIN_LENGTH; i++) {
//                    Quad chain = new Quad((String) words.get(i), (String) words.get(i + 1), (String) words.get(i + 2), (String) words.get(i + 3));
                    Triple chain = new Triple((String) words.get(i), (String) words.get(i + 1), (String) words.get(i + 2));
//                    Dual chain = new Dual((String) words.get(i), (String) words.get(i + 1));
                    Character end = null;
                    if (i == 0) {
                        chain.canStart = true;
                    }
                    if (i == words.size() - CHAIN_LENGTH) {
                        chain.canEnd = true;
                        String lastWord = chain.tokens[chain.tokens.length - 1];
                        if (lastWord.endsWith(".") || lastWord.endsWith("!") || lastWord.endsWith("?")) {
                            chain.tokens[chain.tokens.length - 1] = lastWord.substring(0, lastWord.length() - 1);
                            end = lastWord.charAt(lastWord.length() - 1);
                        }
                    }
                    if (!chains.containsKey(chain)) {
                        chains.put(chain, end);
                    }

                    for (int n = 0; n < CHAIN_LENGTH; n++) {
                        String token = words.get(i + n);
                        if (token.endsWith(".") || token.endsWith("!") || token.endsWith("?")) {
                            token = token.substring(0, token.length() - 1);
                        }
                        if (!wordToChain.containsKey(token)) {
                            wordToChain.put(token, new HashSet());
                        }
                        wordToChain.get(token).add(chain);
                    }

                    if (i > 0) {
                        String token = words.get(i - 1);
                        if (token.endsWith(".") || token.endsWith("!") || token.endsWith("?")) {
                            token = token.substring(0, token.length() - 1);
                        }
                        if (!possiblePreviousWords.containsKey(chain)) {
                            possiblePreviousWords.put(chain, new HashSet());
                        }
                        possiblePreviousWords.get(chain).add(token);
                    }

                    if (i < words.size() - CHAIN_LENGTH) {
                        String token = words.get(i + CHAIN_LENGTH);
                        if (token.endsWith(".") || token.endsWith("!") || token.endsWith("?")) {
                            token = token.substring(0, token.length() - 1);
                        }
                        if (!possibleNextWords.containsKey(chain)) {
                            possibleNextWords.put(chain, new HashSet(1));
                        }
                        possibleNextWords.get(chain).add(token);
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

        List<Triple> startChains;
        if (wordToChain.containsKey(word)) {
            startChains = new ArrayList<>(wordToChain.get(word));
        } else {
            startChains = new ArrayList<>(chains.keySet());
            if (word != null) {
                LOG.info("Didn't find chains containing word " + word);
            }
        }

        if (startChains.isEmpty()) {
            LOG.warn("No possible words found for creating a response");
            return "";
        }

        Triple middleChain = startChains.get(rand.nextInt(startChains.size()));
        Triple chain = middleChain;

        for (int i = 0; i < CHAIN_LENGTH - 1; i++) {
            parts.add(chain.tokens[i]);
        }
        Character end = chains.get(chain);
        String token = chain.tokens[CHAIN_LENGTH - 1];
        if (end != null) {
            token += end;
        }
        parts.add(token);
        while (chain.canEnd == false) {
            Set<String> nextSet = possibleNextWords.get(chain);
            if (nextSet == null) {
                break;
            }
            String[] nextTokens = nextSet.toArray(new String[nextSet.size()]);
            String nextToken = nextTokens[rand.nextInt(nextTokens.length)];
//            chain = new Quad(chain.tokens[1], chain.tokens[2], chain.tokens[3], nextToken);
            chain = new Triple(chain.tokens[1], chain.tokens[2], nextToken);
//            chain = new Dual(chain.tokens[1], nextToken);
            if (!chains.containsKey(chain)) {
                break;
            }
            end = chains.get(chain);
            if (end != null) {
                nextToken += end;
            }
            parts.add(nextToken);
        }

        chain = middleChain;
        while (chain.canStart == false) {
            Set<String> previousSet = possiblePreviousWords.get(chain);
            if (previousSet == null) {
                break;
            }
            String[] previousTokens = previousSet.toArray(new String[previousSet.size()]);
            String previousToken = previousTokens[rand.nextInt(previousTokens.length)];
//            chain = new Quad(previousToken, chain.tokens[0], chain.tokens[1], chain.tokens[2]);
            chain = new Triple(previousToken, chain.tokens[0], chain.tokens[1]);
//            chain = new Dual(previousToken, chain.tokens[0]);
            if (!chains.containsKey(chain)) {
                break;
            }
            parts.addFirst(previousToken);
        }
        String message = parts.stream().collect(Collectors.joining(" "));
        message = message.replaceAll(" ,", ",");
        message = message.replaceAll(" :", ":");
        message = message.replaceAll(" ;", ";");
        message = message.replaceAll(" - ", "-");
        return StringUtils.capitalize(message);
    }

    private static class Dual {

        @Expose
        String[] tokens;
        @Expose
        boolean canStart = false;
        @Expose
        boolean canEnd = false;

        public Dual(String s1, String s2) {
            tokens = new String[]{s1, s2};
        }

        @Override
        public int hashCode() {
            return tokens[0].hashCode()
                    + tokens[1].hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Dual) {
                Dual other = (Dual) o;
                return other.tokens[0].equals(tokens[0])
                        && other.tokens[1].equals(tokens[1]);
            }
            return false;
        }
    }

    private static class Triple {

        @Expose
        String[] tokens;
        @Expose
        boolean canStart = false;
        @Expose
        boolean canEnd = false;

        public Triple(String s1, String s2, String s3) {
            tokens = new String[]{s1, s2, s3};
        }

        @Override
        public int hashCode() {
            return tokens[0].hashCode()
                    + tokens[1].hashCode()
                    + tokens[2].hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Triple) {
                Triple other = (Triple) o;
                return other.tokens[0].equals(tokens[0])
                        && other.tokens[1].equals(tokens[1])
                        && other.tokens[2].equals(tokens[2]);
            }
            return false;
        }
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
            obj.add("words", g.toJsonTree(src.wordToChain, Map.class));
            obj.add("chains", g.toJsonTree(src.chains, Set.class));
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
