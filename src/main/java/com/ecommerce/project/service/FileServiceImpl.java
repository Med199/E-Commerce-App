package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadImage(String path, MultipartFile image) throws IOException {
        // Filename of original file
        String originalFileName = image.getOriginalFilename();

        // Generate a unique file name
        String randomId = UUID.randomUUID().toString();

        //build the new file Name ( myImage.png -> randomId.png )
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));
        String filePath = path+ File.separator+ fileName;

        // Check if path exist, otherwise create this path
        File folder = new File(path);
        if(!folder.exists())
            folder.mkdir();

        // Upload to server
        Files.copy(image.getInputStream(), Paths.get(filePath));

        // Return the new FileName
        return fileName;
    }
}
