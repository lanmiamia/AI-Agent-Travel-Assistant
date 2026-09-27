package com.yupi.yup_agent.constant;

/**
 * @ Gareth Bale
 * @ version 1.0
 */
public interface FileConstant {
    /*
    * 文件保存目录
    * */
    //System.getProperty("user.dir")获取程序当前的工作目录，即D:\JAVAproject0\Complete_Project\YUP_Agent
    //和src、target在同一目录下
    String FILE_SAVE_DIR = System.getProperty("user.dir") + "/tmp";

}
