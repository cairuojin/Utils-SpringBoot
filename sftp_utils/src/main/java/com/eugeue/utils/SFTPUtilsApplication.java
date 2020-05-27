package com.eugeue.utils;

import com.eugeue.utils.sftp.SFtpHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;


@SpringBootApplication
@ServletComponentScan
public class SFTPUtilsApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SFTPUtilsApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SFTPUtilsApplication.class, args);

        //开启方法
        testConnect();
    }

    /**
     * 连接到SFTP服务器，然后断开
     */
    public static void testConnect(){
        SFtpHelper sFtpHelper = new SFtpHelper();
        try {
            sFtpHelper.connectServer("172.16.24.126",22,"ftpUser1","iBsop@2015",null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sFtpHelper.closeConnect();
        }
    }


    /**
     * 创建目录并上传文件
     */
    public static void testMkdirAndUpload(){
        SFtpHelper sFtpHelper = new SFtpHelper();
        try {
            sFtpHelper.connectServer("172.16.24.126",22,"ftpUser1","iBsop@2015",null);

            //测试创建目录
            boolean mkdirStatus = sFtpHelper.makesDirectory("/home/ftpUser1/data/bill");
            if (mkdirStatus) {
                //测试从本地上传文件
                File file = new File("/usr/tmp/a.txt");  //todo 本地要上传的文件
                FileInputStream inputStream = new FileInputStream(file);
                sFtpHelper.uploadFile(inputStream,"/home/ftpUser1/data/bill" + File.separator + file.getName(), "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sFtpHelper.closeConnect();
        }
    }


    /**
     * 下载文件
     */
    public static void testDownload(){
        SFtpHelper sFtpHelper = new SFtpHelper();
        try {
            sFtpHelper.connectServer("172.16.24.126",22,"ftpUser1","iBsop@2015",null);

            //测试下载文件
            sFtpHelper.downloadFile("/home/ftpUser1/data/bill/a.txt", "/usr/tmp/download.txt");

            File file = new File("/usr/tmp/download1.txt");
            FileOutputStream outputStream = new FileOutputStream(file);
            sFtpHelper.downloadFile("/home/ftpUser1/data/bill/a.txt", outputStream);


            InputStream inputStream = sFtpHelper.downloadFileInputStream("/home/ftpUser1/data/bill/a.txt");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sFtpHelper.closeConnect();
        }
    }

    /**
     * 删除文件
     */
    public static void testDelete(){
        SFtpHelper sFtpHelper = new SFtpHelper();
        try {
            sFtpHelper.connectServer("172.16.24.126",22,"ftpUser1","iBsop@2015",null);

            //测试删除文件
            sFtpHelper.deleteFile("/home/ftpUser1/data/bill/a.txt");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sFtpHelper.closeConnect();
        }
    }


    /**
     * 删除文件夹
     */
    public static void testDeleteDirectory(){
        SFtpHelper sFtpHelper = new SFtpHelper();
        try {
            sFtpHelper.connectServer("172.16.24.126",22,"ftpUser1","iBsop@2015",null);

            String dir = "/home/ftpUser1";
            //先判断是否是文件夹
            boolean isDirectory = sFtpHelper.isDirectory(dir);
            if (isDirectory) {
                //如果文件夹不为空，无法直接rm
                //sFtpHelper.deleteEmptyDirectory(dir);

                //测试删除不为空的文件夹
                sFtpHelper.deleteDirectory(dir);
            } else {
                System.out.println("不是文件夹");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sFtpHelper.closeConnect();
        }
    }


    /**
     * 进入文件夹并修改文件名字
     */
    public static void testCdAndRename(){
        SFtpHelper sFtpHelper = new SFtpHelper();
        try {
            sFtpHelper.connectServer("172.16.24.126",22,"ftpUser1","iBsop@2015",null);

            String dir = "/home/ftpUser1/data/bill/";
            //先进入文件夹
            sFtpHelper.changeWorkingDirectory(dir);
            //修改文件名
            sFtpHelper.renameFile("a.txt","b.txt");

            //等同于
            //sFtpHelper.renameFile(dir + "a.txt" , dir + "b.txt");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sFtpHelper.closeConnect();
        }
    }
}
