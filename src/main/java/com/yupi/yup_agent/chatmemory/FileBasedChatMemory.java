package com.yupi.yup_agent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.*;
import java.util.*;

/**
 * @ Gareth Bale
 * @ version 1.0
 */

/* Message的子类没有实现Serializable序列化接口,因此不能用JSON进行序列化 */
/* 取而代之的是 Kyro */
public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;

    private static final Kryo kryo = new Kryo();

    static{
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时,指定文件保存目录
    public FileBasedChatMemory(String dir){
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()){
            baseDir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, Message message) {
        ChatMemory.super.add(conversationId, message);
    }

    //读文件——>添加——>写文件
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> messageList = readConversation(conversationId);
        messageList.addAll(messages);
        writeConversation(conversationId,messageList);
    }

    //读文件——>获取最后N条message
    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> messageList = readConversation(conversationId);
        return messageList.stream()
                .skip(Math.max(0,messageList.size()-lastN))
                .toList();
    }

    @Override
    public void clear(String conversationId) {

    }

    //读文件
    //指定路径的.kyro后缀的文件通过Input类读进来变成——>ArrayList
    private List<Message> readConversation(String conversationId){
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()){
            try(Input input = new Input(new FileInputStream(file))){
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }


    //写文件
    //通过kyro的Output类将List<Message>写到指定逻辑文件路径File
    private void writeConversation(String conversationId, List<Message> messages){
        File file = getConversationFile(conversationId);
        try(Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output,messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //字符串——>逻辑文件路径
    //根据会话ID conversationId创建新逻辑文件
    private File getConversationFile(String conversationId){
        return new File(BASE_DIR,conversationId + ".kyro");
    }
}
