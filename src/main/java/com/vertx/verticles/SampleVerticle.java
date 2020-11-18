package com.vertx.verticles;

import io.vertx.core.AbstractVerticle;

public class SampleVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        System.out.println("Vertx is started");
    }


}
