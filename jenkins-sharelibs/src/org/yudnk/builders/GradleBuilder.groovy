package org.yudnk.builders

class GradleBuilder implements Builder, Serializable {
    
    @Override
    Map build(Object script, Map config) {
        script.echo "Building with Gradle..."
        
        def tasks = config.tasks ?: 'clean build'
        def additionalArgs = config.additionalArgs ?: ''
        
        script.sh """
            ./gradlew ${tasks} ${additionalArgs} \
                ${config.skipTests ? '-x test' : ''}
        """
        
        Map result = [
            success: true,
            artifacts: this.getArtifacts(script, config),
            tool: 'gradle'
        ]
        
        return result
    }
    
    @Override
    void test(Object script, Map config) {
        script.echo "Running Gradle tests..."
        
        def testTask = config.testTask ?: 'test'
        
        script.sh "./gradlew ${testTask}"
        
        // Publish test results
        script.junit '**/build/test-results/test/*.xml'
    }
    
    @Override
    String getToolName() {
        return 'Gradle'
    }
    
    private List getArtifacts(Object script, Map config) {
        def artifactPattern = config.artifactPattern ?: '**/build/libs/*.jar'
        return script.findFiles(glob: artifactPattern) as List
    }

}