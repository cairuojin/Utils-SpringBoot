package com.eugeue.utils.sftp;

import com.jcraft.jsch.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * SFTP工具类
 * Created by cairuojin on 2020/05/26.
 */
public class SFtpHelper {
    private static Logger logger = LoggerFactory.getLogger(SFtpHelper.class);

    private String userName;  // SFTP 登录用户名
    private String password;  // SFTP 登录密码
    private String privateKey;// SFTP 私钥
    private String ip; // FTP 服务器地址IP地址
    private int port; // FTP 端口
    private ChannelSftp sftp;
    private Session session;// SFTP 客户端代理

    /**
     * 连接到服务器
     * @return true 连接服务器成功，false 连接服务器失败
     */
    public boolean connectServer(String ip, int port, String userName, String password, String privateKey) {
        boolean flag = true;
        if (sftp == null) {
            try {
                JSch jsch = new JSch();
                if (privateKey != null) {
                    jsch.addIdentity(privateKey);// 设置私钥
                }

                session = jsch.getSession(userName, ip, port);

                if (password != null) {
                    session.setPassword(password);
                }
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");

                session.setConfig(config);
                session.setTimeout(5000);       //设置连接超时
                session.connect();

                Channel channel = session.openChannel("sftp");
                channel.connect();

                sftp = (ChannelSftp) channel;
            } catch (JSchException e) {
                e.printStackTrace();
                flag = false;
            }
        }
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.privateKey = privateKey;
        return flag;
    }

    /**
     * 关闭连接
     */
    public void closeConnect() {
        try {
            if (sftp != null) {
                if (sftp.isConnected()) {
                    sftp.disconnect();
                }
            }
            if (session != null) {
                if (session.isConnected()) {
                    session.disconnect();
                }
            }
        } catch (Exception e) {
            logger.error("关闭连接失败！", e);
        }
    }

    /**
     * 创建目录，支持连级创建
     * @param path 要创建的目录，目录间用"/"号分开，例如:/dir1/dir2
     * @return 返回true创建成功，返回false创建失败
     */
    public boolean makesDirectory(String path) {
        if (StringUtils.isBlank(path))
            return false;

        try {
            StringBuffer currentPath = new StringBuffer("/");

            String[] paths = path.split("/");
            for (int i = 0; i < paths.length; i++) {

                currentPath.append(paths[i]);
                currentPath.append("/");

                try {
                    sftp.cd(currentPath.toString());
                } catch (Exception e) {
                    sftp.mkdir(currentPath.toString());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("创建目录失败！", e);
            return false;
        }
    }

    /**
     * 上传文件
     * @param inputStream 文件的输入流
     * @param fileName    文件名字
     * @param targetDir   文件上传的目标目录
     * @return
     */
    public boolean uploadFile(InputStream inputStream, String fileName, String targetDir) throws Exception {
        try {
            sftp.put(inputStream, fileName);  //上传文件
            return true;
        } catch (SftpException e) {
            return false;
        }
    }


    /**
     * 下载文件
     * @param remoteFileName 服务器上的文件名
     * @param localFileName  本地文件名
     * @return true 下载成功，false 下载失败
     */
    public boolean downloadFile(String remoteFileName, String localFileName) {
        // 下载文件
        BufferedOutputStream buffOut = null;
        try {
            buffOut = new BufferedOutputStream(new FileOutputStream(localFileName));
            sftp.get(remoteFileName, buffOut);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("本地文件下载失败！", e);
            return false;
        } finally {
            try {
                if (buffOut != null)
                    buffOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载文件
     * @param remoteFileName 服务器上的文件名
     * @param out            本地文件名
     * @return true 下载成功，false 下载失败
     */
    public boolean downloadFile(String remoteFileName, OutputStream out) {
        try {
            sftp.get(remoteFileName, out);
            return true;
        } catch (Exception e) {
            logger.error("本地文件下载失败！", e);
            return false;
        }
    }

    /**
     * 下载文件，返回文件流
     * @param remoteFileName
     * @return
     */
    public InputStream downloadFileInputStream(String remoteFileName) {
        InputStream in = null;
        try {
            in = sftp.get(remoteFileName);
        } catch (Exception e) {
            logger.error("本地文件下载失败！", e);
        }

        return in;
    }

    /**
     * 删除一个文件
     */
    public boolean deleteFile(String filename) {
        boolean flag = true;
        try {
            sftp.rm(filename);
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 判断是否为目录
     *
     * @param pathname 要判断的路径
     * @return 返回true表示当前路径是目录，反之为不存在
     * @throws IOException
     */
    public boolean isDirectory(String pathname) {
        boolean isExist = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(pathname);
            isExist = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isExist = false;
            }
        }
        return isExist;
    }

    /**
     * 删除目录
     */
    public void deleteDirectory(String pathname) {
        try {
            boolean isDirectory = this.isDirectory(pathname);
            if (isDirectory) {
                Vector<ChannelSftp.LsEntry> subFiles = sftp.ls(pathname);
                if (subFiles != null && subFiles.size() > 0) {
                    Iterator<ChannelSftp.LsEntry> iterator = subFiles.iterator();
                    while (iterator.hasNext()) {
                        String subFileName = iterator.next().getFilename();
                        if (".".equals(subFileName) || "..".equals(subFileName))
                            continue;
                        String filename = pathname + File.separator + subFileName;
                        if (this.isDirectory(filename)) {
                            this.deleteDirectory(filename);
                        } else {
                            deleteFile(filename);
                        }
                    }
                }
            } else {
                deleteFile(pathname);
            }

            deleteEmptyDirectory(pathname);
        } catch (Exception e) {
            logger.error("删除目录失败！", e);
        }
    }


    /**
     * 删除空目录
     */
    public void deleteEmptyDirectory(String pathname) {
        try {
            sftp.rmdir(pathname);
        } catch (SftpException e) {
            logger.error("删除空目录失败！", e);
            e.printStackTrace();
        }
    }

    /**
     * 进入到服务器的某个目录下
     *
     * @param directory
     */
    public boolean changeWorkingDirectory(String directory) {
        try {
            sftp.cd(directory);
            return true;
        } catch (SftpException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 重命名文件
     *
     * @param oldFileName 原文件名
     * @param newFileName 新文件名
     */
    public void renameFile(String oldFileName, String newFileName) {
        try {
            sftp.rename(oldFileName, newFileName);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public ChannelSftp getSftp() {
        return sftp;
    }

    public void setSftp(ChannelSftp sftp) {
        this.sftp = sftp;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}