package com.flow.workflow.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowRabbitConfig {

    public static final String WORKFLOW_EXCHANGE = "workflow.events";

    @Bean
    public TopicExchange workflowEventsExchange() {
        return ExchangeBuilder.topicExchange(WORKFLOW_EXCHANGE).durable(true).build();
    }

    @Bean
    public Jackson2JsonMessageConverter workflowJsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate workflowRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(workflowJsonConverter());
        return template;
    }
}
