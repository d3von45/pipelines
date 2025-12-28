package org.yudnk.notificers

interface Notifier {
    void notify(Object script, String message, Map config)
    void notifySuccess(Object script, String message, Map config)
    void notifyFailure(Object script, String message, Map config)
}
