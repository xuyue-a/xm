package com.example.controller;


import cn.hutool.core.io.FileUtil;
import com.example.common.Result;
import com.example.exception.CustomException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files")
public class FileController {
    //System.getProperty("user.dir")   获取当前项目的根路径
    //文件上传目录的路径
    private static final String filePath = System.getProperty("user.dir") + "/files/";

    //   文件上传
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {  //文件流的形式接收前端发过来的文件
        String originalFilename = file.getOriginalFilename(); //xxx.png
        if(!FileUtil.isDirectory(filePath)){  // 如果目录不存在 需要创建目录
            FileUtil.mkdir(filePath);   //创建一个 files 目录
        }
        //  提供文件存储完整的路径
        //  给文件名加一个唯一的标识System.currentTimeMillis()时间戳
        String fileName = System.currentTimeMillis() + "_" + originalFilename;
        String realPath = filePath + fileName; //完整的文件路径
        try {
            FileUtil.writeBytes(file.getBytes(), realPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("500","文件上传失败");
        }
        //返回一个网络链接
        String url = "http://localhost:8080/files/download/" + fileName;
        return Result.success(url);
    }


    //   文件下载
    @GetMapping("/download/{fileName}")
    public void downlaod(@PathVariable String fileName, HttpServletResponse response) {
        try {
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            OutputStream os = response.getOutputStream();
            String realPath = filePath + fileName; //完整的文件路径
            //获取到文件的字节数组
            byte[] bytes = FileUtil.readBytes(realPath);
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException("500","文件下载失败");
        }
    }

}
