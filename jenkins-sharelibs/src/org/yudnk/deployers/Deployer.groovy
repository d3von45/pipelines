package org.yudnk.deployers

interface Deployer {
    Map deploy(Object script, Map config)
    void rollback(Object script, Map config)
    String getDeployerName()
}
