package com.x.xaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String result = tool.executeTerminalCommand("cmd /c dir");
        System.out.println(result);
        assertNotNull(result);
    }
}