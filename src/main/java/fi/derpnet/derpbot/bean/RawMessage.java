package fi.derpnet.derpbot.bean;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Describes a raw message from the server. See section 2.3 Messages from
 * https://tools.ietf.org/html/rfc2812
 */
public class RawMessage implements Message {

    /**
     * The prefix is used by servers to indicate the true origin of the message.
     * If the prefix is missing from the message, it is assumed to have
     * originated from the connection from which it was received from. Clients
     * SHOULD NOT use a prefix when sending a message; if they use one, the only
     * valid prefix is the registered nickname associated with the client.
     */
    public final String prefix;
    public final String command;
    public final List<String> parameters;
    private final String rawLine;

    /**
     * Constructs a new raw message with the provided values
     *
     * @param prefix (Optional) prefix without leading semicolon. Clients SHOULD
     * NOT use a prefix when sending a message; if they use one, the only valid
     * prefix is the registered nickname associated with the client.
     * @param command command
     * @param parameters parameters (up to 15). Note: the leading colon must
     * be present in the last parameter if it requires one
     */
    public RawMessage(String prefix, String command, List<String> parameters) {
        this.prefix = prefix;
        this.command = command;
        this.parameters = Collections.unmodifiableList(parameters);
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(':').append(prefix).append(' ');
        }
        sb.append(command);
        parameters.forEach(s -> sb.append(' ').append(s));
        rawLine = sb.toString();
    }

    /**
     * Constructs a new raw message with the provided values
     *
     * @param prefix (Optional) prefix without leading semicolon. Clients SHOULD
     * NOT use a prefix when sending a message; if they use one, the only valid
     * prefix is the registered nickname associated with the client.
     * @param command command
     * @param parameters parameters (up to 15). Note: the leading colon must
     * be present in the last parameter if it requires one
     */
    public RawMessage(String prefix, String command, String... parameters) {
        this(prefix, command, Arrays.asList(parameters));
    }

    /**
     * Constructs a new raw message from a String containing the raw message
     *
     * @param rawLine The message as specified in RFC 2812
     */
    public RawMessage(String rawLine) {
        this.rawLine = rawLine;
        String string;
        if (rawLine.charAt(0) == ':') { // has prefix
            prefix = rawLine.substring(1, rawLine.indexOf(' '));
            string = rawLine.substring(rawLine.indexOf(' ') + 1); // +1 because of the space
        } else { // no prefix
            prefix = null;
            string = rawLine;
        }
        command = string.substring(0, string.indexOf(' '));
        string = string.substring(string.indexOf(' ') + 1); // +1 because of the space

        List<String> paramList = new LinkedList<>();
        // The last parameter may contain spaces or other "special characters" so 
        // it's prefixed by a colon which is technically not part of the parameter itself
        if (string.charAt(0) == ':') {
            // if the remaining starts with a colon, then it's the only parameter
            paramList.add(string.substring(1));
        } else if (string.contains(" :")) {
            String[] colonSplit = string.split(" :", 2);
            String[] parameterSplit = colonSplit[0].split(" ");
            paramList.addAll(Arrays.asList(parameterSplit));
            paramList.add(colonSplit[1]);
        } else {
            paramList.addAll(Arrays.asList(string.split(" ")));
        }
        parameters = Collections.unmodifiableList(paramList);
    }

    @Override
    public String toString() {
        return rawLine;
    }
}
