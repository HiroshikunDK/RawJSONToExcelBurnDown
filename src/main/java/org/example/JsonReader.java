package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonReader {

    // Function to read multiple JSON strings and return a list of JsonNode objects
    // Function to parse each JSON string into a JsonNode object
    public static List<JsonNode> parseJsonArrayFromStrings(List<String> jsonStrings) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<JsonNode> jsonNodeList = new ArrayList<>();

        try {
            // Iterate through each JSON string and parse it as a JsonNode
            for (String jsonString : jsonStrings) {
                JsonNode jsonNode = objectMapper.readTree(jsonString);
                jsonNodeList.add(jsonNode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonNodeList;
    }

    // Function to write the extracted data to a CSV file
    public static void saveJsonToCsv(List<JsonNode> jsonNodeList, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Writing the CSV header (the column names)
            String[] header = {"CardName", "TimeSpent", "StoryPoints", "Sprint"};
            writer.writeNext(header);

            // Writing the CSV rows
            for (JsonNode node : jsonNodeList) {
                String cardName = node.has("CardName") ? node.get("CardName").asText() : "";
                String timeSpent = node.has("TimeSpent") ? node.get("TimeSpent").asText() : "";
                String storyPoints = node.has("StoryPoints") ? node.get("StoryPoints").asText() : "";
                String sprint = node.has("Sprint") ? node.get("Sprint").asText() : "";

                // Create a row for the CSV file
                String[] row = {cardName, timeSpent, storyPoints, sprint};
                writer.writeNext(row);
            }

            System.out.println("CSV file saved at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving CSV file.");
        }
    }

    public static void saveJsonArrayToFile(ArrayList<String> jsonArray, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convert ArrayList to a JSON array
            objectMapper.writeValue(new File(filePath), jsonArray);
            System.out.println("JSON file saved at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to save JSON file.");
        }
    }

    public static String[] extractDescFromJson(String jsonFilePath) {
        List<String> descriptions = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

            // Recursively search for the "desc" field inside "cards"
            findDescInCards(rootNode, descriptions);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return descriptions.toArray(new String[0]);
    }

    public static String processLineToJson(String line) {
        Map<String, String> extractedData = new HashMap<>();

        // Define regular expression patterns for each key (capturing the key and value inside "!")
        Pattern pattern = Pattern.compile("!(\\w+):\\s*([^!]+)!");

        // Matcher to find all the occurrences of the pattern in the given line
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String key = matcher.group(1);  // Extract key (e.g., Sprint, StoryPoints, etc.)
            String value = matcher.group(2); // Extract value (e.g., 5, 2, etc.)
            extractedData.put(key, value);
        }

        // Convert the extracted data into JSON format
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(extractedData); // Convert map to JSON string
        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Return empty JSON object in case of error
        }
    }

    // Recursively search for "cards" and extract "desc"
    private static void findDescInCards(JsonNode node, List<String> descriptions) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldNode = node.get(fieldName);

                if (fieldName.equals("cards") && fieldNode.isArray()) {
                    // Process each card
                    for (JsonNode card : fieldNode) {
                        JsonNode descNode = card.get("desc");
                        JsonNode nameNode = card.get("name");
                        if (descNode != null && descNode.isTextual()) {
                            descriptions.add(descNode.asText() + " !CardName: " + nameNode.asText() + "! " );
                        }
                    }
                } else {
                    // Recursively check deeper nodes
                    findDescInCards(fieldNode, descriptions);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                findDescInCards(arrayElement, descriptions);
            }
        }
    }

    public static void main(String[] args) {
        String jsonFilePath = "C:\\Users\\thoma\\Downloads\\yYdzS9Up - your-green-car.json";
        String[] descArray = extractDescFromJson(jsonFilePath);
        ArrayList<String> jsonArray =new ArrayList<>();

        // Print out the result
        for (String desc : descArray) {
            jsonArray.add(processLineToJson(desc));
        }
        System.out.println(jsonArray);


        // Parse the JSON strings into a list of JsonNode objects
        List<JsonNode> parsedJson = parseJsonArrayFromStrings(jsonArray);

        String filePath = "C:\\Users\\thoma\\Downloads\\TrelloOutput.csv";

        // Save the parsed data as CSV
        saveJsonToCsv(parsedJson, filePath);

    }
}