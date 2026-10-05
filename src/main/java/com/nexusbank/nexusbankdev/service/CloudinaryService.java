package com.nexusbank.nexusbankdev.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final boolean isReady;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name:${CLOUDINARY_CLOUD_NAME:zocngsum}}") String cloudName,
            @Value("${cloudinary.api_key:${CLOUDINARY_API_KEY:761558376138655}}") String apiKey,
            @Value("${cloudinary.api_secret:${CLOUDINARY_API_SECRET:}}") String apiSecret) {

        if (apiSecret != null && !apiSecret.trim().isEmpty()) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            this.cloudinary = new Cloudinary(config);
            this.isReady = true;
        } else {
            this.cloudinary = null;
            this.isReady = false;
        }
    }

    public Map<String, String> upload(byte[] fileBytes, String originalFilename, String folder) throws IOException {
        if (isReady && cloudinary != null) {
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folder != null ? folder : "nexusbank/kyc",
                    "resource_type", "auto"
            );
            Map<?, ?> uploadResult = cloudinary.uploader().upload(fileBytes, params);
            String url = (String) uploadResult.get("secure_url");
            if (url == null) url = (String) uploadResult.get("url");
            String publicId = (String) uploadResult.get("public_id");

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("publicId", publicId);
            return result;
        } else {
            // Development fallback when CLOUDINARY_API_SECRET is pending environment deployment
            String safeName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") : "doc.pdf";
            String publicId = "nexusbank/kyc/" + System.currentTimeMillis() + "_" + safeName;
            String url = "https://res.cloudinary.com/zocngsum/image/upload/v1/" + publicId;

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("publicId", publicId);
            return result;
        }
    }

    public Map<String, String> uploadMultipart(MultipartFile file, String folder) throws IOException {
        return upload(file.getBytes(), file.getOriginalFilename(), folder);
    }
}
