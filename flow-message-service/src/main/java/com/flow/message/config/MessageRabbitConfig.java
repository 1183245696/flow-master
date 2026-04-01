package com.flow.message.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageRabbitConfig {

    // Exchanges
    public static final String WORKFLOW_EXCHANGE = "workflow.events";
    public static final String USER_EXCHANGE     = "user.events";

    // Queues
    public static final String WORKFLOW_MSG_QUEUE = "workflow.message.queue";
    public static final String USER_MSG_QUEUE     = "user.message.queue";

    @Bean
    public TopicExchange workflowExchange() {
        return ExchangeBuilder.topicExchange(WORKFLOW_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder.topicExchange(USER_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue workflowMessageQueue() {
        return QueueBuilder.durable(WORKFLOW_MSG_QUEUE).build();
    }

    @Bean
    public Queue userMessageQueue() {
        return QueueBuilder.durable(USER_MSG_QUEUE).build();
    }

    @Bean
    public Binding workflowQueueBinding() {
        return BindingBuilder.bind(workflowMessageQueue())
                .to(workflowExchange())
                .with("workflow.task.#");
    }

    @Bean
    public Binding userQueueBinding() {
        return BindingBuilder.bind(userMessageQueue())
                .to(userExchange())
                .with("user.#");
    }

    @Bean
    public Jackson2JsonMessageConverter messageJsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate messageRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageJsonConverter());
        return template;
    }
}
