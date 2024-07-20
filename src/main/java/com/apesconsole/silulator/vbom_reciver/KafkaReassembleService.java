package com.apesconsole.silulator.vbom_reciver;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaReassembleService {
	
	@Autowired
	private JsonFileWriterService jsonFileWriterService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<Integer, String>> partialMessages = new ConcurrentHashMap<>();

    @KafkaListener(topics = "output_topic", groupId = "test_group_id")
    public void consumeAndReassemble(String message) {
        try {
            ChunkMessage chunk = objectMapper.readValue(message, ChunkMessage.class);
            partialMessages.putIfAbsent(chunk.messageId, new ConcurrentHashMap<>());
            partialMessages.get(chunk.messageId).put(chunk.chunkIndex, chunk.data);

            if (partialMessages.get(chunk.messageId).size() == chunk.totalChunks) {
                // Sort the keys (chunk indices) and join the corresponding values (chunks)
                String completeMessage = partialMessages.get(chunk.messageId).entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(Map.Entry::getValue)
                        .collect(Collectors.joining());

                Object originalData = objectMapper.readValue(completeMessage, Object.class);
                try {
                    jsonFileWriterService.writeJsonToFile(originalData, "large.json");
                    log.info("Message Written: " + Calendar.getInstance().getTime());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                partialMessages.remove(chunk.messageId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
