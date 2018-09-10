package com.compomics.psi_io.ionbot;

import com.compomics.psi_io.util.ResourceUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class holds all headers for the different Ionbot identification files.
 * <p>
 * Created by Niels Hulstaert on 13/09/16.
 */
public abstract class IonbotHeaders<T extends Enum<T>> {

    /**
     * The header enum class.
     */
    protected final Class<T> enumType;
    /**
     * The headers map (key: header enum; value: the {@link IonbotHeaders} instance).
     */
    protected final EnumMap<T, IonbotHeader> headersMap;
    /**
     * The path of the JSON file with the header information.
     */
    protected final String jsonRelativePath;

    /**
     * Constructor.
     *
     * @param enumType         the enum class for generics purposes
     * @param headersMap       the headers map
     * @param jsonRelativePath the path of the JSON file with the header values
     * @param toLowerCase      convert the parsed values to lowercase
     * @throws IOException in case of an Input/Output related problem
     */
    public IonbotHeaders(Class<T> enumType, EnumMap<T, IonbotHeader> headersMap, String jsonRelativePath, boolean toLowerCase) throws IOException {
        this.enumType = enumType;
        this.headersMap = headersMap;
        this.jsonRelativePath = jsonRelativePath;
        parse(toLowerCase);
    }

    /**
     * Constructor. The header values are by default parsed in lower case.
     *
     * @param enumType         the enum class for generics purposes
     * @param headersMap       the headers map
     * @param jsonRelativePath the path of the JSON file with the header values
     */
    public IonbotHeaders(Class<T> enumType, EnumMap<T, IonbotHeader> headersMap, String jsonRelativePath) throws IOException {
        this(enumType, headersMap, jsonRelativePath, true);
    }

    public Class<T> getEnumType() {
        return enumType;
    }

    public EnumMap<T, IonbotHeader> getHeadersMap() {
        return headersMap;
    }

    /**
     * Get all the headers for a given Ionbot file.
     *
     * @return the list of mandatory headers
     */
    public List<IonbotHeader> getHeaders() {
        return headersMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Get all the mandatory headers for a given Ionbot file.
     *
     * @return the list of mandatory headers
     */
    public List<IonbotHeader> getMandatoryHeaders() {
        return headersMap.values().stream().filter(IonbotHeader::isMandatory).collect(Collectors.toList());
    }

    /**
     * Get the parsed header value for the given header.
     *
     * @param headerEnum the header enum
     * @return the header value
     */
    public String get(T headerEnum) {
        return headersMap.get(headerEnum).getValue();
    }

    /**
     * Get the {@link IonbotHeader} instance associated with the given enum.
     *
     * @param headerEnum the enum value
     * @return the {@link IonbotHeader} instance
     */
    public IonbotHeader getHeader(T headerEnum) {
        return headersMap.get(headerEnum);
    }

    /**
     * Parse the JSON file and populate the {@link EnumMap} instance.
     *
     * @param toLowerCase convert the parsed values to lower case
     * @throws IOException              in case of an Input/Output related problem while parsing the JSON file
     * @throws IllegalArgumentException in case a header entry could not be matched with an Enum value
     */
    protected void parse(boolean toLowerCase) throws IOException {
        Resource jsonHeadersResource = ResourceUtils.getResourceByRelativePath(jsonRelativePath);
        ObjectMapper objectMapper = new ObjectMapper();
        //read the JSON file
        JsonNode headersNode = objectMapper.readTree(jsonHeadersResource.getInputStream());

        //iterate over the header entries
        Iterator<Map.Entry<String, JsonNode>> headersIterator = headersNode.fields();
        while (headersIterator.hasNext()) {
            Map.Entry<String, JsonNode> headerEntry = headersIterator.next();

            String headerName = headerEntry.getKey();
            T headerEnum = Enum.valueOf(enumType, headerName);
            boolean mandatory = Boolean.valueOf(headerEntry.getValue().get("mandatory").asText());

            //iterate over the values array
            JsonNode valuesNode = headerEntry.getValue().get("values");
            List<String> values = new ArrayList<>();
            for (JsonNode valueNode : valuesNode) {
                if (toLowerCase) {
                    values.add(valueNode.asText().toLowerCase());
                } else {
                    values.add(valueNode.asText());
                }
            }

            IonbotHeader ionbotHeader = new IonbotHeader(headerName, mandatory, values);
            headersMap.put(headerEnum, ionbotHeader);
        }
    }

}
