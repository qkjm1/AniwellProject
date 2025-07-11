package com.example.RSW.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

@Controller
public class FileController {

    @GetMapping("/gen/file/download")
    public void downloadFile(@RequestParam("path") String path, HttpServletResponse response) throws IOException {
        String basePath = "C:/upload/";
        String fullPath = basePath + path.replace("/", File.separator);

        System.out.println("▶ 요청 경로 확인: " + fullPath);

        File file = new File(fullPath);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(file.toPath());
        response.setContentType(contentType != null ? contentType : "application/octet-stream");

        String encodedName = URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "inline; filename=\"" + encodedName + "\"");
        response.setContentLengthLong(file.length());

        FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
    }

}
