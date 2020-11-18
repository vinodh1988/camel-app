package com.vertx.verticles;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.vertx.contexts.CamelContextProvider;
import com.vertx.model.Invoice;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.camel.InboundMapping;
import io.vertx.core.AbstractVerticle;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.s3.S3Constants;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.main.Main;

import javax.xml.bind.JAXBContext;

public class CamelVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        CamelContext camel = CamelContextProvider.getContext();
        CamelBridge bridge = CamelBridge.create(vertx,
                new CamelBridgeOptions(camel).addInboundMapping(
                        InboundMapping.fromCamel("seda:end").toVertx("queue"))
                      );




        camel.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("activemq:queue:firstQueue").to("seda:end");
            }
        });

        camel.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("file:d:/files?delete=true").to("activemq:queue:fileQueue");
            }
        });

        camel.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                try{

                    JaxbDataFormat xmlDataFormat = new JaxbDataFormat();
                    JAXBContext con = JAXBContext.newInstance(Invoice.class);
                    xmlDataFormat.setContext(con);

                from("activemq:queue:fileQueue")
                        .process()
                        .message(this::setExtensionHeader)
                        .choice()
                        .when(header("ext").isEqualTo("xml"))
                        .setHeader("CamelAwsS3Key",simple("${in.header.CamelFileName}"))
                        .to("aws-s3://vinodhbucket1998?amazonS3Client=#s3Client")
                        .when(header("ext").isEqualTo("pdf"))
                        .setHeader("CamelAwsS3Key",simple("${in.header.CamelFileName}"))
                        .setHeader(S3Constants.CONTENT_TYPE,simple("application/octet-stream"))
                        .to("aws-s3://vinodhbucket1998?amazonS3Client=#s3Client")
                        .endChoice()
                        .end()
                        .process()
                        .message(this::setExtensionHeader)
                        .choice()
                         .when(header("ext").isEqualTo("xml"))
                                 .unmarshal(xmlDataFormat).process(exchange -> {
                                    Invoice i=exchange.getIn().getBody(Invoice.class);
                                     System.out.println(i);
                                     exchange.getOut().setBody(
                                             "insert into invoice(invoiceno,name,date) values" +
                                                     "("+i.getInvoiceno()+", '"+i.getName()+"', '"+
                                    i.getDate()+"')");
                        })
                        .to("jdbc:mysqlDataSource")
                        .when(header("ext").isEqualTo("pdf"))
                        .process(exchange -> {
                              String filename=(String)exchange.getIn().getHeader("CamelFileName");
                              Integer invoice=Integer.parseInt(filename.replace("invoice","")
                                      .replace(".pdf",""));
                              exchange.getOut().setBody(
                                      "update invoice set filename='"+filename+"'"+" where invoiceno="+invoice
                              );
                        })
                        .to("jdbc:mysqlDataSource")
                       ;







                }
                catch(Exception e){
                 e.printStackTrace();
                }
            }

            private void setExtensionHeader(Message m) {
                String fileName = (String) m.getHeader("CamelFileName");
                String ext = fileName.substring(fileName.lastIndexOf(".")+1);
                m.setHeader("filename",fileName);
                m.setHeader("ext", ext);
            }

            });


        vertx.eventBus().consumer("queue",(message)->{
            System.out.println(message.body());

            message.reply("Got it");
        });


        bridge.start();


    }


    public void processData(Exchange exchange){
        try{
            System.out.println(exchange.getIn());
            System.out.println(exchange.getIn().getHeaders());
            System.out.println(exchange.getProperties());
            Byte[] i=(Byte[]) exchange.getIn().getBody(Byte[].class);
            System.out.println(i);
        }
        catch(Exception e){
            e.printStackTrace();}
    }

}
