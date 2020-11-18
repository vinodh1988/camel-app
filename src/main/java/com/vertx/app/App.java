package com.vertx.app;

import com.vertx.verticles.CamelVerticle;
import com.vertx.verticles.RestVerticle;
import com.vertx.verticles.SampleVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;


public class App 
{
    public static void main( String[] args )
    {

        Vertx vertx=Vertx.vertx( new VertxOptions()
                .setBlockedThreadCheckInterval(10000000)
                .setMaxEventLoopExecuteTime(10000000)
                .setMaxWorkerExecuteTime(120000000));




        vertx.deployVerticle(new SampleVerticle());
        vertx.deployVerticle(new RestVerticle());
        vertx.deployVerticle(new CamelVerticle());
    }
}
