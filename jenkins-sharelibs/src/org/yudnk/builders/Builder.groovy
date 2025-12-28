package org.yudnk.builders

interface Builder {
    
    def build(script, Map config)

    void test(script, Map config)

    String getToolName()

    void folder(script, Map config)

    void folder(script, Map config){
        if (config.srcFolder) {
            script.echo "Move to source folder: ${config.srcFolder}"
            script.sh "cd ${config.srcFolder}"
        }
        
    }
}