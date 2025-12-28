package org.yudnk.notificers

class SlackNotifier implements Notifier, Serializable {
    
    @Override
    void notify(Object script, String message, Map config) {
        def channel = config.channel ?: '#general'
        def color = config.color ?: 'good'
        
        script.slackSend(
            channel: channel,
            color: color,
            message: message
        )
    }
    
    @Override
    void notifySuccess(Object script, String message, Map config) {
        config.color = 'good'
        this.notify(script, "✅ SUCCESS: ${message}", config)
    }
    
    @Override
    void notifyFailure(Object script, String message, Map config) {
        config.color = 'danger'
        this.notify(script, "❌ FAILURE: ${message}", config)
    }
}
