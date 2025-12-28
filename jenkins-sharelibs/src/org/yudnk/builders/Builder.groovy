package org.yudnk.builders

interface Builder {
    
    def build(script, Map config)

    void test(script, Map config)

    String getToolName()

    void folder(script, Map config)
}