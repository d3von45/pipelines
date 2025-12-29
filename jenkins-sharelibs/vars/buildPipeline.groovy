import org.mycompany.builders.MavenBuilder
import org.mycompany.builders.GradleBuilder
import org.mycompany.notifiers.SlackNotifier
import org.mycompany.notifiers.EmailNotifier
import org.mycompany.utils.GitUtils

/**
 * Standard build pipeline with OOP structure
 * 
 * Usage:
 * buildPipeline {
 *     buildTool = 'maven'  // or 'gradle'
 *     runTests = true
 *     notifySlack = true
 *     slackChannel = '#builds'
 * }
 */
def call(Map config = [:]) {
    
    pipeline {
        agent any
        
        environment {
            BUILD_TOOL = config.buildTool ?: 'maven'
        }
        
        stages {
            stage('Checkout') {
                steps {
                    script {
                        checkout scm
                        
                        // Use GitUtils
                        def gitUtils = new GitUtils(this)
                        env.GIT_COMMIT_SHORT = gitUtils.getShortCommitHash()
                        env.GIT_BRANCH = gitUtils.getCurrentBranch()
                        env.GIT_AUTHOR = gitUtils.getCommitAuthor()
                        
                        echo "Building branch: ${env.GIT_BRANCH}"
                        echo "Commit: ${env.GIT_COMMIT_SHORT}"
                        echo "Author: ${env.GIT_AUTHOR}"
                    }
                }
            }
            
            stage('Build') {
                steps {
                    script {
                        // Factory pattern to select builder
                        def builder = getBuilder(env.BUILD_TOOL)
                        
                        echo "Using builder: ${builder.getToolName()}"
                        
                        Map buildConfig = [
                            skipTests: !config.runTests,
                            profiles: config.profiles ?: [],
                            additionalArgs: config.buildArgs ?: ''
                        ]
                        
                        Map buildResult = builder.build(this, buildConfig)
                        
                        echo "Build completed with ${buildResult.tool}"
                        env.BUILD_SUCCESS = 'true'
                    }
                }
            }
            
            stage('Test') {
                when {
                    expression { config.runTests != false }
                }
                steps {
                    script {
                        def builder = getBuilder(env.BUILD_TOOL)
                        
                        Map testConfig = [
                            profiles: config.profiles ?: []
                        ]
                        
                        builder.test(this, testConfig)
                    }
                }
            }
            
            stage('Archive') {
                steps {
                    script {
                        def artifactPattern = config.artifactPattern ?: 
                            (env.BUILD_TOOL == 'maven' ? '**/target/*.jar' : '**/build/libs/*.jar')
                        
                        archiveArtifacts artifacts: artifactPattern, allowEmptyArchive: false
                    }
                }
            }
        }
        
        post {
            success {
                script {
                    sendNotifications(
                        "Build #${env.BUILD_NUMBER} succeeded for ${env.JOB_NAME}",
                        'success',
                        config
                    )
                }
            }
            failure {
                script {
                    sendNotifications(
                        "Build #${env.BUILD_NUMBER} failed for ${env.JOB_NAME}",
                        'failure',
                        config
                    )
                }
            }
        }
    }
}

def getBuilder(String buildTool) {
    switch(buildTool.toLowerCase()) {
        case 'maven':
            return new MavenBuilder()
        case 'gradle':
            return new GradleBuilder()
        default:
            error "Unsupported build tool: ${buildTool}"
    }
}

void sendNotifications(String message, String status, Map config) {
    if (config.notifySlack) {
        def slackNotifier = new SlackNotifier()
        Map slackConfig = [
            channel: config.slackChannel ?: '#builds'
        ]
        
        if (status == 'success') {
            slackNotifier.notifySuccess(this, message, slackConfig)
        } else {
            slackNotifier.notifyFailure(this, message, slackConfig)
        }
    }
    
    if (config.notifyEmail) {
        def emailNotifier = new EmailNotifier()
        Map emailConfig = [
            recipients: config.emailRecipients ?: 'team@example.com'
        ]
        
        if (status == 'success') {
            emailNotifier.notifySuccess(this, message, emailConfig)
        } else {
            emailNotifier.notifyFailure(this, message, emailConfig)
        }
    }
}
