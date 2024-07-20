package com.apesconsole.silulator.vbom_reciver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class JsonFileWriterService {

    private final ObjectMapper objectMapper;

    public JsonFileWriterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeJsonToFile(Object data, String fileName) throws IOException {
        String targetDir = "target/";
        File dir = new File(targetDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = Paths.get(targetDir, fileName).toFile();
        objectMapper.writeValue(file, data);
    }
}