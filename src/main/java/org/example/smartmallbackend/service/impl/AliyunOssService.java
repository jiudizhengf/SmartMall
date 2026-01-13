package org.example.smartmallbackend.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.smartmallbackend.common.BusinessException;
import org.example.smartmallbackend.config.AliyunOssProperties;
import org.example.smartmallbackend.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@Slf4j
@Service
public class AliyunOssService implements OssService {
    @Autowired
    private AliyunOssProperties aliyunOssProperties;
    @Override
    public String upload(MultipartFile file) {
        if(file==null||file.isEmpty()){
            throw new BusinessException("上传文件不能为空");
        }
        //构建客户端
        OSS ossClient = new OSSClientBuilder().build(
                aliyunOssProperties.getEndpoint(),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret());
        try{
            //获取文件输入流
            InputStream inputStream=file.getInputStream();
            //构建文件路径
            String originalFilename=file.getOriginalFilename();
            String suffix = StrUtil.subAfter(originalFilename, ".", true);
            String dataPath = DateUtil.format(new Date(), "yyyy/MM/dd");
            String fileName = dataPath+"/"+UUID.randomUUID().toString(true)+"." + suffix;
            //上传文件
            ossClient.putObject(aliyunOssProperties.getBucketName(), fileName, inputStream);
            //构建文件访问url
            return "https://" + aliyunOssProperties.getBucketName() +"." + aliyunOssProperties.getEndpoint() + "/" + fileName;
        }catch (IOException e){
            log.error("文件上传失败",e);
            throw new BusinessException("文件上传失败");
        }catch (Exception e){
            log.error("OSS服务异常",e);
            throw new BusinessException("OSS服务异常");
        }finally {
            //关闭客户端
            if(ossClient!=null){
                ossClient.shutdown();
            }
        }
    }
}
