package com.vertx.verticles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.vertx.contexts.CamelContextProvider;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class RestVerticle  extends AbstractVerticle {
    private HttpServer httpServer = null;
    @Override
    public void start() throws Exception {
        try {
            HttpServer http = vertx.createHttpServer();
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());

            http.requestHandler(router).listen(8980);
            System.out.println("Vertex listening ar port 8980");

           router.route().handler(BodyHandler.create().setDeleteUploadedFilesOnEnd(true));
            router.route("/hello*").handler(StaticHandler.create("webroot"));
            router.get("/greet").handler(this::greet);
            router.get("/downloads/:filename").handler(this::filehandler);
            router.post("/upload").handler(this::uploadHandler);
            router.route("/hello-home").handler(routingContext -> {
                HttpServerResponse response = routingContext.response();
                System.out.println("Hello");
                response.sendFile("webroot/home.html");
            });

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
   
    public void uploadHandler(RoutingContext ctx) {
    	ctx.response().putHeader("Content-Type", "text/plain");

        ctx.response().setChunked(true);

        for (FileUpload f : ctx.fileUploads()) {
          System.out.println("f");
          Buffer fileUploaded = ctx.vertx().fileSystem().readFileBlocking(f.uploadedFileName());
          byte[] b=fileUploaded.getBytes();
          File file=new File("d:\\files\\"+f.fileName());
      
          try {
			FileOutputStream fo=new FileOutputStream(file);
			fo.write(b);
			fo.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
          
          ctx.response().write("Filename: " + f.fileName());
          ctx.response().write("\n");
          ctx.response().write("Size: " + f.size());
        }

        ctx.response().end();
      
    }

    public void filehandler(RoutingContext context){
          HttpServerResponse response=context.response();
          response.setStatusCode(200);
          HttpServerRequest request=context.request();
          String filename=request.getParam("filename");
          System.out.println("running file name is"+filename);
          AmazonS3 client= CamelContextProvider.asclient;
         S3Object fullObject = client.getObject(new GetObjectRequest("vinodhbucket1998", filename+".pdf"));
          response.putHeader("Content-Disposition","attachment; filename="+filename+".pdf");
          S3ObjectInputStream istream=fullObject.getObjectContent();


       try{
            response.end(Buffer.buffer(IOUtils.toByteArray(istream)));

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public void greet(RoutingContext context){
        try {
            System.out.println("Called");
            HttpServerResponse response = context.response();
            response.setStatusCode(200);
            response.headers()
                    .add("Content-Type", "text/html");
            response.setChunked(true);
            response.write("World is good n Great");
            response.end();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }
}
