/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramltester.util;

import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

/**
 *
 */
public class MediaType {
    public static final MediaType JSON = valueOf("application/json");

    private static final String CHARSET = "charset";
    private static final String WILDCARD_TYPE = "*";
    private static final Map<String, MediaType> KNOWN_SUFFICES = new HashMap<String, MediaType>() {{
        put("json", JSON);
    }};

    private String type;
    private String subtype;
    private Map<String, String> parameters;

    public MediaType(String type, String subtype, Map<String, String> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
    }

    public static MediaType valueOf(String mimeType) {
        if (mimeType == null || mimeType.length() == 0) {
            throw new InvalidMediaTypeException(mimeType, "'mimeType' must not be empty");
        }
        String[] parts = tokenizeToStringArray(mimeType, ";");

        String fullType = parts[0].trim();
        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (WILDCARD_TYPE.equals(fullType)) {
            fullType = "*/*";
        }
        int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new InvalidMediaTypeException(mimeType, "does not contain '/'");
        }
        if (subIndex == fullType.length() - 1) {
            throw new InvalidMediaTypeException(mimeType, "does not contain subtype after '/'");
        }
        String type = fullType.substring(0, subIndex);
        String subtype = fullType.substring(subIndex + 1, fullType.length());
        if (WILDCARD_TYPE.equals(type) && !WILDCARD_TYPE.equals(subtype)) {
            throw new InvalidMediaTypeException(mimeType, "wildcard type is legal only in '*/*' (all mime types)");
        }

        Map<String, String> parameters = new LinkedHashMap<>(parts.length);
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                String parameter = parts[i];
                int eqIndex = parameter.indexOf('=');
                if (eqIndex != -1) {
                    String attribute = parameter.substring(0, eqIndex);
                    String value = parameter.substring(eqIndex + 1, parameter.length());
                    parameters.put(attribute, value);
                }
            }
        }

        try {
            return new MediaType(type, subtype, parameters);
        } catch (UnsupportedCharsetException ex) {
            throw new InvalidMediaTypeException(mimeType, "unsupported charset '" + ex.getCharsetName() + "'");
        } catch (IllegalArgumentException ex) {
            throw new InvalidMediaTypeException(mimeType, ex.getMessage());
        }
    }

    public boolean isWildcardType() {
        return WILDCARD_TYPE.equals(getType());
    }

    public boolean isWildcardSubtype() {
        return WILDCARD_TYPE.equals(getSubtype()) || getSubtype().startsWith("*+");
    }

    public boolean isCompatibleWith(MediaType other) {
        if (other == null) {
            return false;
        }
        if (isWildcardType() || other.isWildcardType()) {
            return true;
        }
        final MediaType thisKnown = applyKnownSuffices();
        final MediaType otherKnown = other.applyKnownSuffices();
        if ((thisKnown != this || otherKnown != other) && thisKnown.isCompatibleWith(otherKnown)) {
            return true;
        }
        if (!getType().equals(other.getType())) {
            return false;
        }
        if (getSubtype().equals(other.getSubtype())) {
            return true;
        }
        // wildcard with suffix? e.g. application/*+xml
        if (this.isWildcardSubtype() || other.isWildcardSubtype()) {
            final String[] thisSubtypeParts = findSuffix();
            final String[] otherSubtypeParts = other.findSuffix();
            if (thisSubtypeParts[1] == null && otherSubtypeParts[1] == null) {
                return true;
            }
            if (thisSubtypeParts[1] != null && otherSubtypeParts[1] != null) {
                if (thisSubtypeParts[1].equals(otherSubtypeParts[1]) &&
                        (WILDCARD_TYPE.equals(thisSubtypeParts[0]) || WILDCARD_TYPE.equals(otherSubtypeParts[0]))) {
                    return true;
                }
            }
        }
        return false;
    }

    private MediaType applyKnownSuffices() {
        final MediaType known = KNOWN_SUFFICES.get(findSuffix()[1]);
        return known == null ? this : known;
    }

    private String[] findSuffix() {
        int pos = getSubtype().indexOf('+');
        return pos == -1
                ? new String[]{getSubtype(), null}
                : new String[]{getSubtype().substring(0, pos), getSubtype().substring(pos + 1)};
    }

    public String getType() {
        return this.type;
    }

    public String getSubtype() {
        return subtype;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getCharset(String defaultCharset) {
        final String charset = parameters.get(CHARSET);
        return charset != null ? charset : defaultCharset;
    }

    private static String[] tokenizeToStringArray(String str, String delimiters) {
        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            token = token.trim();
            if (token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    private static String[] toStringArray(Collection<String> collection) {
        return collection == null ? null : collection.toArray(new String[collection.size()]);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MediaType)) {
            return false;
        }
        MediaType otherType = (MediaType) other;
        return (this.type.equalsIgnoreCase(otherType.type) &&
                this.subtype.equalsIgnoreCase(otherType.subtype) &&
                this.parameters.equals(otherType.parameters));
    }

    @Override
    public int hashCode() {
        int result = this.type.hashCode();
        result = 31 * result + this.subtype.hashCode();
        result = 31 * result + this.parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String res = type + "/" + subtype;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            res += ";" + entry.getKey() + "=" + entry.getValue();
        }
        return res;
    }
}
