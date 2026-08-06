package com.x.xaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.x.xaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类
 */
public class FileOperationTool {
    @Tool(description = "Read file content form a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        String url = FileConstant.FILE_PATH + "/" + fileName;
        String content = null;
        try {
            return content = FileUtil.readUtf8String(url);
        } catch (Exception e) {
            return "读取文件失败,url：" + url + ",错误信息：" + e.getMessage();
        }
    }

    @Tool(description = "Write file content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {

        String filePath = FileConstant.FILE_PATH + "/" + fileName;
        try {
            FileUtil.mkParentDirs(filePath);
            FileUtil.writeUtf8String(content, filePath);
            return "写入文件成功,url：" + filePath;
        } catch (Exception e) {
            return "写入文件失败,url：" + filePath + ",错误信息：" + e.getMessage();
        }
    }
}
