package com.vertx.contexts;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import javax.jms.ConnectionFactory;



public class CamelContextProvider {
    public static CamelContext context=null;
    public static AmazonS3 asclient=null;
    static
    {
        try {
            String secretkey=System.getenv("AWSSecretKey");

            BasicAWSCredentials awsCreds = new BasicAWSCredentials("XXXXXXXXXXXXXXXXXXXXXXX",
                    "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX ");  //fill your credentials
            SimpleRegistry registry=new SimpleRegistry();
            asclient=AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                    .withRegion(Regions.AP_SOUTH_1).build();
            registry.put("s3Client", asclient);

            MysqlDataSource dataSource=new MysqlDataSource();
            dataSource.setUrl("jdbc:mysql://localhost:3306/isql");
            dataSource.setUser("root");
            dataSource.setPassword("password");
            registry.put("mysqlDataSource",dataSource);


            context=new DefaultCamelContext(registry);
            ConnectionFactory c=new ActiveMQConnectionFactory();
            context.addComponent("activemq",  JmsComponent.jmsComponentAutoAcknowledge(c));
            System.out.println("Component added..!!!");
            context.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CamelContext getContext(){

        return context;
    }
}
