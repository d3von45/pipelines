package org.yudnk.utils

class AWSUtils implements Serializable{

    String getSSMParameter(script, String ssmPath){
        
        def rs = script.sh(
            script: "aws ssm get-parameters --name \"${ssmPath}\" --with-decryption --query \"Parameters[].Value\" --output text",
            returnStdout: true
        ).trim()

        return rs

    }

}