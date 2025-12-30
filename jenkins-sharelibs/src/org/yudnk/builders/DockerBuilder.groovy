package org.yudnk.builders

class DockerBuilder implements Builder, Serializable {

    def defaultConfig = [
        imageName = "",
        context = ".",
        dockerfile = "Dockerfile"
        tags: []
    ]
    
    @Override
    Map build(Object script, Map config) {

        script.echo "Building with Docker..."

        config = defaultConfig + config

        def image

        script.echo(config)
        
        // if(config.imageName){
        //     image = docker.build("users-api:${env.BUILD_NUMBER}", "-f ${config.dockerfile} ${config.context}")
        // }

        image = docker.build("users-api:${env.BUILD_NUMBER}", "-f ${config.dockerfile} ${config.context}")

        
        Map result = [
            success: true,
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