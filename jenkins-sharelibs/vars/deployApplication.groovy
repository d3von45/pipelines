import org.mycompany.deployers.KubernetesDeployer
import org.mycompany.deployers.DockerDeployer
import org.mycompany.notifiers.SlackNotifier
import org.mycompany.utils.GitUtils

def call(Map config = [:]) {
    
    pipeline {
        agent any
        
        environment {
            DEPLOY_ENV = config.environment ?: 'staging'
            IMAGE_NAME = config.image
            IMAGE_TAG = config.tag ?: 'latest'
        }
        
        stages {
            stage('Pre-Deploy Checks') {
                steps {
                    script {
                        echo "Deploying to environment: ${env.DEPLOY_ENV}"
                        echo "Image: ${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                        
                        if (!config.image) {
                            error "Image name is required"
                        }
                    }
                }
            }
            
            stage('Deploy') {
                steps {
                    script {
                        def deployer = getDeployer(config.deployer ?: 'kubernetes')
                        
                        echo "Using deployer: ${deployer.getDeployerName()}"
                        
                        Map deployConfig = buildDeployConfig(config)
                        
                        Map deployResult = deployer.deploy(this, deployConfig)
                        
                        echo "Deployment successful: ${deployResult}"
                        env.DEPLOY_SUCCESS = 'true'
                    }
                }
            }
            
            stage('Verify Deployment') {
                steps {
                    script {
                        if (config.healthCheckUrl) {
                            echo "Running health check: ${config.healthCheckUrl}"
                            
                            int maxRetries = config.healthCheckRetries ?: 5
                            int retryDelay = config.healthCheckDelay ?: 10
                            
                            for (int i = 0; i < maxRetries; i++) {
                                try {
                                    sh "curl -f ${config.healthCheckUrl}"
                                    echo "Health check passed!"
                                    return
                                } catch (Exception e) {
                                    if (i == maxRetries - 1) {
                                        error "Health check failed after ${maxRetries} attempts"
                                    }
                                    echo "Health check attempt ${i + 1} failed, retrying in ${retryDelay}s..."
                                    sleep(retryDelay)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        post {
            success {
                script {
                    if (config.notifySlack) {
                        def notifier = new SlackNotifier()
                        notifier.notifySuccess(
                            this,
                            "Deployment to ${env.DEPLOY_ENV} succeeded: ${env.IMAGE_NAME}:${env.IMAGE_TAG}",
                            [channel: config.slackChannel ?: '#deployments']
                        )
                    }
                }
            }
            failure {
                script {
                    if (config.notifySlack) {
                        def notifier = new SlackNotifier()
                        notifier.notifyFailure(
                            this,
                            "Deployment to ${env.DEPLOY_ENV} failed: ${env.IMAGE_NAME}:${env.IMAGE_TAG}",
                            [channel: config.slackChannel ?: '#deployments']
                        )
                    }
                    
                    if (config.autoRollback && config.previousTag) {
                        echo "Initiating automatic rollback..."
                        def deployer = getDeployer(config.deployer ?: 'kubernetes')
                        deployer.rollback(this, config)
                    }
                }
            }
        }
    }
}

def getDeployer(String deployerType) {
    switch(deployerType.toLowerCase()) {
        case 'kubernetes':
        case 'k8s':
            return new KubernetesDeployer()
        case 'docker':
            return new DockerDeployer()
        default:
            error "Unsupported deployer type: ${deployerType}"
    }
}

Map buildDeployConfig(Map config) {
    Map deployConfig = [
        image: config.image,
        tag: config.tag ?: 'latest'
    ]
    
    if (config.deployer == 'kubernetes' || !config.deployer) {
        deployConfig.namespace = config.namespace ?: config.environment
        deployConfig.deploymentName = config.deploymentName ?: config.image
    }
    
    if (config.deployer == 'docker') {
        deployConfig.containerName = config.containerName ?: config.image
        deployConfig.ports = config.ports ?: []
        deployConfig.environment = config.environmentVariables ?: [:]
    }
    
    return deployConfig
}