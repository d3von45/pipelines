package org.yudnk.deployers

class KubernetesDeployer implements Deployer, Serializable {
    
    @Override
    Map deploy(Object script, Map config) {
        script.echo "Deploying to Kubernetes..."
        
        def namespace = config.namespace ?: 'default'
        def deploymentName = config.deploymentName
        def image = config.image
        def tag = config.tag ?: 'latest'
        
        script.sh """
            kubectl set image deployment/${deploymentName} \
                ${deploymentName}=${image}:${tag} \
                -n ${namespace}
            
            kubectl rollout status deployment/${deploymentName} \
                -n ${namespace} \
                --timeout=5m
        """
        
        Map result = [
            success: true,
            namespace: namespace,
            deployment: deploymentName,
            image: "${image}:${tag}"
        ]
        
        return result
    }
    
    @Override
    void rollback(Object script, Map config) {
        script.echo "Rolling back Kubernetes deployment..."
        
        def namespace = config.namespace ?: 'default'
        def deploymentName = config.deploymentName
        
        script.sh """
            kubectl rollout undo deployment/${deploymentName} \
                -n ${namespace}
        """
    }
    
    @Override
    String getDeployerName() {
        return 'Kubernetes'
    }
}
