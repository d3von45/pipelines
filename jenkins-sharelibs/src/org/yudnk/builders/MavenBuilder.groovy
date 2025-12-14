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
            tool: 'maven'
        ]
    }
}