package com.coap.core.coap;

/**
 * @ClassName Message
 * @Description TODO
 * @Author wuxiaojian
 * @Date 2019/6/15 15:25
 * @Version 1.0
 **/

import com.coap.elements.EndpointContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static com.coap.core.coap.CoAP.*;

/**
 * The class Message models the base class of all CoAP messages. CoAP messages
 * are of type {@link Request}, {@link Response} or {@link EmptyMessage}. Each
 * message has a {@link Type}, a message identifier (MID), a token (0-8 bytes),
 * a collection of options ({@link OptionSet}) and a payload.
 * <p>
 * Furthermore, a message can be acknowledged, rejected, canceled, or time out;
 * the meaning of which is defined more specifically in the subclasses. Clients
 * can register {@link MessageObserver}s with a message which will be notified
 * when any of the events listed above occur.
 * <p>
 * Note: The {@link #messageObservers} and {@link #options} properties are
 * initialized lazily. This saves a few bytes in case the properties are not in
 * use. For instance an empty message should not have any options and most
 * messages will not have any observers registered.
 *
 * @see Request
 * @see Response
 * @see EmptyMessage
 */
public abstract class Message {

    protected final static Logger LOGGER = LoggerFactory.getLogger(Message.class.getCanonicalName());

    /** The Constant NONE in case no MID has been set. */
    public static final int NONE = -1;

    /**
     * The largest message ID allowed by CoAP.
     * <p>
     * The value of this constant is 2^16 - 1.
     */
    public static final int MAX_MID = (1 << 16) - 1;

    /** The type. One of {CON, NON, ACK or RST}. */
    private CoAP.Type type;

    /** The 16-bit Message Identification. */
    private volatile int mid = NONE; // Message ID

    /**
     * The token, a 0-8 byte array.
     * <p>
     * This field is initialized to {@code null} so that client code can
     * determine whether the message already has a token assigned or not. An
     * empty array would not work here because it is already a valid token
     * according to the CoAP spec.
     */
    private volatile Token token = null;

    /** The set of options of this message. */
    private OptionSet options;

    /** The payload of this message. */
    private byte[] payload;

    /** Marks this message to have payload even if this is not intended */
    private boolean unintendedPayload;

    /**
     * Destination endpoint context. Used for outgoing messages.
     */
    private volatile EndpointContext destinationContext;

    /**
     * Source endpoint context. Used for incoming messages.
     */
    private volatile EndpointContext sourceContext;

    /** Indicates if the message has sent. */
    private volatile boolean sent;

    /** Indicates if the message has been acknowledged(已确认的). */
    private volatile boolean acknowledged;

    /** Indicates if the message has been rejected. */
    private volatile boolean rejected;

    /** Indicates if the message has been canceled. */
    private volatile boolean canceled;

    /** Indicates if the message has timed out */
    private volatile boolean timedOut; // Important for CONs

    /** Indicates if the message is a duplicate. */
    private volatile boolean duplicate;

    /** Indicates if sending the message caused an error. */
    private volatile Throwable sendError;

    /** The serialized message as byte array. */
    private volatile byte[] bytes;

    /**
     * A list of all {@link ObserveManager} that should be notified when an
     * event for this message occurs. By default, this field is null
     * (lazy-initialization). If a handler is added, the list will be created
     * and from then on must never again become null.
     */
    private final AtomicReference<List<MessageObserver>> messageObservers = new AtomicReference<List<MessageObserver>>();

    /**
     * A unmodifiable(不可修改的) facade(外观) for the list of all {@link ObserveManager}.
     *
     * @see #messageObservers
     * @see #getMessageObservers()
     */
    private volatile List<MessageObserver> unmodifiableMessageObserversFacade = null;

    /**
     * The timestamp when this message has been received, sent, or 0, if neither
     * has happened yet. The {@link Matcher} sets the timestamp.
     */
    private volatile long timestamp;

    /**
     * Creates a new message with no specified message type.
     */
    protected Message() {
    }

    /**
     * Creates a new message of a given type.
     * <p>
     * The type must be one of CON, NON, ACK or RST.
     *
     * @param type the type
     */
    public Message(Type type) {
        this.type = type;
    }

    /**
     * Gets the message type ({@link Type#CON}, {@link Type#NON},
     * {@link Type#ACK} or {@link Type#RST}). If no type has been defined, the
     * type is null.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the CoAP message type.
     *
     * Provides a fluent API to chain setters.
     *
     * @param type the new type
     * @return this Message
     */
    public Message setType(Type type) {
        this.type = type;
        return this;
    }

    /**
     * Checks if this message is confirmable.
     *
     * @return true, if is confirmable
     */
    public boolean isConfirmable() {
        return getType() == Type.CON;
    }

    /**
     * Chooses between confirmable and non-confirmable message.
     *
     * Pass true for CON, false for NON. Provides a fluent API to chain setters.
     *
     * @param con true for CON, false for NON
     * @return this Message
     */
    public Message setConfirmable(boolean con) {
        setType(con ? Type.CON : Type.NON);
        return this;
    }

    /**
     * Gets the raw integer value of this message's <em>code</em>.
     *
     * @return the code value.
     */
    public abstract int getRawCode();

    /**
     * Checks, if this message is intended to have payload.
     *
     * To be overwritten by subclass to provide a specific check.
     *
     * @return {@code true}, if message is intended to have payload
     */
    public boolean isIntendedPayload() {
        return true;
    }

    /**
     * Set marker for unintended payload.
     *
     * Enables to use payload with messages, which are not intended to have
     * payload.
     *
     * @throws IllegalStateException if message is intended to have payload
     */
    public void setUnintendedPayload() {
        if (isIntendedPayload()) {
            throw new IllegalStateException("Message is already intended to have payload!");
        }
        unintendedPayload = true;
    }

    /**
     * Checks, if message is marked to have unintended payload.
     *
     * @return {@code true} if message is marked to have unintended payload
     */
    public boolean isUnintendedPayload() {
        return unintendedPayload;
    }

    /**
     * Gets the 16-bit message identification.
     *
     * @return the mid
     */
    public int getMID() {
        return mid;
    }

    /**
     * Checks whether this message has a valid ID.
     *
     * @return {@code true} if this message's ID is a 16 bit unsigned integer.
     */
    public boolean hasMID() {
        return mid != NONE;
    }

    /**
     * Sets the 16-bit message identification.
     *
     * Reset {@link #bytes} to force new serialization.
     *
     * Provides a fluent API to chain setters.
     *
     * @param mid the new mid
     * @return this Message
     */
    public Message setMID(int mid) {
        // NONE is allowed as a temporary placeholder
        if (mid > MAX_MID || mid < NONE) {
            throw new IllegalArgumentException("The MID must be an unsigned 16-bit number but was " + mid);
        }
        this.mid = mid;
        bytes = null;
        return this;
    }

    /**
     * Clears this message's MID.
     */
    public void removeMID() {
        setMID(NONE);
    }

    /**
     * Checks whether this message has a non-zero length token.
     *
     * @return {@code true} if this message's token is either {@code null} or of
     *         length 0.
     */
    public boolean hasEmptyToken() {
        return token == null || token.isEmpty();
    }

    /**
     * Gets this message's token.
     *
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
     * Gets this message's 0- -8 byte token.
     *
     * @return the token
     */
    public byte[] getTokenBytes() {
        return token == null ? null : token.getBytes();
    }

    /**
     * Gets the 0--8 byte token as string representation.
     *
     * @return the token as string
     */
    public String getTokenString() {
        return token == null ? "null" : token.getAsString();
    }

    /**
     * Sets the token bytes, which can be 0--8 bytes.
     *
     * Note:
     * To support address changes, the provided tokens must be unique for
     * all clients and not only for the client the message is sent to. This
     * narrows the definition of RFC 7252, 5.3.1, from "client-local" to
     * "system-local".
     *
     * Reset {@link #bytes} to force new serialization.
     *
     * Provides a fluent API to chain setters.
     *
     * @param tokenBytes the new token bytes
     * @return this Message
     * @see #setToken(Token)
     */
    public Message setToken(byte[] tokenBytes) {
        Token token = null;
        if (tokenBytes != null) {
            token = new Token(tokenBytes);
        }
        return setToken(token);
    }

    /**
     * Sets the token.
     *
     * Note:
     * To support address changes, the provided tokens must be unique for
     * all clients and not only for the client the message is sent to. This
     * narrows the definition of RFC 7252, 5.3.1, from "client-local" to
     * "system-local".
     *
     * Reset {@link #bytes} to force new serialization.
     *
     * Provides a fluent API to chain setters.
     *
     * @param token the new token
     * @return this Message
     */
    public Message setToken(Token token) {
        this.token = token;
        bytes = null;
        return this;
    }

    /**
     * Gets the set of options. If no set has been defined yet, it creates a new
     * one. EmptyMessages should not have any options.
     *
     * @return the options
     */
    public OptionSet getOptions() {
        if (options == null) {
            options = new OptionSet();
        }
        return options;
    }

    /**
     * Sets the set of options.
     *
     * This function makes a defensive copy of the specified set of options.
     * Provides a fluent API to chain setters.
     *
     * @param options the new options
     * @return this Message
     */
    public Message setOptions(OptionSet options) {
        this.options = new OptionSet(options);
        return this;
    }

    /**
     * Gets the size (amount of bytes) of the payload. Be aware that this might
     * differ from the payload string length due to the UTF-8 encoding.
     *
     * @return the payload size
     */
    public int getPayloadSize() {
        return payload == null ? 0 : payload.length;
    }

    /**
     * Gets the raw payload.
     *
     * @return the payload
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Gets the payload in the form of a string. Returns an empty string if no
     * payload is defined.
     *
     * @return the payload as string
     */
    public String getPayloadString() {
        if (payload == null) {
            return "";
        }
        return new String(payload, CoAP.UTF8_CHARSET);
    }

    protected String getPayloadTracingString() {

        if (null == payload || 0 == payload.length) {
            return "no payload";
        }
        boolean text = true;
        for (byte b : payload) {
            if (' ' > b) {
                switch (b) {
                    case '\t':
                    case '\n':
                    case '\r':
                        continue;
                }
                text = false;
                break;
            }
        }
        if (text) {
            CharsetDecoder decoder = CoAP.UTF8_CHARSET.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer in = ByteBuffer.wrap(payload);
            CharBuffer out = CharBuffer.allocate(24);
            CoderResult result = decoder.decode(in, out, true);
            decoder.flush(out);
            ((Buffer)out).flip();
            if (CoderResult.OVERFLOW == result) {
                return "\"" + out + "\".. " + payload.length + " bytes";
            } else if (!result.isError()) {
                return "\"" + out + "\"";
            }
        }
        return Utils.toHexText(payload, 256);
    }

    /**
     * Sets the UTF-8 bytes from the specified string as payload.
     *
     * Provides a fluent API to chain setters.
     *
     * @param payload the payload as string. {@code null} or a empty string are
     *            not considered to be payload and therefore not cause a
     *            IllegalArgumentException, if this message must not have
     *            payload.
     * @return this Message
     * @throws IllegalArgumentException if this message must not have payload
     * @see #isIntendedPayload()
     * @see #isUnintendedPayload()
     * @see #setUnintendedPayload()
     */
    public Message setPayload(String payload) {
        if (payload == null) {
            this.payload = null;
        } else {
            setPayload(payload.getBytes(CoAP.UTF8_CHARSET));
        }
        return this;
    }

    /**
     * Sets the payload.
     *
     * Provides a fluent API to chain setters.
     *
     * @param payload the new payload. {@code null} or a empty array are not
     *            considered to be payload and therefore not cause a
     *            IllegalArgumentException, if this message must not have
     *            payload.
     * @return this Message
     * @throws IllegalArgumentException if this message must not have payload
     * @see #isIntendedPayload()
     * @see #isUnintendedPayload()
     * @see #setUnintendedPayload()
     */
    public Message setPayload(byte[] payload) {
        if (payload != null && payload.length > 0 && !isIntendedPayload() && !isUnintendedPayload()) {
            throw new IllegalArgumentException("Message must not have payload!");
        }
        this.payload = payload;
        return this;
    }








}
