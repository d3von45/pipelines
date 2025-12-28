package org.yudnk.utils

class GitUtils implements Serializable {
    
    private Object script
    
    GitUtils(Object script) {
        this.script = script
    }
    
    String getCurrentBranch() {
        String result = this.script.sh(
            script: 'git rev-parse --abbrev-ref HEAD',
            returnStdout: true
        ).trim()
        return result
    }
    
    String getCommitHash() {
        String result = this.script.sh(
            script: 'git rev-parse HEAD',
            returnStdout: true
        ).trim()
        return result
    }
    
    String getShortCommitHash() {
        String result = this.script.sh(
            script: 'git rev-parse --short HEAD',
            returnStdout: true
        ).trim()
        return result
    }
    
    String getCommitMessage() {
        String result = this.script.sh(
            script: 'git log -1 --pretty=%B',
            returnStdout: true
        ).trim()
        return result
    }
    
    String getCommitAuthor() {
        String result = this.script.sh(
            script: 'git log -1 --pretty=%an',
            returnStdout: true
        ).trim()
        return result
    }
    
    boolean hasChanges(String path = '.') {
        String status = this.script.sh(
            script: "git status --porcelain ${path}",
            returnStdout: true
        ).trim()
        return status != ''
    }
}
