package org.yudnk.builders

interface Builder {
    
    def build(script, Map config)

    def test(script, Map config)

    String getToolName()
}