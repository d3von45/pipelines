package org.yudnk.builders

class MavenBuilder implements Builder, Serializable {

    @Override
    def build(script, Map config){
        script.echo "Build with maven...."

        def goals = config.goals ?:'clean package'
        def profiles = config.profiles ? "-P${config.profiles.join(',')}" : ''
        def additionalArgs = config.additionalArgs?: ''

        script.sh """
            mvn ${goals} ${profiles} ${additionalArgs} \
                -DskipTests=${config.skipTests ?: false}
        """

        return [
            success: true,
            tool: this.getToolName()
        ]
    }

    @Override
    void test(Object script, Map config) {
        script.echo "Running Maven tests..."
        
        def testGoal = config.testGoal ?: 'test'
        def profiles = config.profiles ? "-P${config.profiles.join(',')}" : ''
        
        script.sh "mvn ${testGoal} ${profiles}"
        
        // Publish test results
        script.junit '**/target/surefire-reports/*.xml'
    }
    
    @Override
    String getToolName() {
        return 'Maven'
    }

    @Override
    void folder(script, Map config){
        if (config.srcFolder) {
            script.echo "Move to source folder: ${config.srcFolder}"
            script.sh "cd ${config.srcFolder}"
        }
        
    }
}