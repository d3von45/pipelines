package org.yudnk.builders

class GradleBuilder implements Builder, Serializable {
    
    @Override
    Map build(Object script, Map config) {
        script.echo "Building with Docker..."
        
        if(config.imageName){
            docker.build("${config.imageName}:${env.BUILD_NUMBER}")
        }

        if(config.push){
            docker.push("${env.BUILD_NUMBER}")
            docker.push("latest")
        }
        
        Map result = [
            success: true,
            artifacts: this.getArtifacts(script, config),
            tool: this.getToolName()
        ]
        
        return result
    }
    
    @Override
    void test(Object script, Map config) {
        script.echo "Running Docker tests..."
    }
    
    @Override
    String getToolName() {
        return 'Docker'
    }

}