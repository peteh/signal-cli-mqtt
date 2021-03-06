package org.asamk.signal.mqtt;

import org.asamk.signal.manager.Manager;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceContent;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;

/**
 * Handler class that passes incoming Signal messages to an mqtt broker.
 */
public class SignalMsgToMqttBridge implements Manager.ReceiveMessageHandler {

    public static final String DEFAULT_TOPIC = "signal-cli/messages/incoming/";

    private static final int DEFAULT_QUALITY_OF_SERVICE = 2;

    private final MqttTopicClient mqttClient;
    private final Manager manager;

    /**
     * Creates a new instance that passes all incoming messages to the provided mqttClient.
     *
     * @param mqttClient the broker to pass all the incoming messages to
     */
    public SignalMsgToMqttBridge(Manager manager, MqttTopicClient mqttClient) {
        this.manager = manager;
        this.mqttClient = mqttClient;
    }

    /**
     * Publishes message on mqtt under the given topic.
     *
     * @param topic   the topic to publish the message on
     * @param content the content of the message
     */
    private void publishMessage(String topic, String content) {
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(DEFAULT_QUALITY_OF_SERVICE);
        try {
            System.out.println("Topic: " + topic);
            System.out.println("Publishing message: " + content);
            mqttClient.publish(topic, message);
        } catch (MqttException ex) {
            System.err.println("Failed to publish message: "+ ex.getMessage());
        }
    }

    @Override
    public void handleMessage(final SignalServiceEnvelope envelope, final SignalServiceContent decryptedContent, final Throwable e) {
        MqttJsonMessage msg = MqttJsonMessage.build(envelope, decryptedContent, e);
        String topic = DEFAULT_TOPIC
                + MqttUtils.stripIllegalTopicCharacters(manager.getUsername()
                + "/" + msg.getSubTopic());

        publishMessage(topic, msg.getJsonContent());
    }
}
